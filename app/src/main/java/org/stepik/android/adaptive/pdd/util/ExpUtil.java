package org.stepik.android.adaptive.pdd.util;

import org.stepik.android.adaptive.pdd.data.AnalyticMgr;
import org.stepik.android.adaptive.pdd.data.SharedPreferenceMgr;

public class ExpUtil {
    private static String EXP_KEY = "exp_key";
    private static String STREAK_KEY = "streak_key";

    public static long getExp() {
        return SharedPreferenceMgr.getInstance().getLong(EXP_KEY);
    }

    public static long changeExp(long delta) {
        long exp = SharedPreferenceMgr.getInstance().changeLong(EXP_KEY, delta);
        AnalyticMgr.getInstance().onExpReached(exp - delta, delta);
        return exp;
    }

    public static long incStreak() {
        return changeStreak(1);
    }

    public static long changeStreak(long delta) {
        final long streak = SharedPreferenceMgr.getInstance().changeLong(STREAK_KEY, delta);
        AnalyticMgr.getInstance().onStreak(streak);
        return streak;
    }

    public static long getStreak() {
        return SharedPreferenceMgr.getInstance().getLong(STREAK_KEY);
    }

    public static long getCurrentLevel(long exp) {
        if (exp < 5) return 1;

        return 2 + (long) (Math.log(exp / 5) / Math.log(2));
    }

    public static long getNextLevelExp(long currentLevel) {
        if (currentLevel == 1) return 5;

        return 5 * (long) Math.pow(2, currentLevel - 1);
    }

    public static void resetStreak() {
        AnalyticMgr.getInstance().onStreakLost(getStreak());
        SharedPreferenceMgr.getInstance().saveLong(STREAK_KEY, 0);
    }

    public static void reset() {
        SharedPreferenceMgr.getInstance().remove(EXP_KEY);
        SharedPreferenceMgr.getInstance().remove(STREAK_KEY);
    }
}