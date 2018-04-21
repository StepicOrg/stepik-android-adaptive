package org.stepik.android.adaptive.data

import android.os.Bundle
import org.stepik.android.adaptive.data.model.Step
import org.stepik.android.adaptive.data.model.Submission

interface Analytics {
    fun successLogin()
    fun onBoardingFinished()

    fun logEvent(name: String, bundle: Bundle? = null)

    fun logEventWithName(eventName: String, name: String?)
    fun logEventWithLongParam(event: String, param: String, value: Long)

    fun reactionHard(lesson: Long)
    fun reactionEasy(lesson: Long)
    fun reactionHardAfterCorrect(lesson: Long)
    fun reactionEasyAfterCorrect(lesson: Long)
    fun answerResult(step: Step?, submission: Submission)
    fun onSubmissionWasMade()
    fun rate(rating: Int)
    fun rateCanceled()
    fun ratePositiveLater()
    fun ratePositiveGooglePlay()
    fun rateNegativeLater()
    fun rateNegativeEmail()
    fun statsOpened()
    fun paidContentOpened()
    fun onExpReached(exp: Long, delta: Long)
    fun onStreakRestoreDialogShown()
    fun onStreakRestored(streak: Long)
    fun onStreakRestoreCanceled(streak: Long)
    fun onStreakLost(streak: Long)
    fun onStreak(streak: Long)
    fun onNotificationCanceled(days: Int)
    fun onRatingError()
    fun onQuestionsPacksOpened()

    companion object {
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