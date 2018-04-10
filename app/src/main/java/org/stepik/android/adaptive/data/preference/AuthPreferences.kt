package org.stepik.android.adaptive.data.preference

import org.stepik.android.adaptive.api.auth.OAuthResponse

interface AuthPreferences {
    var oAuthResponse: OAuthResponse?
    val authResponseDeadline: Long
    var isAuthTokenSocial: Boolean

    fun resetAuthResponseDeadline()
}