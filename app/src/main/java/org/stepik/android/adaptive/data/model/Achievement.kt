package org.stepik.android.adaptive.data.model

import android.support.annotation.DrawableRes
import org.stepik.android.adaptive.data.SharedPreferenceMgr
import org.stepik.android.adaptive.util.AchievementManager


data class Achievement(
        val title: String,
        val description: String,

        val path: String,
        val eventType: AchievementManager.Event,

        val targetValue: Long,

        @DrawableRes val icon: Int,

        val showProgress: Boolean = true
) {
    var currentValue: Long
        get() = SharedPreferenceMgr.getInstance().getLong(path)
        set(value) = SharedPreferenceMgr.getInstance().saveLong(path, value)

    fun isComplete() = currentValue >= targetValue
}