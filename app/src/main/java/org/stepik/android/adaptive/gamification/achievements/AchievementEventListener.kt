package org.stepik.android.adaptive.gamification.achievements

interface AchievementEventListener {
    fun onEvent(event: AchievementManager.Event, value: Long, show: Boolean = true)
}