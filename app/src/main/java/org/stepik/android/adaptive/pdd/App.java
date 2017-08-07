package org.stepik.android.adaptive.pdd;

import android.support.multidex.MultiDexApplication;

import org.stepik.android.adaptive.pdd.notifications.LocalReminder;

public class App extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        Util.initMgr(getApplicationContext());

        LocalReminder.INSTANCE.resolveDailyRemind();
    }
}
