package org.stepik.droid.adaptive.pdd;

import android.app.Application;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Util.initMgr(getApplicationContext());
    }
}
