package org.stepik.android.adaptive.pdd.core;


import android.content.Context;
import android.content.Intent;

import org.stepik.android.adaptive.pdd.data.SharedPreferenceMgr;
import org.stepik.android.adaptive.pdd.ui.activity.IntroActivity;
import org.stepik.android.adaptive.pdd.ui.activity.LaunchActivity;
import org.stepik.android.adaptive.pdd.ui.activity.LoginActivity;
import org.stepik.android.adaptive.pdd.ui.activity.RegisterActivity;
import org.stepik.android.adaptive.pdd.ui.activity.StudyActivity;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class ScreenManager {
    private static ScreenManager instance;

    public synchronized static ScreenManager getInstance() {
        return instance;
    }

    public synchronized static void init(final Context context) {
        if (instance == null) {
            instance = new ScreenManager(context);
        }
    }

    private final Context context;

    public ScreenManager(final Context context) {
        this.context = context;
    }

    public void showLaunchScreen() {
        Intent intent = new Intent(context, LaunchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void startStudy() {
        Observable.fromCallable(SharedPreferenceMgr.getInstance()::isNotFirstTime)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((notFirstTime) -> {
                    Intent intent = new Intent(context, notFirstTime ? StudyActivity.class : IntroActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(intent);
                });
    }
}
