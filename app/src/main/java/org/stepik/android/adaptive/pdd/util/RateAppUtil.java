package org.stepik.android.adaptive.pdd.util;

import org.stepik.android.adaptive.pdd.data.SharedPreferenceMgr;

public class RateAppUtil {
    private static long NOTIFY_DELAY_LATER    = 1000 * 60 * 60 * 24 * 2;
    private static long NOTIFY_DELAY_NEGATIVE = 1000 * 60 * 60 * 24 * 14;

    private static int REQUIRED_ENGAGEMENT = 10;

    private static String KEY_RATED = "rate_app_is_rated";
    private static String KEY_NOTIFY_ALLOWED = "rate_app_notify_allowed";
    private static String KEY_ENGAGEMENT_COUNT = "rate_app_engagement_count";

    /**
     * Registers engagement and notifies when to show rate dialog
     * @return true if you should show app rate dialog
     */
    public static boolean onEngagement() {
        final boolean isRated = SharedPreferenceMgr.getInstance().getBoolean(KEY_RATED);
        final long notifyAllowed = SharedPreferenceMgr.getInstance().getLong(KEY_NOTIFY_ALLOWED);

        if (!isRated && notifyAllowed == 0 && notifyAllowed < System.currentTimeMillis()) {
            long engagements = SharedPreferenceMgr.getInstance().getLong(KEY_ENGAGEMENT_COUNT);
            if (engagements + 1 == REQUIRED_ENGAGEMENT) {
                return true;
            } else {
                SharedPreferenceMgr.getInstance().saveLong(KEY_ENGAGEMENT_COUNT, engagements + 1);
            }
        }
        return false;
    }

    public static void onRated() {
        SharedPreferenceMgr.getInstance().saveBoolean(KEY_RATED, true);
    }

    public static void onCloseLater() {
        SharedPreferenceMgr.getInstance().saveLong(KEY_ENGAGEMENT_COUNT, 0);
        SharedPreferenceMgr.getInstance().saveLong(KEY_NOTIFY_ALLOWED, SharedPreferenceMgr.getInstance().getLong(KEY_NOTIFY_ALLOWED) + NOTIFY_DELAY_LATER);
    }

    public static void onCloseNegative() {
        SharedPreferenceMgr.getInstance().saveLong(KEY_ENGAGEMENT_COUNT, 0);
        SharedPreferenceMgr.getInstance().saveLong(KEY_NOTIFY_ALLOWED, SharedPreferenceMgr.getInstance().getLong(KEY_NOTIFY_ALLOWED) + NOTIFY_DELAY_NEGATIVE);
    }

    public static void reset() {
        SharedPreferenceMgr.getInstance().remove(KEY_RATED);
        SharedPreferenceMgr.getInstance().remove(KEY_NOTIFY_ALLOWED);
        SharedPreferenceMgr.getInstance().remove(KEY_ENGAGEMENT_COUNT);
    }
}
