package org.stepik.android.adaptive.core.presenter

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.stepik.android.adaptive.api.RecommendationsResponse
import org.stepik.android.adaptive.core.presenter.contracts.RecommendationsView
import org.stepik.android.adaptive.data.SharedPreferenceMgr
import org.stepik.android.adaptive.data.model.Card
import org.stepik.android.adaptive.data.model.RecommendationReaction
import org.stepik.android.adaptive.notifications.LocalReminder
import org.stepik.android.adaptive.ui.adapter.QuizCardsAdapter
import org.stepik.android.adaptive.ui.helper.CardHelper
import org.stepik.android.adaptive.ui.listener.AdaptiveReactionListener
import org.stepik.android.adaptive.ui.listener.AnswerListener
import org.stepik.android.adaptive.util.DailyRewardManager
import org.stepik.android.adaptive.util.ExpUtil
import org.stepik.android.adaptive.util.InventoryUtil
import org.stepik.android.adaptive.util.RateAppUtil
import retrofit2.HttpException
import java.util.*

class RecommendationsPresenter : PresenterBase<RecommendationsView>(), AnswerListener {
    companion object : PresenterFactory<RecommendationsPresenter> {
        override fun create() = RecommendationsPresenter()

        private const val MIN_STREAK_TO_OFFER_TO_BUY = 7
    }

    private val compositeDisposable = CompositeDisposable()
    private val retrySubject = PublishSubject.create<Any>()

    private val cards = ArrayDeque<Card>()
    private val adapter = QuizCardsAdapter(AdaptiveReactionListener(this::createReaction), this)

    private var cardDisposable: Disposable? = null

    private var error: Throwable? = null

    private var isCourseCompleted = false


    init {
        createReaction(0, RecommendationReaction.Reaction.INTERESTING)
        InventoryUtil.starterPack()
        LocalReminder.resolveDailyRemind()
    }

    private fun resolveDailyReward() {
        val progress = DailyRewardManager.giveRewardAndGetCurrentRewardDay()
        if (progress != DailyRewardManager.DISCARD)
            view?.showDailyRewardDialog(progress)
    }

    override fun attachView(view: RecommendationsView) {
        super.attachView(view)
        resolveDailyReward()
        updateExp()

        view.onLoading()

        if (isCourseCompleted) {
            view.onCourseCompleted()
        } else {
            resubscribe()
            error?.let(this::onError)
        }

        view.onAdapter(adapter)
    }

    private fun updateExp(exp: Long = ExpUtil.getExp(), streak: Long = 0, showLevelDialog: Boolean = false) {
        val level = ExpUtil.getCurrentLevel(exp)

        val prev = ExpUtil.getNextLevelExp(level - 1)
        val next = ExpUtil.getNextLevelExp(level)

        view?.updateExp(exp, prev, next, level)

        if (showLevelDialog && level != ExpUtil.getCurrentLevel(exp - streak)) {
            view?.showNewLevelDialog(level)
        }
    }

    fun restoreStreak(streak: Long) {
        ExpUtil.changeStreak(streak)
        view?.onStreakRestored()
    }

    override fun onCorrectAnswer(submissionId: Long) {
        view?.hideStreakRestoreDialog()
        val streak = ExpUtil.incStreak()

        view?.onStreak(streak)
        updateExp(ExpUtil.changeExp(streak, submissionId), streak, true)

        if (RateAppUtil.onEngagement()) {
            view?.showRateAppDialog()
        }
    }

    override fun onWrongAnswer() {
        view?.hideStreakRestoreDialog()
        val streak = ExpUtil.getStreak()

        if (streak > 1) {
            view?.onStreakLost()

            view?.let {
                if (InventoryUtil.hasTickets()) {
                    it.showStreakRestoreDialog(streak, withTooltip = !SharedPreferenceMgr.getInstance().isStreakRestoreTooltipWasShown)
                    SharedPreferenceMgr.getInstance().afterStreakRestoreTooltipWasShown()
                } else {
                    it.showStreakRestoreDialog(streak, withTooltip = streak > MIN_STREAK_TO_OFFER_TO_BUY && !SharedPreferenceMgr.getInstance().isPaidContentTooltipWasShown)
                    if (streak > MIN_STREAK_TO_OFFER_TO_BUY) {
                        SharedPreferenceMgr.getInstance().afterPaidContentTooltipWasShown()
                    }
                }
            }
        }

        ExpUtil.resetStreak()
    }


    private fun createReaction(lesson: Long, reaction: RecommendationReaction.Reaction) {
        if (adapter.isEmptyOrContainsOnlySwipedCard(lesson)) {
            view?.onLoading()
        }

        compositeDisposable.add(CardHelper.createReactionObservable(lesson, reaction, cards.size + adapter.getItemCount())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(this::onError)
                .retryWhen { it.zipWith(retrySubject, BiFunction<Any, Any, Any> {a, _ -> a}) }
                .subscribe(this::onRecommendation, this::onError))
    }

    private fun onRecommendation(response: RecommendationsResponse) {
        val recommendations = response.recommendations
        if (recommendations == null || recommendations.isEmpty()) {
            isCourseCompleted = true
            view?.onCourseCompleted()
        } else {
            val size = cards.size
            recommendations
                    .filter { !isCardExists(it.lesson) }
                    .forEach { cards.add(Card(it.lesson)) }

            if (size == 0) resubscribe()
        }
    }

    private fun onError(throwable: Throwable?) {
        this.error = throwable
        when(throwable) {
            is HttpException -> view?.onRequestError()
            else -> view?.onConnectivityError()
        }
    }

    fun retry() {
        this.error = null
        retrySubject.onNext(0)
        view?.onLoading()

        if (cards.isNotEmpty()) {
            cards.peek().init()
            resubscribe()
        }
    }

    private fun resubscribe() {
        if (cards.isNotEmpty()) {
            if (cardDisposable != null && cardDisposable?.isDisposed == false) {
                cardDisposable?.dispose()
            }

            cardDisposable = cards.peek()
                    .subscribe(this::onCardDataLoaded, this::onError)
        }
    }

    private fun onCardDataLoaded(card: Card) {
        adapter.add(card)
        view?.onCardLoaded()
        cards.poll()
        resubscribe()
    }

    private fun isCardExists(lessonId: Long) =
            cards.any { it.lessonId == lessonId } || adapter.isCardExists(lessonId)


    override fun detachView(view: RecommendationsView) {
        adapter.detach()
        cardDisposable?.dispose()
        super.detachView(view)
    }

    override fun destroy() {
        compositeDisposable.dispose()
        cards.forEach(Card::recycle)
        adapter.destroy()
    }
}
