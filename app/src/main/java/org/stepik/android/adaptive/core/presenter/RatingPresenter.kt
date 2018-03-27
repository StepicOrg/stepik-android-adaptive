package org.stepik.android.adaptive.core.presenter

import io.reactivex.BackpressureStrategy
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import org.stepik.android.adaptive.api.API
import org.stepik.android.adaptive.core.presenter.contracts.RatingView
import org.stepik.android.adaptive.data.SharedPreferenceMgr
import org.stepik.android.adaptive.data.db.DataBaseMgr
import org.stepik.android.adaptive.data.model.RatingItem
import org.stepik.android.adaptive.di.qualifiers.BackgroundScheduler
import org.stepik.android.adaptive.di.qualifiers.MainScheduler
import org.stepik.android.adaptive.ui.adapter.RatingAdapter
import org.stepik.android.adaptive.gamification.ExpManager
import org.stepik.android.adaptive.util.RatingNamesGenerator
import retrofit2.HttpException
import javax.inject.Inject

class RatingPresenter
@Inject
constructor(
        private val api: API,
        private val sharedPreferenceMgr: SharedPreferenceMgr,
        @BackgroundScheduler
        private val backgroundScheduler: Scheduler,
        @MainScheduler
        private val mainScheduler: Scheduler,
        private val ratingNamesGenerator: RatingNamesGenerator
): PresenterBase<RatingView>() {
    companion object {
        private const val ITEMS_PER_PAGE = 10

        @JvmStatic
        private val RATING_PERIODS = arrayOf(1, 7, 0)
    }

    private val adapters = RATING_PERIODS.map { RatingAdapter(sharedPreferenceMgr.profileId) }

    private val compositeDisposable = CompositeDisposable()
    private val retrySubject = PublishSubject.create<Int>()

    private var error: Throwable? = null

    private var ratingPeriod = 0

    private var periodsLoaded = 0

    private val dataBaseMgr = DataBaseMgr.instance // to inject

    init {
        compositeDisposable.add(
                ExpManager.syncRating(dataBaseMgr, api)
                        .subscribeOn(backgroundScheduler)
                        .observeOn(mainScheduler)
                        .doOnError(this::onError)
                        .doOnComplete { initRatingPeriods() }
                        .retryWhen { x -> x.zipWith<Int, Throwable>(retrySubject.toFlowable(BackpressureStrategy.BUFFER), BiFunction { a, _ -> a }) }
                        .subscribe())
    }

    private fun initRatingPeriods() {
        val first = BiFunction<Throwable, Int, Throwable> { a, _ -> a }
        RATING_PERIODS.forEachIndexed { pos, period ->
            compositeDisposable.add(api.getRating(ITEMS_PER_PAGE, period)
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .doOnError(this::onError)
                    .retryWhen { it.zipWith(retrySubject, first) }
                    .map { it.users }
                    .subscribe({
                        adapters[pos].set(prepareRatingItems(it))
                        periodsLoaded++
                        onLoadComplete()
                    }, this::onError))
        }
    }

    private fun prepareRatingItems(data: List<RatingItem>) =
            data.mapIndexed { index, (rank, _, exp, user) ->
                RatingItem(
                        if (rank == 0) index + 1 else rank,
                        ratingNamesGenerator.getName(user),
                        exp,
                        user
                )
            }


    fun changeRatingPeriod(pos: Int) {
        this.ratingPeriod = pos
        view?.onRatingAdapter(adapters[pos])
    }

    override fun attachView(view: RatingView) {
        super.attachView(view)

        view.onRatingAdapter(adapters[ratingPeriod])
        view.onLoading()

        if (error != null) {
            error?.let { onError(it) }
        } else {
            onLoadComplete()
        }
    }

    private fun onLoadComplete() {
        if (periodsLoaded == RATING_PERIODS.size) {
            view?.onComplete()
        }
    }

    private fun onError(throwable: Throwable) {
        error = throwable
        if (throwable is HttpException) {
            view?.onRequestError()
        } else {
            view?.onConnectivityError()
        }
    }

    fun retry() {
        error = null
        retrySubject.onNext(0)
    }

    override fun destroy() {
        compositeDisposable.dispose()
    }
}