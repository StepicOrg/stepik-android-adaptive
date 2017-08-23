package org.stepik.android.adaptive.data;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.stepik.android.adaptive.data.model.Step;
import org.stepik.android.adaptive.data.model.Submission;

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
        firebaseAnalytics.logEvent(EVENT_SUCCESS_LOGIN, null);
    }

    public void onBoardingFinished() {
        firebaseAnalytics.logEvent(EVENT_ONBOARDING_FINISHED, null);
    }

    private void logEventWithLongParam(final String event, final String param, final long value) {
        final Bundle bundle = new Bundle();
        bundle.putLong(param, value);
        firebaseAnalytics.logEvent(event, bundle);
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
        firebaseAnalytics.logEvent(EVENT_SUBMISSION_WAS_MADE, null);
    }

    public void rate(int rating) {
        logEventWithLongParam(EVENT_APP_RATE, PARAM_RATING, rating);
    }

    public void rateCanceled() {
        firebaseAnalytics.logEvent(EVENT_APP_RATE_CANCELED, null);
    }

    public void ratePositiveLater() {
        firebaseAnalytics.logEvent(EVENT_APP_RATE_POSITIVE_LATER, null);
    }

    public void ratePositiveGooglePlay() {
        firebaseAnalytics.logEvent(EVENT_APP_RATE_POSITIVE_GOOGLE_PLAY, null);
    }

    public void rateNegativeLater() {
        firebaseAnalytics.logEvent(EVENT_APP_RATE_NEGATIVE_LATER, null);
    }

    public void rateNegativeEmail() {
        firebaseAnalytics.logEvent(EVENT_APP_RATE_NEGATIVE_EMAIL, null);
    }

    public void statsOpened() {
        firebaseAnalytics.logEvent(EVENT_STATS_OPENED, null);
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
            firebaseAnalytics.logEvent(event, null);
    }

    public void onStreakRestoreDialogShown() {
        firebaseAnalytics.logEvent(EVENT_STREAK_RESTORE_DIALOG_SHOWN, null);
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
        firebaseAnalytics.logEvent(EVENT_ON_RATING_ERROR, null);
    }
}