package org.stepik.android.adaptive.pdd.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.stepik.android.adaptive.pdd.Util
import org.stepik.android.adaptive.pdd.notifications.LocalReminder
import org.stepik.android.adaptive.pdd.notifications.RemindNotificationManager


class NotificationAlarmReceiver : BroadcastReceiver() {
    companion object {
        val REQUEST_CODE = 564
    }

    override fun onReceive(context: Context, intent: Intent?) {
        Util.initMgr(context)
        intent?.extras?.get(LocalReminder.DAYS_MULTIPLIER_KEY)?.let {
            when (it) {
                1 -> RemindNotificationManager.showEveryDayNotification()
                3 -> RemindNotificationManager.show3DaysNotification()
            }
        }
    }
}