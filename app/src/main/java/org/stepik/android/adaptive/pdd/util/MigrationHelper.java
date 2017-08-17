package org.stepik.android.adaptive.pdd.util;

import android.support.v4.util.Pair;

import org.stepik.android.adaptive.pdd.api.API;
import org.stepik.android.adaptive.pdd.data.db.DataBaseMgr;

import io.reactivex.Observable;

public final class MigrationHelper {
    public static Observable<?> migrate() {
         return Observable.fromCallable(DataBaseMgr.getInstance()::getExp)
                .zipWith(Observable.fromCallable(DataBaseMgr.getInstance()::getStreak), Pair::new)
                .switchMap(e -> API.getInstance().migrate(e.first, e.second).toObservable());
    }
}
