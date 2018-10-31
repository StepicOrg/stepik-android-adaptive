package org.stepik.android.adaptive.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.data.analytics.Analytics
import org.stepik.android.adaptive.notifications.LocalReminder
import org.stepik.android.adaptive.notifications.RemindNotificationManager
import javax.inject.Inject


class NotificationsReceiver : BroadcastReceiver() {
    companion object {
        const val REQUEST_CODE = 564

        const val NOTIFICATION_CANCELED = "notification canceled"
        const val SHOW_NOTIFICATION = "show notification"
    }

    @Inject
    lateinit var remindNotificationManager: RemindNotificationManager

    @Inject
    lateinit var localReminder: LocalReminder

    @Inject
    lateinit var analytics: Analytics

    init {
        App.component().inject(this)
    }

    override fun onReceive(context: Context, intent: Intent?) {
        intent?.let {
            val days = it.getIntExtra(LocalReminder.DAYS_MULTIPLIER_KEY, 0)
            if (it.action == NOTIFICATION_CANCELED) {
                localReminder.resolveDailyRemind()
                analytics.onNotificationCanceled(days)
            } else if (it.action == SHOW_NOTIFICATION) {
                when (days) {
                    1 -> remindNotificationManager.showEveryDayNotification()
                    3 -> remindNotificationManager.show3DaysNotification()
                }
            }
        }
    }

}