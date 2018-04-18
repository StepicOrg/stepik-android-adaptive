package org.stepik.android.adaptive.data

import android.content.Context
import android.os.Bundle

import com.google.firebase.analytics.FirebaseAnalytics
import com.yandex.metrica.YandexMetrica

import org.stepik.android.adaptive.data.model.Step
import org.stepik.android.adaptive.data.model.Submission
import org.stepik.android.adaptive.di.AppSingleton

import java.util.HashMap
import javax.inject.Inject

@AppSingleton
class Analytics
@Inject
constructor(context: Context) {
    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)

    fun successLogin() {
        logEvent(EVENT_SUCCESS_LOGIN)
    }

    fun onBoardingFinished() {
        logEvent(EVENT_ONBOARDING_FINISHED)
    }

    @JvmOverloads
    fun logEvent(name: String, bundle: Bundle? = null) {
        firebaseAnalytics.logEvent(name, bundle)
        if (bundle == null) {
            YandexMetrica.reportEvent(name)
        } else {
            val map = HashMap<String, Any>()
            for (key in bundle.keySet()) {
                map[key] = bundle.get(key)
            }
            YandexMetrica.reportEvent(name, map)
        }
    }

    fun logEventWithName(eventName: String, name: String?) {
        val bundle = Bundle()
        if (name != null) {
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name)
        }
        logEvent(eventName, bundle)
    }

    fun logEventWithLongParam(event: String, param: String, value: Long) {
        val bundle = Bundle()
        bundle.putLong(param, value)
        logEvent(event, bundle)
    }

    fun reactionHard(lesson: Long) {
        logEventWithLongParam(EVENT_REACTION_HARD, PARAM_LESSON, lesson)
    }

    fun reactionEasy(lesson: Long) {
        logEventWithLongParam(EVENT_REACTION_EASY, PARAM_LESSON, lesson)
    }

    fun reactionHardAfterCorrect(lesson: Long) {
        logEventWithLongParam(EVENT_REACTION_HARD_AFTER_CORRECT, PARAM_LESSON, lesson)
    }

    fun reactionEasyAfterCorrect(lesson: Long) {
        logEventWithLongParam(EVENT_REACTION_EASY_AFTER_CORRECT, PARAM_LESSON, lesson)
    }

    fun answerResult(step: Step?, submission: Submission) {
        val lesson = step?.lesson ?: 0
        when (submission.status) {
            Submission.Status.CORRECT -> logEventWithLongParam(EVENT_CORRECT_ANSWER, PARAM_LESSON, lesson)
            Submission.Status.WRONG -> logEventWithLongParam(EVENT_WRONG_ANSWER, PARAM_LESSON, lesson)
        }
    }

    fun onSubmissionWasMade() {
        logEvent(EVENT_SUBMISSION_WAS_MADE)
    }

    fun rate(rating: Int) {
        logEventWithLongParam(EVENT_APP_RATE, PARAM_RATING, rating.toLong())
    }

    fun rateCanceled() {
        logEvent(EVENT_APP_RATE_CANCELED)
    }

    fun ratePositiveLater() {
        logEvent(EVENT_APP_RATE_POSITIVE_LATER)
    }

    fun ratePositiveGooglePlay() {
        logEvent(EVENT_APP_RATE_POSITIVE_GOOGLE_PLAY)
    }

    fun rateNegativeLater() {
        logEvent(EVENT_APP_RATE_NEGATIVE_LATER)
    }

    fun rateNegativeEmail() {
        logEvent(EVENT_APP_RATE_NEGATIVE_EMAIL)
    }

    fun statsOpened() {
        logEvent(EVENT_STATS_OPENED)
    }

    fun paidContentOpened() {
        logEvent(EVENT_PAID_CONTENT_OPENED)
    }


    fun onExpReached(exp: Long, delta: Long) {
        var event: String? = null
        if (exp <= 500 && exp + delta >= 500)
            event = EVENT_REACHED_EXP_500
        else if (exp <= 1000 && exp + delta >= 1000)
            event = EVENT_REACHED_EXP_1000
        else if (exp <= 5000 && exp + delta >= 5000)
            event = EVENT_REACHED_EXP_5000
        if (event != null)
            logEvent(event)
    }

    fun onStreakRestoreDialogShown() {
        logEvent(EVENT_STREAK_RESTORE_DIALOG_SHOWN)
    }

    fun onStreakRestored(streak: Long) {
        logEventWithLongParam(EVENT_STREAK_RESTORED, PARAM_STREAK, streak)
    }

    fun onStreakRestoreCanceled(streak: Long) {
        logEventWithLongParam(EVENT_STREAK_RESTORE_CANCELED, PARAM_STREAK, streak)
    }

    fun onStreakLost(streak: Long) {
        logEventWithLongParam(EVENT_STREAK_LOST, PARAM_STREAK, streak)
    }

    fun onStreak(streak: Long) {
        logEventWithLongParam(EVENT_STREAK, PARAM_STREAK, streak)
    }

    fun onNotificationCanceled(days: Int) {
        logEventWithLongParam(EVENT_STREAK, PARAM_NOTIFICATION_DAYS, days.toLong())
    }

    fun onRatingError() {
        logEvent(EVENT_ON_RATING_ERROR)
    }

    fun onQuestionsPacksOpened() {
        logEvent(EVENT_ON_QUESTIONS_PACKS_SCREEN_OPENED)
    }

    companion object {
        private const val EVENT_SUCCESS_LOGIN = "success_login"
        private const val EVENT_ONBOARDING_FINISHED = "onboarding_finished"

        private const val EVENT_REACTION_HARD = "reaction_hard"
        private const val EVENT_REACTION_EASY = "reaction_easy"

        private const val EVENT_REACTION_HARD_AFTER_CORRECT = "reaction_hard_after_correct_answer"
        private const val EVENT_REACTION_EASY_AFTER_CORRECT = "reaction_easy_after_correct_answer"

        private const val EVENT_SUBMISSION_WAS_MADE = "submission_was_made"

        private const val EVENT_CORRECT_ANSWER = "correct_answer"
        private const val EVENT_WRONG_ANSWER = "wrong_answer"

        private const val PARAM_LESSON = "lesson"

        private const val EVENT_APP_RATE = "app_rate"
        private const val EVENT_APP_RATE_CANCELED = "app_rate_canceled"
        private const val PARAM_RATING = "rating"

        private const val EVENT_APP_RATE_POSITIVE_LATER = "app_rate_positive_later"
        private const val EVENT_APP_RATE_POSITIVE_GOOGLE_PLAY = "app_rate_positive_google_play"

        private const val EVENT_APP_RATE_NEGATIVE_LATER = "app_rate_negative_later"
        private const val EVENT_APP_RATE_NEGATIVE_EMAIL = "app_rate_negative_email"

        private const val EVENT_STATS_OPENED = "stats_opened"
        private const val EVENT_PAID_CONTENT_OPENED = "paid_content_opened"

        private const val EVENT_REACHED_EXP_500 = "reached_exp_500"
        private const val EVENT_REACHED_EXP_1000 = "reached_exp_1000"
        private const val EVENT_REACHED_EXP_5000 = "reached_exp_5000"

        private const val EVENT_STREAK_RESTORE_DIALOG_SHOWN = "streak_restore_dialog_shown"
        private const val EVENT_STREAK_RESTORED = "streak_restored"
        private const val EVENT_STREAK_RESTORE_CANCELED = "streak_restore_canceled"
        private const val EVENT_STREAK_LOST = "streak_lost"
        private const val EVENT_STREAK = "streak"

        private const val PARAM_STREAK = "streak"

        private const val EVENT_NOTIFICATION_CANCELED = "notification_canceled"
        private const val PARAM_NOTIFICATION_DAYS = "days"

        private const val EVENT_ON_RATING_ERROR = "rating_sync_error"

        private const val EVENT_ON_QUESTIONS_PACKS_SCREEN_OPENED = "questions_packs_opened"

        const val EVENT_ON_QUESTIONS_DIALOG_SHOWN = "questions_dialog_shown"
        const val EVENT_ON_QUESTIONS_DIALOG_ACTION_CLICKED = "questions_dialog_action_clicked"

        const val EVENT_ON_QUESTIONS_PACK_SWITCHED = "questions_pack_switched"
        const val EVENT_ON_QUESTIONS_PACK_PURCHASE_BUTTON_CLICKED = "questions_pack_purchase_clicked"
        const val PARAM_COURSE = "course"

        const val EVENT_ON_BOOKMARK_CLICKED = "bookmark_clicked"
        const val EVENT_ON_BOOKMARK_ADDED = "bookmark_added"
        const val EVENT_ON_BOOKMARK_REMOVED = "bookmark_removed"

        const val EVENT_ON_GAMIFICATION_DESCRIPTION_SHOWN = "gamification_description_shown"
    }

    object Profile {
        const val ON_CHANGE_NAME = "profile_change_name"
        const val ON_CHANGE_EMAIL = "profile_change_email"
        const val ON_CHANGE_PASS = "profile_change_pass"

        const val ON_NAME_CHANGED = "profile_name_changed"
        const val ON_EMAIL_CHANGED = "profile_email_changed"
        const val ON_PASS_CHANGED = "profile_pass_changed"
    }

    object Registration {
        const val SUCCESS_REGISTER = "success register"
        const val ERROR = "registration_error"
        const val SHOW_REGISTRATION_SCREEN = "show_registration_screen"
        const val SHOW_REGISTRATION_SCREEN_FROM_PROFILE = "show_registration_screen_from_profile"
    }

    object Login {
        const val FAIL_LOGIN = "fail_login"
        const val SUCCESS_LOGIN = "success login"
        const val SUCCESS_LOGIN_WITH_PASSWORD = "success login with password"
        const val SHOW_LOGIN_SCREEN = "show_login_screen"
        const val SHOW_LOGIN_SCREEN_FROM_PROFILE = "show_login_screen_from_profile"
        const val AUTH_SKIPPED = "auth_skipped"
        const val SHOW_EMPTY_AUTH_SCREEN = "show_empty_auth_screen"
    }
}
