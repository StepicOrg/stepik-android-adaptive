package org.stepik.android.adaptive.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.stepik.android.adaptive.Util
import org.stepik.android.adaptive.notifications.LocalReminder

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Util.initMgr(context)
        LocalReminder.resolveDailyRemind()
    }
}