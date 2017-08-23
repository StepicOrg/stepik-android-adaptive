package org.stepik.android.adaptive.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.app.TaskStackBuilder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.data.db.DataBaseMgr
import org.stepik.android.adaptive.receivers.NotificationsReceiver
import org.stepik.android.adaptive.ui.activity.SplashActivity


object RemindNotificationManager {
    private val notificationId = 2138

    private val MIN_DAILY_EXP = 10

    private lateinit var context: Context
    private lateinit var notificationManager: NotificationManagerCompat

    fun init(context: Context) {
        this.context = context
        this.notificationManager = NotificationManagerCompat.from(context)
    }


    fun showEveryDayNotification() {
        val title = context.getString(R.string.local_push_title)
        Observable.fromCallable(DataBaseMgr.instance::getExpForLast7Days)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
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

        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}