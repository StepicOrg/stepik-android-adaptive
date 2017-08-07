package org.stepik.android.adaptive.pdd.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import org.stepik.android.adaptive.pdd.Util


class NotificationAlarmReceiver : BroadcastReceiver() {
    companion object {
        val REQUEST_CODE = 564
    }

    override fun onReceive(context: Context, intent: Intent?) {
        Util.initMgr(context)
        Log.d(javaClass.canonicalName, "onReceive($intent)")
    }
}