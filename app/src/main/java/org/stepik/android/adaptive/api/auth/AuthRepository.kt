package org.stepik.android.adaptive.api.auth

import io.reactivex.Single
import org.stepik.android.adaptive.api.RegistrationResponse
import org.stepik.android.adaptive.data.model.RegistrationUser
import retrofit2.Response

interface AuthRepository {

    fun authWithLoginPassword(login: String, password: String): Single<OAuthResponse>
    fun authWithNativeCode(code: String, type: SocialManager.SocialType): Single<OAuthResponse>

    fun authWithCode(code: String): Single<OAuthResponse>

    fun createAccount(credentials: RegistrationUser): Single<Response<RegistrationResponse>>

}