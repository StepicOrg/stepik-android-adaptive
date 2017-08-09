package org.stepik.android.adaptive.pdd.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.stepik.android.adaptive.pdd.Util
import org.stepik.android.adaptive.pdd.notifications.LocalReminder

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Util.initMgr(context)
        LocalReminder.resolveDailyRemind()
    }
}