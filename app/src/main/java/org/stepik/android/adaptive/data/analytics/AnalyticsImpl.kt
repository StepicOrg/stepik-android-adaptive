package org.stepik.android.adaptive.data.analytics

import android.content.Context
import android.os.Bundle
import com.amplitude.api.Amplitude
import com.amplitude.api.Identify
import com.amplitude.api.Revenue

import com.google.firebase.analytics.FirebaseAnalytics
import com.yandex.metrica.YandexMetrica
import org.json.JSONObject
import org.solovyev.android.checkout.Sku
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.configuration.Config

import org.stepik.android.adaptive.data.model.Step
import org.stepik.android.adaptive.data.model.Submission
import org.stepik.android.adaptive.di.AppSingleton
import org.stepik.android.adaptive.resolvers.ContentPriceResolver

import java.util.HashMap
import javax.inject.Inject

@AppSingleton
class AnalyticsImpl
@Inject
constructor(
        context: Context,
        config: Config,

        private val contentPriceResolver: ContentPriceResolver
) : Analytics {
    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)
    private val amplitude = Amplitude.getInstance()
            .initialize(context, config.amplitudeKey)
            .enableForegroundTracking(App.app)

    init {
        amplitude.identify(Identify()
                .set(AmplitudeAnalytics.Properties.APPLICATION_ID, context.packageName))
    }

    override fun successLogin() {
        logEvent(EVENT_SUCCESS_LOGIN)
    }

    override fun onBoardingFinished() {
        logEvent(EVENT_ONBOARDING_FINISHED)
    }

    override fun logEvent(name: String, bundle: Bundle?) {
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

    override fun logEventWithName(eventName: String, name: String?) {
        val bundle = Bundle()
        if (name != null) {
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name)
        }
        logEvent(eventName, bundle)
    }

    override fun logEventWithLongParam(event: String, param: String, value: Long) {
        val bundle = Bundle()
        bundle.putLong(param, value)
        logEvent(event, bundle)
    }

    override fun logAmplitudeEvent(eventName: String, params: Map<String, Any?>?) {
        amplitude.logEvent(eventName, params.toJsonObject())
    }

    override fun logAmplitudePurchase(sku: Sku, params: Map<String, Any?>?) {
        val price = contentPriceResolver.resolveSkuPrice(sku)

        amplitude.logRevenueV2(Revenue()
                .setPrice(price)
                .setQuantity(1)
                .setProductId(sku.id.code)
                .setEventProperties(params.toJsonObject()))
    }

    override fun reactionHard(lesson: Long) {
        logEventWithLongParam(EVENT_REACTION_HARD, PARAM_LESSON, lesson)
    }

    override fun reactionEasy(lesson: Long) {
        logEventWithLongParam(EVENT_REACTION_EASY, PARAM_LESSON, lesson)
    }

    override fun reactionHardAfterCorrect(lesson: Long) {
        logEventWithLongParam(EVENT_REACTION_HARD_AFTER_CORRECT, PARAM_LESSON, lesson)
    }

    override fun reactionEasyAfterCorrect(lesson: Long) {
        logEventWithLongParam(EVENT_REACTION_EASY_AFTER_CORRECT, PARAM_LESSON, lesson)
    }

    override fun answerResult(step: Step?, submission: Submission) {
        val lesson = step?.lesson ?: 0
        when (submission.status) {
            Submission.Status.CORRECT -> logEventWithLongParam(EVENT_CORRECT_ANSWER, PARAM_LESSON, lesson)
            Submission.Status.WRONG -> logEventWithLongParam(EVENT_WRONG_ANSWER, PARAM_LESSON, lesson)
        }
    }

    override fun onSubmissionWasMade() {
        logEvent(EVENT_SUBMISSION_WAS_MADE)
    }

    override fun rate(rating: Int) {
        logEventWithLongParam(EVENT_APP_RATE, PARAM_RATING, rating.toLong())
    }

    override fun rateCanceled() {
        logEvent(EVENT_APP_RATE_CANCELED)
    }

    override fun ratePositiveLater() {
        logEvent(EVENT_APP_RATE_POSITIVE_LATER)
    }

    override fun ratePositiveGooglePlay() {
        logEvent(EVENT_APP_RATE_POSITIVE_GOOGLE_PLAY)
    }

    override fun rateNegativeLater() {
        logEvent(EVENT_APP_RATE_NEGATIVE_LATER)
    }

    override fun rateNegativeEmail() {
        logEvent(EVENT_APP_RATE_NEGATIVE_EMAIL)
    }

    override fun statsOpened() {
        logEvent(EVENT_STATS_OPENED)
    }

    override fun paidContentOpened() {
        logEvent(EVENT_PAID_CONTENT_OPENED)
    }


    override fun onExpReached(exp: Long, delta: Long) {
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

    override fun onStreakRestoreDialogShown() {
        logEvent(EVENT_STREAK_RESTORE_DIALOG_SHOWN)
    }

    override fun onStreakRestored(streak: Long) {
        logEventWithLongParam(EVENT_STREAK_RESTORED, PARAM_STREAK, streak)
    }

    override fun onStreakRestoreCanceled(streak: Long) {
        logEventWithLongParam(EVENT_STREAK_RESTORE_CANCELED, PARAM_STREAK, streak)
    }

    override fun onStreakLost(streak: Long) {
        logEventWithLongParam(EVENT_STREAK_LOST, PARAM_STREAK, streak)
    }

    override fun onStreak(streak: Long) {
        logEventWithLongParam(EVENT_STREAK, PARAM_STREAK, streak)
    }

    override fun onNotificationCanceled(days: Int) {
        logEventWithLongParam(EVENT_STREAK, PARAM_NOTIFICATION_DAYS, days.toLong())
    }

    override fun onRatingError() {
        logEvent(EVENT_ON_RATING_ERROR)
    }

    override fun onQuestionsPacksOpened() {
        logEvent(EVENT_ON_QUESTIONS_PACKS_SCREEN_OPENED)
    }

    companion object {
        private fun Map<String, Any?>?.toJsonObject(): JSONObject {
            val properties = JSONObject()
            this?.let {
                for ((k, v) in it.entries) {
                    properties.put(k, v)
                }
            }
            return properties
        }

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
    }
}
