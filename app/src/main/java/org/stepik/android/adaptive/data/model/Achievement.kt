package org.stepik.android.adaptive.data.model

import android.support.annotation.DrawableRes
import org.stepik.android.adaptive.data.SharedPreferenceMgr
import org.stepik.android.adaptive.gamification.achievements.AchievementManager


data class Achievement(
        val title: String,
        val description: String,

        val path: String,
        val eventType: AchievementManager.Event,

        val targetValue: Long,

        @DrawableRes val icon: Int,

        val showProgress: Boolean = true,

        private val sharedPreferenceMgr: SharedPreferenceMgr
) {
    var currentValue: Long
        get() = sharedPreferenceMgr.getLong(path)
        set(value) = sharedPreferenceMgr.saveLong(path, value)

    fun isComplete() = currentValue >= targetValue
}