package org.stepik.android.adaptive.pdd.core.presenter.contracts

import org.stepik.android.adaptive.pdd.data.model.Achievement

interface AchievementView {
    fun showAchievement(achievement: Achievement)
}