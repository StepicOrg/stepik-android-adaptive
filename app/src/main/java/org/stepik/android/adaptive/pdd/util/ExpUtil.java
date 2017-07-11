package org.stepik.android.adaptive.pdd.util;

import org.stepik.android.adaptive.pdd.data.SharedPreferenceMgr;

public class ExpUtil {
    private static String EXP_KEY = "exp_key";

    public static long getExp() {
        return SharedPreferenceMgr.getInstance().getLong(EXP_KEY);
    }

    public static long addExp(long delta) {
        long current = SharedPreferenceMgr.getInstance().getLong(EXP_KEY);
        SharedPreferenceMgr.getInstance().saveLong(EXP_KEY, current + delta);
        return current + delta;
    }

    public static long incExp() {
        return addExp(1);
    }

    public static long getCurrentLevel(long exp) {
        if (exp < 1) return 0;
        if (exp < 2) return 1;
        if (exp < 5) return 2;

        return 3 + (long) (Math.log(exp / 5) / Math.log(2));
    }

    public static long getNextLevelExp(long currentLevel) {
        if (currentLevel == 0) return 1;
        if (currentLevel == 1) return 2;
        if (currentLevel == 2) return 5;

        return 5 * (long) Math.pow(2, currentLevel - 2);
    }

}