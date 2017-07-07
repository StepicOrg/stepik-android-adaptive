package org.stepik.android.adaptive.pdd.core;


import android.content.Context;
import android.content.Intent;

import org.stepik.android.adaptive.pdd.ui.activity.IntroActivity;
import org.stepik.android.adaptive.pdd.ui.activity.PhotoViewActivity;
import org.stepik.android.adaptive.pdd.ui.activity.StudyActivity;

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
        Intent intent = new Intent(context, IntroActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void startStudy() {
        Intent intent = new Intent(context,  StudyActivity.class); // notFirstTime ? StudyActivity.class :
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
//        Observable.fromCallable(SharedPreferenceMgr.getInstance()::isNotFirstTime)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe((notFirstTime) -> {
//
//                });
    }

    public static void showImage(Context context, String path) {
        Intent intent = new Intent(context, PhotoViewActivity.class);
        intent.putExtra(PhotoViewActivity.Companion.getPathKey(), path);
        context.startActivity(intent);
    }
}
