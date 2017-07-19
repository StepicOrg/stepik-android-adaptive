package org.stepik.android.adaptive.pdd.util;

import org.stepik.android.adaptive.pdd.data.SharedPreferenceMgr;

public class ExpUtil {
    private static String EXP_KEY = "exp_key";
    private static String STREAK_KEY = "streak_key";

    public static long getExp() {
        return SharedPreferenceMgr.getInstance().getLong(EXP_KEY);
    }

    public static long addExp(long delta) {
        return addValue(EXP_KEY, delta);
    }

    public static long incStreak() {
        return addValue(STREAK_KEY, 1);
    }

    private static long addValue(final String key, final long delta) {
        long current = SharedPreferenceMgr.getInstance().getLong(key);
        SharedPreferenceMgr.getInstance().saveLong(key, current + delta);
        return current + delta;
    }

    public static long getCurrentLevel(long exp) {
        if (exp < 1) return 1;
        if (exp < 2) return 2;
        if (exp < 5) return 3;

        return 4 + (long) (Math.log(exp / 5) / Math.log(2));
    }

    public static long getNextLevelExp(long currentLevel) {
        if (currentLevel == 1) return 1;
        if (currentLevel == 2) return 2;
        if (currentLevel == 3) return 5;

        return 5 * (long) Math.pow(2, currentLevel - 3);
    }

    public static void resetStreak() {
        SharedPreferenceMgr.getInstance().saveLong(STREAK_KEY, 0);
    }

    public static void reset() {
        SharedPreferenceMgr.getInstance().remove(EXP_KEY);
        SharedPreferenceMgr.getInstance().remove(STREAK_KEY);
    }
}