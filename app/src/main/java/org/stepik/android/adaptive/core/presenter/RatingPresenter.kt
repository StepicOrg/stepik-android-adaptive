package org.stepik.android.adaptive.core.presenter

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.stepik.android.adaptive.api.API
import org.stepik.android.adaptive.core.presenter.contracts.RatingView
import org.stepik.android.adaptive.data.model.RatingItem
import org.stepik.android.adaptive.ui.adapter.RatingAdapter
import org.stepik.android.adaptive.util.ExpUtil
import org.stepik.android.adaptive.util.RatingNamesGenerator
import retrofit2.HttpException

class RatingPresenter : PresenterBase<RatingView>() {
    companion object : PresenterFactory<RatingPresenter> {
        override fun create() = RatingPresenter()

        private const val ITEMS_PER_PAGE = 10

        @JvmStatic
        private val RATING_PERIODS = arrayOf(1, 7, 0)
    }

    private val adapters = RATING_PERIODS.map { RatingAdapter() }

    private val compositeDisposable = CompositeDisposable()
    private val retrySubject = PublishSubject.create<Int>()

    private var error: Throwable? = null

    private var ratingPeriod = 0

    private var periodsLoaded = 0

    init {
        compositeDisposable.add(
                ExpUtil.syncRating()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnError(this::onError)
                        .doOnComplete { initRatingPeriods() }
                        .retryWhen { x -> x.zipWith(retrySubject, BiFunction<Throwable, Int, Throwable> { a, _ -> a }) }
                        .subscribe())
    }

    private fun initRatingPeriods() {
        val first = BiFunction<Throwable, Int, Throwable> { a, _ -> a }
        RATING_PERIODS.forEachIndexed { pos, period ->
            compositeDisposable.add(API.getInstance().getRating(ITEMS_PER_PAGE, period)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
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
                        RatingNamesGenerator.getName(user),
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