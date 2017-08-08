package org.stepik.android.adaptive.pdd.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.app.TaskStackBuilder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.stepik.android.adaptive.pdd.R
import org.stepik.android.adaptive.pdd.data.db.DataBaseMgr
import org.stepik.android.adaptive.pdd.receivers.NotificationsReceiver
import org.stepik.android.adaptive.pdd.ui.activity.SplashActivity


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
                    showNotification(title, description)
                }, {})
    }

    fun show3DaysNotification() {
        showNotification(context.getString(R.string.local_push_title), context.getString(R.string.local_push_3days))
    }

    private fun getDeleteIntent() : PendingIntent {
        val intent = Intent(context, NotificationsReceiver::class.java)
        intent.action = NotificationsReceiver.NOTIFICATION_CANCELED
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
    }

    private fun showNotification(title: String, description: String) {
        val notificationBuilder = NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_car)
                .setContentTitle(title)
                .setContentText(description)
                .setAutoCancel(true)

        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack(SplashActivity::class.java)

        val intent = Intent(context, SplashActivity::class.java)
        stackBuilder.addNextIntent(intent)

        val resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        notificationBuilder.setContentIntent(resultPendingIntent)

        notificationBuilder.setDeleteIntent(getDeleteIntent())

        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}