package org.stepik.android.adaptive.core.presenter

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import org.stepik.android.adaptive.api.Api
import org.stepik.android.adaptive.api.RecommendationsResponse
import org.stepik.android.adaptive.content.questions.QuestionsPacksManager
import org.stepik.android.adaptive.core.presenter.contracts.RecommendationsView
import org.stepik.android.adaptive.data.preference.SharedPreferenceHelper
import org.stepik.android.adaptive.data.model.Card
import org.stepik.android.adaptive.data.model.RecommendationReaction
import org.stepik.android.adaptive.di.qualifiers.BackgroundScheduler
import org.stepik.android.adaptive.di.qualifiers.MainScheduler
import org.stepik.android.adaptive.notifications.LocalReminder
import org.stepik.android.adaptive.ui.adapter.QuizCardsAdapter
import org.stepik.android.adaptive.ui.helper.CardHelper
import org.stepik.android.adaptive.ui.listener.AdaptiveReactionListener
import org.stepik.android.adaptive.ui.listener.AnswerListener
import org.stepik.android.adaptive.gamification.DailyRewardManager
import org.stepik.android.adaptive.gamification.ExpManager
import org.stepik.android.adaptive.gamification.InventoryManager
import org.stepik.android.adaptive.util.RateAppManager
import org.stepik.android.adaptive.util.addDisposable
import retrofit2.HttpException
import java.util.*
import javax.inject.Inject

class RecommendationsPresenter
@Inject
constructor(
        private val api: Api,
        private val sharedPreferenceHelper: SharedPreferenceHelper,
        @BackgroundScheduler
        private val backgroundScheduler: Scheduler,
        @MainScheduler
        private val mainScheduler: Scheduler,
        localReminder: LocalReminder,
        private val dailyRewardManager: DailyRewardManager,
        private val expManager: ExpManager,
        private val inventoryManager: InventoryManager,
        private val rateAppManager: RateAppManager,
        questionsPacksManager: QuestionsPacksManager
): PresenterBase<RecommendationsView>(), AnswerListener {
    companion object {
        private const val MIN_STREAK_TO_OFFER_TO_BUY = 7
        private const val LEVEL_TO_SHOW_GAMIFICATION_DESCRIPTION = 2
        private const val LEVEL_TO_SHOW_EMPTY_AUTH_SCREEN = 6
        private const val LEVEL_TOO_HIGH_TO_WAIT = 10
        private const val MIN_EXP_TO_OFFER_PACKS = 50
    }

    private val compositeDisposable = CompositeDisposable()
    private val retrySubject = PublishSubject.create<Any>()

    private val cards = ArrayDeque<Card>()
    private val adapter = QuizCardsAdapter(AdaptiveReactionListener(this::createReaction), this)

    private var cardDisposable: Disposable? = null

    private var error: Throwable? = null

    private var isCourseCompleted = false

    private val isQuestionsPacksSupported = questionsPacksManager.isQuestionsPacksSupported

    private var exp = 0L

    init {
        createReaction(0, RecommendationReaction.Reaction.INTERESTING)
        inventoryManager.starterPack()
        localReminder.resolveDailyRemind()

        fetchExp()
    }

    private fun fetchExp() {
        compositeDisposable addDisposable expManager.fetchExp()
                .observeOn(mainScheduler)
                .subscribeOn(backgroundScheduler)
                .subscribe {
                    exp = it
                    updateExp()
                }
    }

    private fun resolveDailyReward() {
        val progress = dailyRewardManager.giveRewardAndGetCurrentRewardDay()
        if (progress != DailyRewardManager.DISCARD)
            view?.showDailyRewardDialog(progress)
    }

    override fun attachView(view: RecommendationsView) {
        super.attachView(view)
        resolveDailyReward()

        fetchExp()
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

    private fun updateExp(streak: Long = 0, showLevelDialog: Boolean = false) {
        val level = expManager.getCurrentLevel(exp)

        val prev = expManager.getNextLevelExp(level - 1)
        val next = expManager.getNextLevelExp(level)

        view?.updateExp(exp, prev, next, level)

        if (showLevelDialog) {
            val isNewLevelGained = level != expManager.getCurrentLevel(exp - streak)

            val shouldShowGamificationDescription = isQuestionsPacksSupported &&
                    (isNewLevelGained && level >= LEVEL_TO_SHOW_GAMIFICATION_DESCRIPTION || level >= LEVEL_TOO_HIGH_TO_WAIT)
                    && !sharedPreferenceHelper.isGamificationDescriptionWasShown

            val shouldShowEmptyAuthScreen = (isNewLevelGained && level >= LEVEL_TO_SHOW_EMPTY_AUTH_SCREEN)
                    && !sharedPreferenceHelper.isEmptyAuthScreenWasShown

            when {
                shouldShowGamificationDescription -> {
                    sharedPreferenceHelper.isGamificationDescriptionWasShown = true
                    view?.showGamificationDescriptionScreen()
                }
                shouldShowEmptyAuthScreen -> compositeDisposable addDisposable sharedPreferenceHelper.isFakeUser()
                        .subscribeOn(backgroundScheduler)
                        .observeOn(mainScheduler)
                        .subscribe { isFake ->
                            sharedPreferenceHelper.isEmptyAuthScreenWasShown = true
                            if (isFake) {
                                view?.showEmptyAuthScreen()
                            }
                        }
                isNewLevelGained -> view?.showNewLevelDialog(level)
            }
        }

        if (exp > MIN_EXP_TO_OFFER_PACKS) {
            if (!sharedPreferenceHelper.isQuestionsPacksTooltipWasShown) {
                sharedPreferenceHelper.afterQuestionsPacksTooltipWasShown()
                view?.showQuestionsPacksTooltip()
            }
        }
    }

    fun restoreStreak(streak: Long) {
        expManager.changeStreak(streak)
        view?.onStreakRestored()
    }

    override fun onCorrectAnswer(submissionId: Long) {
        view?.hideStreakRestoreDialog()
        val streak = expManager.incStreak()

        view?.onStreak(streak)
        exp = expManager.changeExp(streak, submissionId)
        updateExp(streak, true)

        if (rateAppManager.onEngagement()) {
            view?.showRateAppDialog()
        }
    }

    override fun onWrongAnswer() {
        view?.hideStreakRestoreDialog()
        val streak = expManager.streak

        if (streak > 1) {
            view?.onStreakLost()

            view?.let {
                if (inventoryManager.hasTickets()) {
                    it.showStreakRestoreDialog(streak, withTooltip = !sharedPreferenceHelper.isStreakRestoreTooltipWasShown)
                    sharedPreferenceHelper.afterStreakRestoreTooltipWasShown()
                } else {
                    it.showStreakRestoreDialog(streak, withTooltip = streak > MIN_STREAK_TO_OFFER_TO_BUY && !sharedPreferenceHelper.isPaidContentTooltipWasShown)
                    if (streak > MIN_STREAK_TO_OFFER_TO_BUY) {
                        sharedPreferenceHelper.afterPaidContentTooltipWasShown()
                    }
                }
            }
        }

        expManager.resetStreak()
    }


    private fun createReaction(lesson: Long, reaction: RecommendationReaction.Reaction) {
        if (adapter.isEmptyOrContainsOnlySwipedCard(lesson)) {
            view?.onLoading()
        }

        compositeDisposable addDisposable CardHelper.createReactionObservable(api, sharedPreferenceHelper, lesson, reaction, cards.size + adapter.getItemCount())
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .doOnError(this::onError)
                .retryWhen { it.zipWith(retrySubject, BiFunction<Any, Any, Any> {a, _ -> a}) }
                .subscribe(this::onRecommendation, this::onError)
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
