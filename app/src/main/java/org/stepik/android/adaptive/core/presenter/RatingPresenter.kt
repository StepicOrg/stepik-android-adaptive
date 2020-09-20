package org.stepik.android.adaptive.core.presenter

import io.reactivex.BackpressureStrategy
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.zipWith
import io.reactivex.subjects.PublishSubject
import org.stepik.android.adaptive.api.rating.RatingRepository
import org.stepik.android.adaptive.api.user.UserRepository
import org.stepik.android.adaptive.core.presenter.contracts.RatingView
import org.stepik.android.adaptive.data.db.DataBaseMgr
import org.stepik.android.adaptive.data.model.RatingItem
import org.stepik.android.adaptive.data.preference.ProfilePreferences
import org.stepik.android.adaptive.di.qualifiers.BackgroundScheduler
import org.stepik.android.adaptive.di.qualifiers.MainScheduler
import org.stepik.android.adaptive.gamification.ExpManager
import org.stepik.android.adaptive.ui.adapter.RatingAdapter
import org.stepik.android.adaptive.util.RatingNamesGenerator
import org.stepik.android.adaptive.util.addDisposable
import retrofit2.HttpException
import javax.inject.Inject

class RatingPresenter
@Inject
constructor(
    private val ratingRepository: RatingRepository,
    private val userRepository: UserRepository,
    private val profilePreferences: ProfilePreferences,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler,
    private val ratingNamesGenerator: RatingNamesGenerator,
    dataBaseMgr: DataBaseMgr
) : PresenterBase<RatingView>() {
    companion object {
        private const val ITEMS_PER_PAGE = 10

        @JvmStatic
        private val RATING_PERIODS = arrayOf(1, 7, 0)
    }

    private val adapters = RATING_PERIODS.map { RatingAdapter(profilePreferences.profileId) }

    private val compositeDisposable = CompositeDisposable()
    private val retrySubject = PublishSubject.create<Int>()

    private var error: Throwable? = null

    private var ratingPeriod = 0

    private var periodsLoaded = 0

    init {
        compositeDisposable addDisposable
            ExpManager.syncRating(dataBaseMgr, ratingRepository)
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .doOnError(this::onError)
                .doOnComplete { initRatingPeriods() }
                .retryWhen { x -> x.zipWith<Int, Throwable>(retrySubject.toFlowable(BackpressureStrategy.BUFFER), BiFunction { a, _ -> a }) }
                .subscribe()
    }

    private fun initRatingPeriods() {
        val first = BiFunction<Throwable, Int, Throwable> { a, _ -> a }
        RATING_PERIODS.forEachIndexed { pos, period ->
            compositeDisposable addDisposable resolveUsers(ratingRepository.getRatingTable(ITEMS_PER_PAGE, period))
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .doOnError(this::onError)
                .retryWhen { it.zipWith(retrySubject.toFlowable(BackpressureStrategy.BUFFER), first) }
                .subscribeBy(this::onError) {
                    adapters[pos].set(it)
                    periodsLoaded++
                    onLoadComplete()
                }
        }
    }

    private fun resolveUsers(single: Single<List<RatingItem>>): Single<List<RatingItem>> =
        single.flatMap {
            val userIds = it.filter { it.isNotFake }.map { it.user }.toLongArray()
            if (userIds.isEmpty()) {
                Single.just(emptyList())
            } else {
                userRepository.getUsers(userIds)
            }.zipWith(Single.just(it))
        }.map { (users, items) ->
            items.mapIndexed { index, item ->
                val user = users.find { it.id == item.user }
                val name = user?.fullName ?: ratingNamesGenerator.getName(item.user)

                item.copy(rank = if (item.rank == 0) index + 1 else item.rank, name = name)
            }
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
