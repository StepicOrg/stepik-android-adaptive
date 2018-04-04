package org.stepik.android.adaptive.util

import org.stepik.android.adaptive.data.SharedPreferenceHelper
import org.stepik.android.adaptive.di.AppSingleton
import javax.inject.Inject

@AppSingleton
class RateAppManager
@Inject
constructor(
        private val sharedPreferenceHelper: SharedPreferenceHelper
) {
    companion object {
        private const val NOTIFY_DELAY_LATER = (1000 * 60 * 60 * 24 * 2).toLong()
        private const val NOTIFY_DELAY_NEGATIVE = (1000 * 60 * 60 * 24 * 14).toLong()

        private const val REQUIRED_ENGAGEMENT = 10

        private const val KEY_RATED = "rate_app_is_rated"
        private const val KEY_NOTIFY_ALLOWED = "rate_app_notify_allowed"
        private const val KEY_ENGAGEMENT_COUNT = "rate_app_engagement_count"
    }

    /**
     * Registers engagement and notifies when to show rate dialog
     * @return true if you should show app rate dialog
     */
    fun onEngagement(): Boolean {
        val isRated = sharedPreferenceHelper.getBoolean(KEY_RATED)
        val notifyAllowed = sharedPreferenceHelper.getLong(KEY_NOTIFY_ALLOWED)

        if (!isRated && notifyAllowed == 0L && notifyAllowed < System.currentTimeMillis()) {
            val engagements = sharedPreferenceHelper.getLong(KEY_ENGAGEMENT_COUNT)
            if (engagements + 1 == REQUIRED_ENGAGEMENT.toLong()) {
                return true
            } else {
                sharedPreferenceHelper.saveLong(KEY_ENGAGEMENT_COUNT, engagements + 1)
            }
        }
        return false
    }

    fun onRated() {
        sharedPreferenceHelper.saveBoolean(KEY_RATED, true)
    }

    fun onCloseLater() {
        sharedPreferenceHelper.saveLong(KEY_ENGAGEMENT_COUNT, 0)
        sharedPreferenceHelper.saveLong(KEY_NOTIFY_ALLOWED, sharedPreferenceHelper.getLong(KEY_NOTIFY_ALLOWED) + NOTIFY_DELAY_LATER)
    }

    fun onCloseNegative() {
        sharedPreferenceHelper.saveLong(KEY_ENGAGEMENT_COUNT, 0)
        sharedPreferenceHelper.saveLong(KEY_NOTIFY_ALLOWED, sharedPreferenceHelper.getLong(KEY_NOTIFY_ALLOWED) + NOTIFY_DELAY_NEGATIVE)
    }

    fun reset() {
        sharedPreferenceHelper.remove(KEY_RATED)
        sharedPreferenceHelper.remove(KEY_NOTIFY_ALLOWED)
        sharedPreferenceHelper.remove(KEY_ENGAGEMENT_COUNT)
    }
}
