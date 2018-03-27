package org.stepik.android.adaptive.gamification

import org.joda.time.DateTime
import org.joda.time.Days
import org.stepik.android.adaptive.data.SharedPreferenceMgr
import org.stepik.android.adaptive.di.AppSingleton
import org.stepik.android.adaptive.gamification.achievements.AchievementEventPoster
import org.stepik.android.adaptive.gamification.achievements.AchievementManager
import org.stepik.android.adaptive.util.InventoryUtil
import javax.inject.Inject

@AppSingleton
class DailyRewardManager
@Inject
constructor(
        private val achievementEventPoster: AchievementEventPoster
) {
    companion object {
        const val DISCARD = -1L

        val rewards = listOf(
                listOf(InventoryUtil.Item.Ticket to 3),
                listOf(InventoryUtil.Item.Ticket to 3),
                listOf(InventoryUtil.Item.Ticket to 5),
                listOf(InventoryUtil.Item.Ticket to 5),
                listOf(InventoryUtil.Item.Ticket to 7),
                listOf(InventoryUtil.Item.Ticket to 7),
                listOf(InventoryUtil.Item.Ticket to 25)
        )

        private const val LAST_SESSION_KEY = "last_session"
        private const val REWARD_PROGRESS_KEY = "reward_progress"
        private const val TOTAL_REWARD_PROGRESS_KEY = "total_reward_progress"
    }

    private val sharedPreferenceMgr = SharedPreferenceMgr.getInstance() // to inject

    fun getLastSessionTimestamp() =
            sharedPreferenceMgr.getLong(LAST_SESSION_KEY)

    private fun getRewardProgress() =
            sharedPreferenceMgr.getLong(REWARD_PROGRESS_KEY)

    var totalRewardProgress: Long
        get() = sharedPreferenceMgr.getLong(TOTAL_REWARD_PROGRESS_KEY)
        set(value) = sharedPreferenceMgr.saveLong(TOTAL_REWARD_PROGRESS_KEY, value)

    fun syncRewardProgress() {
        totalRewardProgress = Math.max(totalRewardProgress, getRewardProgress())
    }

    private fun getCurrentRewardDay() : Long {
        val lastSession = sharedPreferenceMgr.getLong(LAST_SESSION_KEY)

        val lastSessionDay = DateTime(lastSession).withTimeAtStartOfDay()
        val now = DateTime.now().withTimeAtStartOfDay()

        val diff = Days.daysBetween(lastSessionDay, now).days

        var progress = if (diff == 1) {
            sharedPreferenceMgr.changeLong(TOTAL_REWARD_PROGRESS_KEY, 1)
            sharedPreferenceMgr.changeLong(REWARD_PROGRESS_KEY, 1)
        } else {
            sharedPreferenceMgr.getLong(REWARD_PROGRESS_KEY)
        }

        val isDayStreakBroken = diff > 1 || diff < 0
        if (isDayStreakBroken || progress >= rewards.size) {
            resetProgress()
            progress = 0

            if (isDayStreakBroken) {
                totalRewardProgress = 0
            }
        }

        sharedPreferenceMgr.saveLong(LAST_SESSION_KEY, now.millis)

        return if (diff < 1)
            DISCARD
        else
            progress
    }

    fun giveRewardAndGetCurrentRewardDay() : Long {
        val day = getCurrentRewardDay()
        if (day != DISCARD) {
            rewards[day.toInt()].forEach {
                InventoryUtil.changeItemCount(it.first, it.second.toLong())
            }
            achievementEventPoster.onEvent(AchievementManager.Event.DAYS, totalRewardProgress + 1)
        }

        return day
    }

    private fun resetProgress() = sharedPreferenceMgr.remove(REWARD_PROGRESS_KEY)

}