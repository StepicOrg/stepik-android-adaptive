package org.stepik.android.adaptive.util;

import org.stepik.android.adaptive.api.API;
import org.stepik.android.adaptive.data.AnalyticMgr;
import org.stepik.android.adaptive.data.SharedPreferenceMgr;
import org.stepik.android.adaptive.data.db.DataBaseMgr;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class ExpUtil {
    private static String EXP_KEY = "exp_key";
    private static String STREAK_KEY = "streak_key";

    private static CompositeDisposable compositeDisposable = new CompositeDisposable();

    public static long getExp() {
        return SharedPreferenceMgr.getInstance().getLong(EXP_KEY);
    }

    public static long changeExp(long delta, long submissionId) {
        long exp = SharedPreferenceMgr.getInstance().changeLong(EXP_KEY, delta);
        AnalyticMgr.getInstance().onExpReached(exp - delta, delta);

        AchievementManager.INSTANCE.onEvent(AchievementManager.Event.EXP, exp, true);


        compositeDisposable.add(
                Completable
                        .fromRunnable(() -> DataBaseMgr.getInstance().onExpGained(delta, submissionId))
                        .andThen(syncRating())
                        .subscribeOn(Schedulers.io())
                        .subscribe((__) -> {}, (e) -> {
                            if (e instanceof HttpException) {
                                AnalyticMgr.getInstance().onRatingError();
                            }
                        }));

        return exp;
    }

    public static Observable<?> syncRating() {
        return Observable.fromCallable(DataBaseMgr.getInstance()::getExp)
                .switchMap((e) -> API.getInstance().putRating(e).toObservable());
    }

    public static long incStreak() {
        return changeStreak(1);
    }

    public static long changeStreak(long delta) {
        final long streak = SharedPreferenceMgr.getInstance().changeLong(STREAK_KEY, delta);
        AnalyticMgr.getInstance().onStreak(streak);
        AchievementManager.INSTANCE.onEvent(AchievementManager.Event.STREAK, streak, true);
        return streak;
    }

    public static long getStreak() {
        return SharedPreferenceMgr.getInstance().getLong(STREAK_KEY);
    }

    public static long getCurrentLevel(long exp) {
        if (exp < 5) return 1;

        final long level = 2 + (long) (Math.log(exp / 5) / Math.log(2));

        AchievementManager.INSTANCE.onEvent(AchievementManager.Event.LEVEL, level, true);

        return level;
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