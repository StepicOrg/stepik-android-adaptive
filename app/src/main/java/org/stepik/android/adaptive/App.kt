package org.stepik.android.adaptive

import android.support.multidex.MultiDexApplication

class App : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        Util.initMgr(applicationContext)
    }
}
