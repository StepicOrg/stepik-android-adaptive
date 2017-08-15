package org.stepik.android.adaptive.pdd.core.presenter

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.stepik.android.adaptive.pdd.api.API
import org.stepik.android.adaptive.pdd.core.presenter.contracts.RatingView
import org.stepik.android.adaptive.pdd.data.model.RatingItem
import org.stepik.android.adaptive.pdd.ui.adapter.RatingAdapter
import org.stepik.android.adaptive.pdd.util.RatingNamesGenerator

class RatingPresenter : PresenterBase<RatingView>() {
    companion object : PresenterFactory<RatingPresenter> {
        override fun create() = RatingPresenter()

        @JvmStatic
        private val ITEMS_PER_PAGE = 50

        @JvmStatic
        private val RATING_PERIODS = arrayOf(0, 7)
    }

    private val adapters = RATING_PERIODS.map { RatingAdapter() }

    private val compositeDisposable = CompositeDisposable()
    private val retrySubject = PublishSubject.create<Int>()

    private var isError = false

    private var ratingPeriod = 0

    private var periodsLoaded = 0

    init {
        RATING_PERIODS.forEachIndexed { pos, period ->
            compositeDisposable.add(API.getInstance().getRating(ITEMS_PER_PAGE, period)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError { onError() }
                    .retryWhen { x -> x.zipWith(retrySubject, BiFunction<Throwable, Int, Throwable> { a, _ -> a }) }
                    .subscribe({
                        adapters[pos].add(prepareRatingItems(it))
                        periodsLoaded++
                        onLoadComplete()
                    }, { onError() }))
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

        if (isError) {
            view.onError()
        } else {
            onLoadComplete()
        }
    }

    private fun onLoadComplete() {
        if (periodsLoaded == RATING_PERIODS.size) {
            view?.onComplete()
        }
    }

    private fun onError() {
        isError = true
        view?.onError()
    }

    fun retry() {
        isError = false
        retrySubject.onNext(0)
    }

    override fun destroy() {
        compositeDisposable.dispose()
    }
}