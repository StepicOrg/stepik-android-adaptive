package org.stepik.android.adaptive.pdd.notifications

import org.joda.time.DateTime
import org.stepik.android.adaptive.pdd.util.DailyRewardManager


object RemindNotificationManager {

    private val lastSession by lazy { DateTime(DailyRewardManager.getLastSessionTimestamp()) }

    fun showEveryDayNotification() {

    }

    fun show3DaysNotification() {

    }
}