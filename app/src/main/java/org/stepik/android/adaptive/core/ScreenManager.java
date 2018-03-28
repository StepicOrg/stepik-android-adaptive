package org.stepik.android.adaptive.core;


import android.content.Context;
import android.content.Intent;

import org.stepik.android.adaptive.data.Analytics;
import org.stepik.android.adaptive.ui.activity.IntroActivity;
import org.stepik.android.adaptive.ui.activity.PhotoViewActivity;
import org.stepik.android.adaptive.ui.activity.StatsActivity;
import org.stepik.android.adaptive.ui.activity.StudyActivity;
import org.stepik.android.adaptive.ui.activity.QuestionsPacksActivity;

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

    public void showOnboardingScreen() {
        Intent intent = new Intent(context, IntroActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void startStudy() {
        Intent intent = new Intent(context, StudyActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    public static void showImage(Context context, String path) {
        Intent intent = new Intent(context, PhotoViewActivity.class);
        intent.putExtra(PhotoViewActivity.PATH_KEY, path);
        context.startActivity(intent);
    }

    public static void showStatsScreen(Context context, Analytics analytics, int page) {
        analytics.statsOpened();
        final Intent intent = new Intent(context, StatsActivity.class);
        intent.putExtra(StatsActivity.PAGE_KEY, page);
        context.startActivity(intent);
    }

    public static void showQuestionsPacksScreen(Context context, Analytics analytics) {
        analytics.onQuestionsPacksOpened();
        context.startActivity(new Intent(context, QuestionsPacksActivity.class));
    }
}
