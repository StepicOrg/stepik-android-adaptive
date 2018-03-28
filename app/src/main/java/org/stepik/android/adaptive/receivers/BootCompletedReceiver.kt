package org.stepik.android.adaptive.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.notifications.LocalReminder
import javax.inject.Inject

class BootCompletedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var localReminder: LocalReminder

    init {
        App.component().inject(this)
    }

    override fun onReceive(context: Context, intent: Intent?) {
        localReminder.resolveDailyRemind()
    }
}