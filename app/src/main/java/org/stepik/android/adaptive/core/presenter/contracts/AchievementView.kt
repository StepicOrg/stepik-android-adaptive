package org.stepik.android.adaptive.core.presenter.contracts

import org.stepik.android.adaptive.data.model.Achievement

interface AchievementView {
    fun showAchievement(achievement: Achievement)
    fun canShowAchievement(): Boolean
}
