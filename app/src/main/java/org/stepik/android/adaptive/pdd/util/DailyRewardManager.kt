package org.stepik.android.adaptive.pdd.util

import org.joda.time.DateTime
import org.joda.time.Days
import org.stepik.android.adaptive.pdd.data.SharedPreferenceMgr

object DailyRewardManager {
    @JvmStatic
    val DISCARD = -1L

    private val LAST_SESSION_KEY = "last_session"
    private val REWARD_PROGRESS_KEY = "reward_progress"
    private val TOTAL_REWARD_PROGRESS_KEY = "total_reward_progress"

    val rewards = listOf(
            listOf(InventoryUtil.Item.Ticket to 3),
            listOf(InventoryUtil.Item.Ticket to 3),
            listOf(InventoryUtil.Item.Ticket to 5),
            listOf(InventoryUtil.Item.Ticket to 5),
            listOf(InventoryUtil.Item.Ticket to 7),
            listOf(InventoryUtil.Item.Ticket to 7),
            listOf(InventoryUtil.Item.Ticket to 25)
    )

    fun getLastSessionTimestamp() =
            SharedPreferenceMgr.getInstance().getLong(LAST_SESSION_KEY)

    fun getRewardProgress() =
            SharedPreferenceMgr.getInstance().getLong(REWARD_PROGRESS_KEY)

    var totalRewardProgress: Long
        get() = SharedPreferenceMgr.getInstance().getLong(TOTAL_REWARD_PROGRESS_KEY)
        set(value) = SharedPreferenceMgr.getInstance().saveLong(TOTAL_REWARD_PROGRESS_KEY, value)

    fun syncRewardProgress() {
        totalRewardProgress = Math.max(totalRewardProgress, getRewardProgress())
    }

    fun getCurrentRewardDay() : Long {
        val lastSession = SharedPreferenceMgr.getInstance().getLong(LAST_SESSION_KEY)

        val lastSessionDay = DateTime(lastSession).withTimeAtStartOfDay()
        val now = DateTime.now().withTimeAtStartOfDay()

        val diff = Days.daysBetween(lastSessionDay, now).days

        var progress = if (diff == 1) {
            SharedPreferenceMgr.getInstance().changeLong(TOTAL_REWARD_PROGRESS_KEY, 1)
            SharedPreferenceMgr.getInstance().changeLong(REWARD_PROGRESS_KEY, 1)
        } else {
            SharedPreferenceMgr.getInstance().getLong(REWARD_PROGRESS_KEY)
        }

        if (diff > 1 || diff < 0 || progress >= rewards.size) {
            resetProgress()
            progress = 0

            if (diff > 1 || diff < 0) {
                totalRewardProgress = 0
            }
        }

        SharedPreferenceMgr.getInstance().saveLong(LAST_SESSION_KEY, now.millis)

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
            AchievementManager.onEvent(AchievementManager.Event.DAYS, totalRewardProgress + 1)
        }

        return day
    }

    private fun resetProgress() = SharedPreferenceMgr.getInstance().remove(REWARD_PROGRESS_KEY)

}