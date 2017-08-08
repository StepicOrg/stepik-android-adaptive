package org.stepik.android.adaptive.pdd.util

import org.joda.time.DateTime
import org.joda.time.Days
import org.stepik.android.adaptive.pdd.data.SharedPreferenceMgr

object DailyRewardManager {
    @JvmStatic
    val DISCARD = -1L

    private val LAST_SESSION_KEY = "last_session"
    private val REWARD_PROGRESS_KEY = "reward_progress"

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

    fun getCurrentRewardDay() : Long {
        val lastSession = SharedPreferenceMgr.getInstance().getLong(LAST_SESSION_KEY)

        val lastSessionDay = DateTime(lastSession).withTimeAtStartOfDay()
        val now = DateTime.now().withTimeAtStartOfDay()

        val diff = Days.daysBetween(lastSessionDay, now).days

        var progress = if (diff == 1) {
            SharedPreferenceMgr.getInstance().changeLong(REWARD_PROGRESS_KEY, 1)
        } else {
            SharedPreferenceMgr.getInstance().getLong(REWARD_PROGRESS_KEY)
        }

        if (diff > 1 || diff < 0 || progress >= rewards.size) {
            resetProgress()
            progress = 0
        }

        SharedPreferenceMgr.getInstance().saveLong(LAST_SESSION_KEY, now.millis)

        return if (diff < 1)
            DISCARD
        else
            progress
    }

    fun giveRewardAndGetCurrentRewardDay() : Long {
        val day = getCurrentRewardDay()
        if (day != DISCARD)
            rewards[day.toInt()].forEach {
                InventoryUtil.changeItemCount(it.first, it.second.toLong())
            }

        return day
    }

    private fun resetProgress() = SharedPreferenceMgr.getInstance().remove(REWARD_PROGRESS_KEY)

}