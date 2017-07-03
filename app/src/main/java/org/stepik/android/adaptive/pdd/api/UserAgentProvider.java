package org.stepik.android.adaptive.pdd.api;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public final class UserAgentProvider {
    private static String userAgent = "";

    public static void init(Context context) {
        try {
            final PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            final int apiLevel = android.os.Build.VERSION.SDK_INT;
            userAgent = "StepikDroid/" + packageInfo.versionName + " (Android " + apiLevel
                    + ") build/" + packageInfo.versionCode + " package/" + packageInfo.packageName;
        } catch (PackageManager.NameNotFoundException e) {}
    }

    public static String provideUserAgent() {
        return userAgent;
    }
}
