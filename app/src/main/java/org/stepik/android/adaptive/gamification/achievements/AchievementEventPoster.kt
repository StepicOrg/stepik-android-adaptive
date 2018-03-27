package org.stepik.android.adaptive.gamification.achievements

import org.stepik.android.adaptive.core.events.ListenerContainer
import javax.inject.Inject

class AchievementEventPoster
@Inject
constructor(
        private val listeners: ListenerContainer<AchievementEventListener>
) {
    fun onEvent(event: AchievementManager.Event, value: Long, show: Boolean = true) {
        listeners.asIterable().forEach {
            it.onEvent(event, value, show)
        }
    }
}