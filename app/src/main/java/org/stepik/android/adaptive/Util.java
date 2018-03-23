package org.stepik.android.adaptive;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.facebook.FacebookSdk;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.vk.sdk.VKSdk;

import org.stepik.android.adaptive.api.API;
import org.stepik.android.adaptive.api.UserAgentProvider;
import org.stepik.android.adaptive.configuration.Config;
import org.stepik.android.adaptive.core.ScreenManager;
import org.stepik.android.adaptive.data.AnalyticMgr;
import org.stepik.android.adaptive.data.SharedPreferenceMgr;
import org.stepik.android.adaptive.data.db.DataBaseMgr;
import org.stepik.android.adaptive.notifications.LocalReminder;
import org.stepik.android.adaptive.notifications.RemindNotificationManager;
import org.stepik.android.adaptive.util.AchievementManager;
import org.stepik.android.adaptive.util.RatingNamesGenerator;

import java.util.Random;

public class Util {
    public static void initMgr(final Context context) {
        Config.init(context);
        ScreenManager.init(context);
        API.init();
        SharedPreferenceMgr.init(context);
        VKSdk.initialize(context);
        FacebookSdk.sdkInitialize(context.getApplicationContext());
        AnalyticMgr.init(context);
        UserAgentProvider.init(context);
        DataBaseMgr.Companion.init(context);
        LocalReminder.INSTANCE.init(context);
        RatingNamesGenerator.init(context);
        AchievementManager.INSTANCE.init(context);
    }

    public static void hideSoftKeyboard(final Activity a) {
        final View view = a.getCurrentFocus();
        if (view != null) {
            final InputMethodManager mgr = (InputMethodManager) a.getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static boolean checkPlayServices(final Context context) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);
        return resultCode == ConnectionResult.SUCCESS;
    }

    public static int getRandomNumberBetween(final int lower, final int upper) {
        return (int) (Math.random() * (upper - lower)) + lower;
    }

    private static String ALLOWED_SYMBOLS = "ABCDEFGHIJKLMNOPQRESTUVWXYZabcdefgghiklmnopqrstuvwxyz1234567890_";

    public static String randomString(final int length) {
        Random random = new Random();
        StringBuilder builder = new StringBuilder(length);

        for (int i = 0; i < length; ++i) {
            builder.append(ALLOWED_SYMBOLS.charAt(random.nextInt(ALLOWED_SYMBOLS.length())));
        }

        return builder.toString();
    }

    public static boolean isLowAndroidVersion() {
        return android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;
    }
}
