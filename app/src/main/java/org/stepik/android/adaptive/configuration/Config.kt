package org.stepik.android.adaptive.configuration

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.stepik.android.adaptive.di.AppSingleton
import java.io.InputStreamReader
import javax.inject.Inject

@AppSingleton
class Config private constructor(
        @SerializedName("COURSE_ID")                val courseId: Long,

        @SerializedName("HOST")                     val host: String,
        @SerializedName("RATING_HOST")              val ratingHost: String,

        @SerializedName("OAUTH_CLIENT_ID")          val oAuthClientId: String,
        @SerializedName("OAUTH_CLIENT_SECRET")      val oAuthClientSecret: String,
        @SerializedName("GRANT_TYPE")               val grantType: String,

        @SerializedName("OAUTH_CLIENT_ID_SOCIAL")   val oAuthClientIdSocial: String,
        @SerializedName("OAUTH_CLIENT_SECRET_SOCIAL") val oAuthClientSecretSocial: String,
        @SerializedName("GRANT_TYPE_SOCIAL")        val grantTypeSocial: String,

        @SerializedName("REDIRECT_URI")             val redirectUri: String,

        @SerializedName("REFRESH_GRANT_TYPE")       val refreshGrantType: String,

        @SerializedName("GOOGLE_SERVER_CLIENT_ID")  val googleServerClientId: String,
        @SerializedName("CODE_QUERY_PARAMETER")     val codeQueryParameter: String,
        @SerializedName("APP_PUBLIC_LICENSE_KEY")   val appPublicLicenseKey: String,
        @SerializedName("APP_METRICA_KEY")          val appMetricaKey: String,
        @SerializedName("IS_BOOKMARKS_SUPPORTED")   val isBookmarksSupported: Boolean
) {
    @AppSingleton
    class ConfigFactory
    @Inject
    constructor(private val context: Context) {
        fun create(): Config = context.assets.open("config.json").use {
            Gson().fromJson(InputStreamReader(it), Config::class.java)
        }
    }
}