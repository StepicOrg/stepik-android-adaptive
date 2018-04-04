package org.stepik.android.adaptive.core.presenter.contracts

import org.stepik.android.adaptive.ui.adapter.QuizCardsAdapter

interface RecommendationsView {
    fun onAdapter(cardsAdapter: QuizCardsAdapter)

    fun onLoading()
    fun onConnectivityError()
    fun onRequestError()

    fun onCourseCompleted()
    fun onCardLoaded()

    fun updateExp(
            exp: Long,
            currentLevelExp: Long,
            nextLevelExp: Long,

            level: Long)
    fun onStreak(streak: Long)
    fun onStreakLost()
    fun onStreakRestored()

    fun showDailyRewardDialog(progress: Long)
    fun showNewLevelDialog(level: Long)
    fun showRateAppDialog()
    fun showGamificationDescriptionScreen()
    fun showStreakRestoreDialog(streak: Long, withTooltip: Boolean = false)
    fun hideStreakRestoreDialog()

    fun showQuestionsPacksTooltip()
}