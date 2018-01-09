package org.stepik.android.adaptive.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.Hours
import org.stepik.android.adaptive.data.SharedPreferenceMgr
import org.stepik.android.adaptive.receivers.NotificationsReceiver
import org.stepik.android.adaptive.util.DailyRewardManager

object LocalReminder {
    private const val NOTIFICATION_TIMESTAMP_KEY = "notification_timestamp"

    const val DAYS_MULTIPLIER_KEY = "days_multiplier"

    private const val GOOD_STUDY_HOUR = 20

    private lateinit var context: Context
    private lateinit var alarmManager: AlarmManager

    fun init(context: Context) {
        LocalReminder.context = context
        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    fun resolveDailyRemind() {
        val notificationTimestamp = SharedPreferenceMgr.getInstance().getLong(NOTIFICATION_TIMESTAMP_KEY)
        val now = DateTime.now()

        val lastSession = DateTime(DailyRewardManager.getLastSessionTimestamp())

        val daysSinceLastSession = Days.daysBetween(lastSession.withTimeAtStartOfDay(), now.withTimeAtStartOfDay()).days
        val dayMultiplier = if (daysSinceLastSession > 2) {
            3 // if user wasn't present for more than 2 days remind him in 3 days
        } else {
            1
        }

        val newNotificationTimestamp = if (notificationTimestamp < now.millis || daysSinceLastSession < 1) {
            val next = now.plusDays(dayMultiplier)
            if (isGoodTime(next.hourOfDay)) {
                next.millis
            } else {
                val desiredDayWithGoodHour = next.withHourOfDay(GOOD_STUDY_HOUR)
                if (Hours.hoursBetween(now, desiredDayWithGoodHour).hours > dayMultiplier * 24) {
                    desiredDayWithGoodHour.minusDays(1).millis
                } else {
                    desiredDayWithGoodHour.millis
                }
            }
        } else {
            notificationTimestamp
        }


        val intent = Intent(context, NotificationsReceiver::class.java)
        intent.putExtra(DAYS_MULTIPLIER_KEY, dayMultiplier)
        intent.action = NotificationsReceiver.SHOW_NOTIFICATION

        val pendingIntent = PendingIntent.getBroadcast(context, NotificationsReceiver.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.cancel(pendingIntent)

        SharedPreferenceMgr.getInstance().saveLong(NOTIFICATION_TIMESTAMP_KEY, newNotificationTimestamp)
        scheduleCompat(newNotificationTimestamp, AlarmManager.INTERVAL_HALF_HOUR, pendingIntent)
    }

    @JvmStatic
    fun isGoodTime(hour: Int) = hour in 7..23

    private fun scheduleCompat(scheduleMillis: Long, interval: Long, pendingIntent: PendingIntent) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setWindow(AlarmManager.RTC_WAKEUP, scheduleMillis, interval, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, scheduleMillis + interval / 2, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, scheduleMillis + interval / 2, pendingIntent)
        }
    }
}