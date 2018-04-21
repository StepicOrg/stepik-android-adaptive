package org.stepik.android.adaptive.data.model

import android.support.annotation.DrawableRes
import org.stepik.android.adaptive.data.preference.SharedPreferenceHelper
import org.stepik.android.adaptive.gamification.achievements.AchievementManager


data class Achievement(
        val title: String,
        val description: String,

        val path: String,
        val eventType: AchievementManager.Event,

        val targetValue: Long,

        @DrawableRes val icon: Int,

        val showProgress: Boolean = true,

        private val sharedPreferenceHelper: SharedPreferenceHelper
) {
    var currentValue: Long
        get() = sharedPreferenceHelper.getLong(path)
        set(value) = sharedPreferenceHelper.saveLong(path, value)

    fun isComplete() = currentValue >= targetValue
}