package org.stepik.android.adaptive.gamification

import org.joda.time.DateTime
import org.joda.time.Days
import org.stepik.android.adaptive.data.SharedPreferenceHelper
import org.stepik.android.adaptive.di.AppSingleton
import org.stepik.android.adaptive.gamification.achievements.AchievementEventPoster
import org.stepik.android.adaptive.gamification.achievements.AchievementManager
import javax.inject.Inject

@AppSingleton
class DailyRewardManager
@Inject
constructor(
        private val achievementEventPoster: AchievementEventPoster,
        private val sharedPreferenceHelper: SharedPreferenceHelper,
        private val inventoryManager: InventoryManager
) {
    companion object {
        const val DISCARD = -1L

        val rewards = listOf(
                listOf(InventoryManager.Item.Ticket to 3),
                listOf(InventoryManager.Item.Ticket to 3),
                listOf(InventoryManager.Item.Ticket to 5),
                listOf(InventoryManager.Item.Ticket to 5),
                listOf(InventoryManager.Item.Ticket to 7),
                listOf(InventoryManager.Item.Ticket to 7),
                listOf(InventoryManager.Item.Ticket to 25)
        )

        private const val LAST_SESSION_KEY = "last_session"
        private const val REWARD_PROGRESS_KEY = "reward_progress"
        private const val TOTAL_REWARD_PROGRESS_KEY = "total_reward_progress"
    }

    fun getLastSessionTimestamp() =
            sharedPreferenceHelper.getLong(LAST_SESSION_KEY)

    private fun getRewardProgress() =
            sharedPreferenceHelper.getLong(REWARD_PROGRESS_KEY)

    var totalRewardProgress: Long
        get() = sharedPreferenceHelper.getLong(TOTAL_REWARD_PROGRESS_KEY)
        set(value) = sharedPreferenceHelper.saveLong(TOTAL_REWARD_PROGRESS_KEY, value)

    fun syncRewardProgress() {
        totalRewardProgress = Math.max(totalRewardProgress, getRewardProgress())
    }

    private fun getCurrentRewardDay() : Long {
        val lastSession = sharedPreferenceHelper.getLong(LAST_SESSION_KEY)

        val lastSessionDay = DateTime(lastSession).withTimeAtStartOfDay()
        val now = DateTime.now().withTimeAtStartOfDay()

        val diff = Days.daysBetween(lastSessionDay, now).days

        var progress = if (diff == 1) {
            sharedPreferenceHelper.changeLong(TOTAL_REWARD_PROGRESS_KEY, 1)
            sharedPreferenceHelper.changeLong(REWARD_PROGRESS_KEY, 1)
        } else {
            sharedPreferenceHelper.getLong(REWARD_PROGRESS_KEY)
        }

        val isDayStreakBroken = diff > 1 || diff < 0
        if (isDayStreakBroken || progress >= rewards.size) {
            resetProgress()
            progress = 0

            if (isDayStreakBroken) {
                totalRewardProgress = 0
            }
        }

        sharedPreferenceHelper.saveLong(LAST_SESSION_KEY, now.millis)

        return if (diff < 1)
            DISCARD
        else
            progress
    }

    fun giveRewardAndGetCurrentRewardDay() : Long {
        val day = getCurrentRewardDay()
        if (day != DISCARD) {
            rewards[day.toInt()].forEach {
                inventoryManager.changeItemCount(it.first, it.second.toLong())
            }
            achievementEventPoster.onEvent(AchievementManager.Event.DAYS, totalRewardProgress + 1)
        }

        return day
    }

    private fun resetProgress() = sharedPreferenceHelper.remove(REWARD_PROGRESS_KEY)

}