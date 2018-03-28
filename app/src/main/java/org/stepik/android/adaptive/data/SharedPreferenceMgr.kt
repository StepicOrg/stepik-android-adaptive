package org.stepik.android.adaptive.data

import android.content.Context
import android.preference.PreferenceManager

import com.google.gson.Gson
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.stepik.android.adaptive.api.Api
import org.stepik.android.adaptive.api.oauth.OAuthResponse
import org.stepik.android.adaptive.content.questions.packs.QuestionsPack
import org.stepik.android.adaptive.data.model.AccountCredentials
import org.stepik.android.adaptive.data.model.Profile
import org.stepik.android.adaptive.di.AppSingleton
import org.stepik.android.adaptive.util.RxOptional
import javax.inject.Inject

@AppSingleton
class SharedPreferenceMgr
@Inject
constructor(context: Context) {

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val gson = Gson()

    var oAuthResponse: OAuthResponse?
        get() {
            val json = getString(OAUTH_RESPONSE) ?: return null
            return gson.fromJson(json, OAuthResponse::class.java)
        }
        set(response) {
            if (response != null) {
                val json = gson.toJson(response)

                val currentTime = DateTime.now(DateTimeZone.UTC).millis

                saveString(OAUTH_RESPONSE, json)
                saveLong(OAUTH_RESPONSE_DEADLINE, currentTime + (response.expiresIn - 50) * 1000)
            }
        }

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
        private set(value) {
            saveLong(PROFILE_ID, value)
        }

    val fakeUser: RxOptional<AccountCredentials>
        get() = RxOptional(getString(FAKE_USER)).map { gson.fromJson(it, AccountCredentials::class.java) }

    var isNotFirstTime: Boolean
        get() = getBoolean(NOT_FIRST_TIME)
        set(notFirstTime) = saveBoolean(NOT_FIRST_TIME, notFirstTime)

    val isStreakRestoreTooltipWasShown: Boolean
        get() = getBoolean(IS_STREAK_RESTORE_TOOLTIP_WAS_SHOWN)

    val isPaidContentTooltipWasShown: Boolean
        get() = getBoolean(IS_PAID_CONTENT_TOOLTIP_WAS_SHOWN)

    val isQuestionsPacksTooltipWasShown: Boolean
        get() = getBoolean(IS_QUESTIONS_PACKS_TOOLTIP_WAS_SHOWN)

    var isAuthTokenSocial: Boolean
        get() = getBoolean(IS_OAUTH_TOKEN_SOCIAL)
        set(value) = saveBoolean(IS_OAUTH_TOKEN_SOCIAL, value)

    val authResponseDeadline: Long
        get() = getLong(OAUTH_RESPONSE_DEADLINE)

    var questionsPackIndex: Int
        get() = getInt(QUESTIONS_PACK_INDEX)
        set(value) = saveInt(QUESTIONS_PACK_INDEX, value)

    fun removeProfile() {
        Api.authLock.lock()
        remove(PROFILE)
        remove(PROFILE_ID)
        remove(OAUTH_RESPONSE)
        remove(IS_OAUTH_TOKEN_SOCIAL)
        remove(OAUTH_RESPONSE_DEADLINE)
        Api.authLock.unlock()
    }

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

    fun resetAuthResponseDeadline() {
        remove(OAUTH_RESPONSE_DEADLINE)
    }

    fun onQuestionsPackViewed(pack: QuestionsPack) {
        saveBoolean(QUESTIONS_PACK_VIEWED_PREFIX + pack.id, true)
    }

    fun isQuestionsPackViewed(pack: QuestionsPack): Boolean =
            getBoolean(QUESTIONS_PACK_VIEWED_PREFIX + pack.id)

    fun saveBoolean(name: String, data: Boolean?) {
        sharedPreferences.edit().putBoolean(name, data!!).apply()
    }

    private fun saveString(name: String, data: String) {
        sharedPreferences.edit().putString(name, data).apply()
    }

    fun saveLong(name: String, data: Long) {
        sharedPreferences.edit().putLong(name, data).apply()
    }

    fun changeLong(name: String, delta: Long): Long {
        val value = getLong(name) + delta
        sharedPreferences.edit().putLong(name, value).apply()
        return value
    }

    fun saveInt(name: String, data: Int) {
        sharedPreferences.edit().putInt(name, data).apply()
    }

    private fun getString(name: String): String? = sharedPreferences.getString(name, null)

    fun getLong(name: String): Long = sharedPreferences.getLong(name, 0)

    fun getBoolean(name: String): Boolean = sharedPreferences.getBoolean(name, false)

    fun getInt(name: String): Int = sharedPreferences.getInt(name, 0)

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

        private const val QUESTIONS_PACK_INDEX = "questions_pack_index"

        private const val FAKE_USER = "fake_user"

        private const val QUESTIONS_PACK_VIEWED_PREFIX = "viewed_"
    }

}