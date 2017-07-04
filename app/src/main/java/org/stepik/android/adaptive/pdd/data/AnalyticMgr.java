package org.stepik.android.adaptive.pdd.data;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.stepik.android.adaptive.pdd.data.model.Step;
import org.stepik.android.adaptive.pdd.data.model.Submission;

public final class AnalyticMgr {
    private final static String EVENT_SUCCESS_LOGIN = "success_login";
    private final static String EVENT_ONBOARDING_FINISHED = "onboarding_finished";

    private final static String EVENT_REACTION_HARD = "reaction_hard";
    private final static String EVENT_REACTION_EASY = "reaction_easy";

    private final static String EVENT_REACTION_HARD_AFTER_CORRECT = "reaction_hard_after_correct_answer";
    private final static String EVENT_REACTION_EASY_AFTER_CORRECT = "reaction_easy_after_correct_answer";


    private final static String EVENT_CORRECT_ANSWER = "correct_answer";
    private final static String EVENT_WRONG_ANSWER = "wrong_answer";

    private final static String PARAM_LESSON = "lesson";

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

    private void logEventWithLesson(final String event, final long lesson) {
        final Bundle bundle = new Bundle();
        bundle.putLong(PARAM_LESSON, lesson);
        firebaseAnalytics.logEvent(event, bundle);
    }

    public void reactionHard(final long lesson) {
        logEventWithLesson(EVENT_REACTION_HARD, lesson);
    }

    public void reactionEasy(final long lesson) {
        logEventWithLesson(EVENT_REACTION_EASY, lesson);
    }

    public void reactionHardAfterCorrect(final long lesson) {
        logEventWithLesson(EVENT_REACTION_HARD_AFTER_CORRECT, lesson);
    }

    public void reactionEasyAfterCorrect(final long lesson) {
        logEventWithLesson(EVENT_REACTION_EASY_AFTER_CORRECT, lesson);
    }

    public void answerResult(final Step step, @NonNull final Submission submission) {
        final long lesson = step != null ? step.getLesson() : 0;
        switch (submission.getStatus()) {
            case CORRECT:
                logEventWithLesson(EVENT_CORRECT_ANSWER, lesson);
            break;
            case WRONG:
                logEventWithLesson(EVENT_WRONG_ANSWER, lesson);
            break;
        }
    }
}
