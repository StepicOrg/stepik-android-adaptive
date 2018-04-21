package org.stepik.android.adaptive;

import android.content.Context;
import android.os.Build;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.Random;

public class Util {
    public static boolean checkPlayServices(final Context context) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);
        return resultCode == ConnectionResult.SUCCESS;
    }

    public static int getRandomNumberBetween(final int lower, final int upper) {
        return (int) (Math.random() * (upper - lower)) + lower;
    }

    private static final String ALLOWED_SYMBOLS = "ABCDEFGHIJKLMNOPQRESTUVWXYZabcdefgghiklmnopqrstuvwxyz1234567890_";

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
