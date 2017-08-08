package org.stepik.android.adaptive.pdd.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import org.stepik.android.adaptive.pdd.Util
import org.stepik.android.adaptive.pdd.notifications.LocalReminder
import org.stepik.android.adaptive.pdd.notifications.RemindNotificationManager


class NotificationsReceiver : BroadcastReceiver() {
    companion object {
        val REQUEST_CODE = 564

        val NOTIFICATION_CANCELED = "notification canceled"
        val SHOW_NOTIFICATION = "show notification"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        Util.initMgr(context)

        intent?.let {
            if (it.action == NOTIFICATION_CANCELED) {
                LocalReminder.resolveDailyRemind()
            } else if (it.action == SHOW_NOTIFICATION) {
                it.extras?.get(LocalReminder.DAYS_MULTIPLIER_KEY)?.let { days ->
                    when (days) {
                        1 -> RemindNotificationManager.showEveryDayNotification()
                        3 -> RemindNotificationManager.show3DaysNotification()
                    }
                }
            }
        }

    }

}