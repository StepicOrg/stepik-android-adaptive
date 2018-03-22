package org.stepik.android.adaptive.data;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.yandex.metrica.YandexMetrica;

import org.stepik.android.adaptive.data.model.Step;
import org.stepik.android.adaptive.data.model.Submission;

import java.util.HashMap;
import java.util.Map;

public final class AnalyticMgr {
    private final static String EVENT_SUCCESS_LOGIN = "success_login";
    private final static String EVENT_ONBOARDING_FINISHED = "onboarding_finished";

    private final static String EVENT_REACTION_HARD = "reaction_hard";
    private final static String EVENT_REACTION_EASY = "reaction_easy";

    private final static String EVENT_REACTION_HARD_AFTER_CORRECT = "reaction_hard_after_correct_answer";
    private final static String EVENT_REACTION_EASY_AFTER_CORRECT = "reaction_easy_after_correct_answer";

    private final static String EVENT_SUBMISSION_WAS_MADE = "submission_was_made";

    private final static String EVENT_CORRECT_ANSWER = "correct_answer";
    private final static String EVENT_WRONG_ANSWER = "wrong_answer";

    private final static String PARAM_LESSON = "lesson";

    private final static String EVENT_APP_RATE = "app_rate";
    private final static String EVENT_APP_RATE_CANCELED = "app_rate_canceled";
    private final static String PARAM_RATING = "rating";

    private final static String EVENT_APP_RATE_POSITIVE_LATER = "app_rate_positive_later";
    private final static String EVENT_APP_RATE_POSITIVE_GOOGLE_PLAY = "app_rate_positive_google_play";

    private final static String EVENT_APP_RATE_NEGATIVE_LATER = "app_rate_negative_later";
    private final static String EVENT_APP_RATE_NEGATIVE_EMAIL = "app_rate_negative_email";

    private final static String EVENT_STATS_OPENED = "stats_opened";
    private final static String EVENT_PAID_CONTENT_OPENED = "paid_content_opened";

    private final static String EVENT_REACHED_EXP_500 = "reached_exp_500";
    private final static String EVENT_REACHED_EXP_1000 = "reached_exp_1000";
    private final static String EVENT_REACHED_EXP_5000 = "reached_exp_5000";

    private final static String EVENT_STREAK_RESTORE_DIALOG_SHOWN = "streak_restore_dialog_shown";
    private final static String EVENT_STREAK_RESTORED = "streak_restored";
    private final static String EVENT_STREAK_RESTORE_CANCELED = "streak_restore_canceled";
    private final static String EVENT_STREAK_LOST = "streak_lost";
    private final static String EVENT_STREAK = "streak";

    private final static String PARAM_STREAK = "streak";

    private final static String EVENT_NOTIFICATION_CANCELED = "notification_canceled";
    private final static String PARAM_NOTIFICATION_DAYS = "days";

    private final static String EVENT_ON_RATING_ERROR = "rating_sync_error";

    private final static String EVENT_ON_QUESTIONS_PACKS_SCREEN_OPENED = "questions_packs_opened";

    public final static String EVENT_ON_QUESTIONS_DIALOG_SHOWN = "questions_dialog_shown";
    public final static String EVENT_ON_QUESTIONS_DIALOG_ACTION_CLICKED = "questions_dialog_action_clicked";

    public final static String EVENT_ON_QUESTIONS_PACK_SWITCHED = "questions_pack_switched";
    public final static String EVENT_ON_QUESTIONS_PACK_PURCHASE_BUTTON_CLICKED = "questions_pack_purchase_clicked";
    public final static String PARAM_COURSE = "course";

    public final static String EVENT_ON_BOOKMARK_CLICKED = "bookmark_clicked";
    public final static String EVENT_ON_BOOKMARK_ADDED = "bookmark_added";
    public final static String EVENT_ON_BOOKMARK_REMOVED = "bookmark_removed";

    private static AnalyticMgr instance;

    private final FirebaseAnalytics firebaseAnalytics;
    private AnalyticMgr(final Context context) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    public synchronized static void init(final Context context) {
        if (instance == null) {
            instance = new AnalyticMgr(context);
        }
    }

    public synchronized static AnalyticMgr getInstance() {
        return instance;
    }

    public void successLogin() {
        logEvent(EVENT_SUCCESS_LOGIN);
    }

    public void onBoardingFinished() {
        logEvent(EVENT_ONBOARDING_FINISHED);
    }

    public void logEvent(@NonNull final String name, @Nullable Bundle bundle) {
        firebaseAnalytics.logEvent(name, bundle);
        if (bundle == null) {
            YandexMetrica.reportEvent(name);
        } else {
            Map<String, Object> map = new HashMap<>();
            for (String key : bundle.keySet()) {
                map.put(key, bundle.get(key));
            }
            YandexMetrica.reportEvent(name, map);
        }
    }

    public void logEvent(@NonNull final String name) {
        logEvent(name, null);
    }

    public void logEventWithLongParam(final String event, final String param, final long value) {
        final Bundle bundle = new Bundle();
        bundle.putLong(param, value);
        logEvent(event, bundle);
    }

    public void reactionHard(final long lesson) {
        logEventWithLongParam(EVENT_REACTION_HARD, PARAM_LESSON, lesson);
    }

    public void reactionEasy(final long lesson) {
        logEventWithLongParam(EVENT_REACTION_EASY, PARAM_LESSON, lesson);
    }

    public void reactionHardAfterCorrect(final long lesson) {
        logEventWithLongParam(EVENT_REACTION_HARD_AFTER_CORRECT, PARAM_LESSON, lesson);
    }

    public void reactionEasyAfterCorrect(final long lesson) {
        logEventWithLongParam(EVENT_REACTION_EASY_AFTER_CORRECT, PARAM_LESSON, lesson);
    }

    public void answerResult(final Step step, @NonNull final Submission submission) {
        final long lesson = step != null ? step.getLesson() : 0;
        switch (submission.getStatus()) {
            case CORRECT:
                logEventWithLongParam(EVENT_CORRECT_ANSWER, PARAM_LESSON, lesson);
            break;
            case WRONG:
                logEventWithLongParam(EVENT_WRONG_ANSWER, PARAM_LESSON, lesson);
            break;
        }
    }

    public void onSubmissionWasMade() {
        logEvent(EVENT_SUBMISSION_WAS_MADE);
    }

    public void rate(int rating) {
        logEventWithLongParam(EVENT_APP_RATE, PARAM_RATING, rating);
    }

    public void rateCanceled() {
        logEvent(EVENT_APP_RATE_CANCELED);
    }

    public void ratePositiveLater() {
        logEvent(EVENT_APP_RATE_POSITIVE_LATER);
    }

    public void ratePositiveGooglePlay() {
        logEvent(EVENT_APP_RATE_POSITIVE_GOOGLE_PLAY);
    }

    public void rateNegativeLater() {
        logEvent(EVENT_APP_RATE_NEGATIVE_LATER);
    }

    public void rateNegativeEmail() {
        logEvent(EVENT_APP_RATE_NEGATIVE_EMAIL);
    }

    public void statsOpened() {
        logEvent(EVENT_STATS_OPENED);
    }

    public void paidContentOpened() {
        logEvent(EVENT_PAID_CONTENT_OPENED);
    }


    public void onExpReached(final long exp, final long delta) {
        String event = null;
        if (exp <= 500 && exp + delta >= 500)
            event = EVENT_REACHED_EXP_500;
        else if (exp <= 1000 && exp + delta >= 1000)
            event = EVENT_REACHED_EXP_1000;
        else if (exp <= 5000 && exp + delta >= 5000)
            event = EVENT_REACHED_EXP_5000;
        if (event != null)
            logEvent(event);
    }

    public void onStreakRestoreDialogShown() {
        logEvent(EVENT_STREAK_RESTORE_DIALOG_SHOWN);
    }

    public void onStreakRestored(long streak) {
        logEventWithLongParam(EVENT_STREAK_RESTORED, PARAM_STREAK, streak);
    }

    public void onStreakRestoreCanceled(long streak) {
        logEventWithLongParam(EVENT_STREAK_RESTORE_CANCELED, PARAM_STREAK, streak);
    }

    public void onStreakLost(long streak) {
        logEventWithLongParam(EVENT_STREAK_LOST, PARAM_STREAK, streak);
    }

    public void onStreak(long streak) {
        logEventWithLongParam(EVENT_STREAK, PARAM_STREAK, streak);
    }

    public void onNotificationCanceled(int days) {
        logEventWithLongParam(EVENT_STREAK, PARAM_NOTIFICATION_DAYS, days);
    }

    public void onRatingError() {
        logEvent(EVENT_ON_RATING_ERROR);
    }

    public void onQuestionsPacksOpened() {
        logEvent(EVENT_ON_QUESTIONS_PACKS_SCREEN_OPENED);
    }
}
