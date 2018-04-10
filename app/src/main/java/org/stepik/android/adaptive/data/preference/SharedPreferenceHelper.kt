package org.stepik.android.adaptive.data.preference

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

import com.google.gson.Gson
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.stepik.android.adaptive.api.auth.OAuthResponse
import org.stepik.android.adaptive.data.model.AccountCredentials
import org.stepik.android.adaptive.data.model.Profile
import org.stepik.android.adaptive.content.questions.QuestionsPack
import org.stepik.android.adaptive.di.AppSingleton
import org.stepik.android.adaptive.util.RxOptional
import javax.inject.Inject

@AppSingleton
class SharedPreferenceHelper
@Inject
constructor(context: Context): SharedPreferenceProvider, AuthPreferences {
    override val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val gson = Gson()

    override var oAuthResponse: OAuthResponse?
        get() {
            val json = getString(OAUTH_RESPONSE) ?: return null
            return gson.fromJson(json, OAuthResponse::class.java)
        }
        set(response) {
            if (response != null) {
                val json = gson.toJson(response)

                val currentTime = DateTime.now(DateTimeZone.UTC).millis

                saveString(OAUTH_RESPONSE, json)
                authResponseDeadline = currentTime + (response.expiresIn - 50) * 1000
            }
        }

    override var authResponseDeadline: Long
        get() = getLong(OAUTH_RESPONSE_DEADLINE)
        private set(value) = saveLong(OAUTH_RESPONSE_DEADLINE, value)

    var profile: Profile?
        get() {
            val json = getString(PROFILE) ?: return null
            return gson.fromJson(json, Profile::class.java)
        }
        set(profile) {
            if (profile != null) {
                val json = gson.toJson(profile)
                saveString(PROFILE, json)

                profileId = profile.id
            }
        }

    var profileId: Long
        get() = getLong(PROFILE_ID)
        private set(value) = saveLong(PROFILE_ID, value)

    val fakeUser: RxOptional<AccountCredentials>
        get() = RxOptional(getString(FAKE_USER)).map { gson.fromJson(it, AccountCredentials::class.java) }

    val isStreakRestoreTooltipWasShown:  Boolean by preference(IS_STREAK_RESTORE_TOOLTIP_WAS_SHOWN)
    val isPaidContentTooltipWasShown:    Boolean by preference(IS_PAID_CONTENT_TOOLTIP_WAS_SHOWN)
    val isQuestionsPacksTooltipWasShown: Boolean by preference(IS_QUESTIONS_PACKS_TOOLTIP_WAS_SHOWN)

    var isGamificationDescriptionWasShown: Boolean by preference(IS_PACKS_FOR_LEVELS_WINDOW_WAS_SHOWN)

    override var isAuthTokenSocial:      Boolean by preference(IS_OAUTH_TOKEN_SOCIAL)
    var isNotFirstTime:                  Boolean by preference(NOT_FIRST_TIME)

    var questionsPackIndex: Int by preference(QUESTIONS_PACK_INDEX)

    fun removeProfile() {
        remove(PROFILE)
        remove(PROFILE_ID)
        remove(OAUTH_RESPONSE)
        remove(IS_OAUTH_TOKEN_SOCIAL)
        remove(OAUTH_RESPONSE_DEADLINE)
    }

    fun isFakeUser(): Boolean =
            sharedPreferences.contains(FAKE_USER)

    fun removeFakeUser() {
        remove(FAKE_USER)
    }

    fun saveFakeUser(credentials: AccountCredentials) {
        val json = gson.toJson(credentials)
        saveString(FAKE_USER, json)
    }

    fun afterStreakRestoreTooltipWasShown() {
        saveBoolean(IS_STREAK_RESTORE_TOOLTIP_WAS_SHOWN, true)
    }

    fun afterPaidContentTooltipWasShown() {
        saveBoolean(IS_PAID_CONTENT_TOOLTIP_WAS_SHOWN, true)
    }

    fun afterQuestionsPacksTooltipWasShown() {
        saveBoolean(IS_QUESTIONS_PACKS_TOOLTIP_WAS_SHOWN, true)
    }

    override fun resetAuthResponseDeadline() {
        remove(OAUTH_RESPONSE_DEADLINE)
    }

    fun onQuestionsPackViewed(pack: QuestionsPack) {
        saveBoolean(QUESTIONS_PACK_VIEWED_PREFIX + pack.id, true)
    }

    fun isQuestionsPackViewed(pack: QuestionsPack): Boolean =
            sharedPreferences[QUESTIONS_PACK_VIEWED_PREFIX + pack.id]

    fun saveBoolean(name: String, data: Boolean) {
        sharedPreferences[name] = data
    }

    private fun saveString(name: String, data: String) {
        sharedPreferences[name] = data
    }

    fun saveLong(name: String, data: Long) {
        sharedPreferences[name] = data
    }

    fun changeLong(name: String, delta: Long): Long {
        val value = sharedPreferences.get<Long>(name) + delta
        sharedPreferences[name] = value
        return value
    }

    private fun getString(name: String): String? = sharedPreferences[name]

    fun getLong(name: String): Long = sharedPreferences[name]

    fun getBoolean(name: String): Boolean = sharedPreferences[name]

    fun remove(name: String) {
        sharedPreferences.edit().remove(name).apply()
    }

    companion object {
        private const val OAUTH_RESPONSE = "oauth_response"
        private const val IS_OAUTH_TOKEN_SOCIAL = "is_oauth_token_social"
        private const val OAUTH_RESPONSE_DEADLINE = "oauth_response_deadline"

        private const val PROFILE = "profile"
        private const val PROFILE_ID = "profile_id"

        private const val NOT_FIRST_TIME = "not_first_time"

        private const val IS_STREAK_RESTORE_TOOLTIP_WAS_SHOWN = "is_streak_restore_tooltip_was_shown"
        private const val IS_PAID_CONTENT_TOOLTIP_WAS_SHOWN = "is_paid_content_tooltip_was_shown"

        private const val IS_QUESTIONS_PACKS_TOOLTIP_WAS_SHOWN = "is_questions_packs_tooltip_was_shown"

        private const val IS_PACKS_FOR_LEVELS_WINDOW_WAS_SHOWN = "is_packs_for_levels_window_was_shown"

        private const val QUESTIONS_PACK_INDEX = "questions_pack_index"

        private const val FAKE_USER = "fake_user"

        private const val QUESTIONS_PACK_VIEWED_PREFIX = "viewed_"
    }

}