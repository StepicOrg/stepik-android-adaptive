//package org.stepik.android.adaptive.configuration
//
//import android.content.Context
//import com.google.gson.Gson
//import com.google.gson.annotations.SerializedName
//import org.stepik.android.adaptive.di.AppSingleton
//import java.io.InputStreamReader
//import javax.inject.Inject
//
//@AppSingleton
//class Config private constructor() {
//
//    @AppSingleton
//    class ConfigFactory
//    @Inject
//    constructor(private val context: Context) {
//        fun create(): Config = context.assets.open("config.json").use {
//            Gson().fromJson(InputStreamReader(it), Config::class.java)
//        }
//    }
//
//    @SerializedName("COURSE_ID")
//    val courseId: Long = 0
//
//    private val HOST = "HOST"
//    private val RATING_HOST = "RATING_HOST"
//
//    private val OAUTH_CLIENT_ID = "OAUTH_CLIENT_ID"
//    private val OAUTH_CLIENT_SECRET = "OAUTH_CLIENT_SECRET"
//    private val GRANT_TYPE = "GRANT_TYPE"
//
//    private val OAUTH_CLIENT_ID_SOCIAL = "OAUTH_CLIENT_ID_SOCIAL"
//    private val OAUTH_CLIENT_SECRET_SOCIAL = "OAUTH_CLIENT_SECRET_SOCIAL"
//    private val GRANT_TYPE_SOCIAL = "GRANT_TYPE_SOCIAL"
//
//    private val REDIRECT_URI = "REDIRECT_URI"
//
//    private val REFRESH_GRANT_TYPE = "REFRESH_GRANT_TYPE"
//
//    private val GOOGLE_SERVER_CLIENT_ID = "GOOGLE_SERVER_CLIENT_ID"
//
//    private val CODE_QUERY_PARAMETER = "CODE_QUERY_PARAMETER"
//
//    private val APP_PUBLIC_LICENSE_KEY = "APP_PUBLIC_LICENSE_KEY"
//
//    private val APP_METRICA_KEY = "APP_METRICA_KEY"
//
//    private val IS_BOOKMARKS_SUPPORTED = false
//
//    fun getHost() = HOST
//
//    fun getRatingHost() = RATING_HOST
//
//    fun getOAuthClientId() = OAUTH_CLIENT_ID
//
//    fun getOAuthClientSecret(): String {
//        return OAUTH_CLIENT_SECRET
//    }
//
//    fun getGrantType(): String {
//        return GRANT_TYPE
//    }
//
//    fun getOAuthClientIdSocial(): String {
//        return OAUTH_CLIENT_ID_SOCIAL
//    }
//
//    fun getOAuthClientSecretSocial(): String {
//        return OAUTH_CLIENT_SECRET_SOCIAL
//    }
//
//    fun getGrantTypeSocial(): String {
//        return GRANT_TYPE_SOCIAL
//    }
//
//    fun getRedirectUri(): String {
//        return REDIRECT_URI
//    }
//
//    fun getRefreshGrantType(): String {
//        return REFRESH_GRANT_TYPE
//    }
//
//    fun getGoogleServerClientId(): String {
//        return GOOGLE_SERVER_CLIENT_ID
//    }
//
//    fun getCodeQueryParameter(): String {
//        return CODE_QUERY_PARAMETER
//    }
//
//    fun getAppPublicLicenseKey(): String {
//        return APP_PUBLIC_LICENSE_KEY
//    }
//
//    fun getAppMetricaKey(): String {
//        return APP_METRICA_KEY
//    }
//
//    fun isBookmarksSupported(): Boolean {
//        return IS_BOOKMARKS_SUPPORTED
//    }
//
//}