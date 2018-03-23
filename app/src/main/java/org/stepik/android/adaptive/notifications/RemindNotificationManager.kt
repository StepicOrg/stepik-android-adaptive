package org.stepik.android.adaptive.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.app.TaskStackBuilder
import io.reactivex.Scheduler
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.data.db.DataBaseMgr
import org.stepik.android.adaptive.di.qualifiers.BackgroundScheduler
import org.stepik.android.adaptive.di.qualifiers.MainScheduler
import org.stepik.android.adaptive.receivers.NotificationsReceiver
import org.stepik.android.adaptive.ui.activity.SplashActivity
import javax.inject.Inject


class RemindNotificationManager
@Inject
constructor(
        private val context: Context,
        @BackgroundScheduler
        private val backgroundScheduler: Scheduler,
        @MainScheduler
        private val mainScheduler: Scheduler
) {
    companion object {
        private const val NOTIFICATION_ID = 2138
        private const val MIN_DAILY_EXP = 10
    }

    private val notificationManager = NotificationManagerCompat.from(context)

    fun showEveryDayNotification() {
        val title = context.getString(R.string.local_push_title)
        DataBaseMgr.instance.getExpForLast7Days()
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribe({
                    val description = if (it[5] < MIN_DAILY_EXP) {
                        val length = it.slice(0..5).takeLastWhile { num -> num > 0 }.size
                        if (length > 0) {
                            context.getString(R.string.local_push_streak, context.resources.getQuantityString(R.plurals.day, length, length))
                        } else {
                            context.getString(R.string.local_push_3days)
                        }
                    } else {
                        context.getString(R.string.local_push_yesterday, it[5])
                    }
                    showNotification(title, description, 1)
                }, {})
    }

    fun show3DaysNotification() {
        showNotification(context.getString(R.string.local_push_title), context.getString(R.string.local_push_3days), 3)
    }

    private fun getDeleteIntent(days: Int) : PendingIntent {
        val intent = Intent(context, NotificationsReceiver::class.java)
        intent.action = NotificationsReceiver.NOTIFICATION_CANCELED
        intent.putExtra(LocalReminder.DAYS_MULTIPLIER_KEY, days)
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
    }

    private fun showNotification(title: String, description: String, days: Int) {
        val notificationBuilder = NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_small_notification)
                .setContentTitle(title)
                .setContentText(description)
                .setAutoCancel(true)

        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack(SplashActivity::class.java)

        val intent = Intent(context, SplashActivity::class.java)
        stackBuilder.addNextIntent(intent)

        val resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        notificationBuilder.setContentIntent(resultPendingIntent)

        notificationBuilder.setDeleteIntent(getDeleteIntent(days))

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }
}