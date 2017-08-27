package org.stepik.android.adaptive.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.stepik.android.adaptive.Util
import org.stepik.android.adaptive.data.AnalyticMgr
import org.stepik.android.adaptive.notifications.LocalReminder
import org.stepik.android.adaptive.notifications.RemindNotificationManager


class NotificationsReceiver : BroadcastReceiver() {
    companion object {
        val REQUEST_CODE = 564

        val NOTIFICATION_CANCELED = "notification canceled"
        val SHOW_NOTIFICATION = "show notification"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        Util.initMgr(context)

        intent?.let {
            val days = it.getIntExtra(LocalReminder.DAYS_MULTIPLIER_KEY, 0)
            if (it.action == NOTIFICATION_CANCELED) {
                LocalReminder.resolveDailyRemind()
                AnalyticMgr.getInstance().onNotificationCanceled(days)
            } else if (it.action == SHOW_NOTIFICATION) {
                when (days) {
                    1 -> RemindNotificationManager.showEveryDayNotification()
                    3 -> RemindNotificationManager.show3DaysNotification()
                }
            }
        }

    }

}