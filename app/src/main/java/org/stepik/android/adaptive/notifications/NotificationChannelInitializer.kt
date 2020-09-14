package org.stepik.android.adaptive.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import org.stepik.android.adaptive.R

object NotificationChannelInitializer {
    fun initNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            //channels were introduced only in O. Before we had used in-app channels
            return
        }

        val channels = listOf(initChannel(context, StepikNotificationChannel.Learning))

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannels(channels)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initChannel(context: Context,
                            stepikChannel: StepikNotificationChannel): NotificationChannel {
        val channelName = context.getString(stepikChannel.visibleChannelNameRes)
        val channel = NotificationChannel(stepikChannel.channelId, channelName, stepikChannel.importance)
        channel.description = context.getString(stepikChannel.visibleChannelDescriptionRes)
        channel.enableLights(true)
        channel.enableVibration(false)
        return channel
    }

    private fun getImportanceCompat(): Int =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                NotificationManager.IMPORTANCE_HIGH
            } else {
                //it is -1 because channel should not be used for previous versions
                -1
            }

    private const val CHANNEL_LEARNING_ID = "learnChannel"

    enum class StepikNotificationChannel(
            val channelId: String,
            @StringRes
            val visibleChannelNameRes: Int,
            @StringRes
            val visibleChannelDescriptionRes: Int,
            val importance: Int = getImportanceCompat()) {
        //order is important!

        Learning(CHANNEL_LEARNING_ID, R.string.notification_channel_learning_name, R.string.notification_channel_learning_description),
    }
}