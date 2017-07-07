package org.stepik.android.adaptive.pdd;

import android.support.multidex.MultiDexApplication;

public class App extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        Util.initMgr(getApplicationContext());
    }
}
