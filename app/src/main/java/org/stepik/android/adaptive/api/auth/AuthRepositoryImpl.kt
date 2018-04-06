package org.stepik.android.adaptive.api.auth

import io.reactivex.Single
import org.stepik.android.adaptive.api.RegistrationResponse
import org.stepik.android.adaptive.api.UserRegistrationRequest
import org.stepik.android.adaptive.api.login.SocialManager
import org.stepik.android.adaptive.configuration.Config
import org.stepik.android.adaptive.data.SharedPreferenceHelper
import org.stepik.android.adaptive.data.model.RegistrationUser
import org.stepik.android.adaptive.di.AppSingleton
import org.stepik.android.adaptive.di.qualifiers.AuthLock
import org.stepik.android.adaptive.di.qualifiers.AuthService
import org.stepik.android.adaptive.di.qualifiers.CookieAuthService
import org.stepik.android.adaptive.di.qualifiers.SocialAuthService
import retrofit2.Response
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import kotlin.concurrent.withLock

@AppSingleton
class AuthRepositoryImpl
@Inject
constructor(
        @AuthLock
        private val authLock: ReentrantLock,

        @AuthService
        private val authService: OAuthService,
        @SocialAuthService
        private val socialAuthService: OAuthService,
        @CookieAuthService
        private val cookieAuthService: OAuthService,

        private val config: Config,
        private val sharedPreferenceHelper: SharedPreferenceHelper
): AuthRepository {

    private fun saveResponse(response: OAuthResponse, isSocial: Boolean) = authLock.withLock {
        sharedPreferenceHelper.oAuthResponse = response
        sharedPreferenceHelper.isAuthTokenSocial = isSocial
    }

    override fun authWithLoginPassword(login: String, password: String): Single<OAuthResponse> = authService
            .authWithLoginPassword(config.grantType, login, password)
            .doOnSuccess { saveResponse(it, isSocial = false) }

    override fun authWithNativeCode(code: String, type: SocialManager.SocialType): Single<OAuthResponse> {
        var codeType: String? = null
        if (type.needUseAccessTokenInsteadOfCode()) {
            codeType = "access_token"
        }

        return socialAuthService.getTokenByNativeCode(
                type.identifier,
                code,
                config.grantTypeSocial,
                config.redirectUri,
                codeType)
                .doOnSuccess { saveResponse(it, isSocial = true) }
    }

    override fun authWithCode(code: String): Single<OAuthResponse> = socialAuthService
            .getTokenByCode(config.grantTypeSocial, code, config.redirectUri)
            .doOnSuccess { saveResponse(it, isSocial = true) }

    override fun authWithRefreshToken(refreshToken: String): Single<OAuthResponse> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createAccount(credentials: RegistrationUser): Single<Response<RegistrationResponse>> =
            cookieAuthService.createAccount(UserRegistrationRequest(credentials))

}