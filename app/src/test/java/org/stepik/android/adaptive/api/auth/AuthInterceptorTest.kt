package org.stepik.android.adaptive.api.auth

import com.nhaarman.mockito_kotlin.*
import okhttp3.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Before
import org.junit.Test
import org.stepik.android.adaptive.configuration.Config
import org.stepik.android.adaptive.core.LogoutHelper
import org.stepik.android.adaptive.data.preference.AuthPreferences
import org.stepik.android.adaptive.util.AppConstants
import java.util.concurrent.locks.ReentrantLock

class AuthInterceptorTest {

    private val userAgent = "user-agent"
    private lateinit var authLock: ReentrantLock
    private lateinit var authService: OAuthService
    private lateinit var socialAuthService: OAuthService

    private lateinit var config: Config
    private lateinit var authPreferences: AuthPreferences

    private lateinit var logoutHelper: LogoutHelper

    private fun isRequestsEquals(expected: Request?, argument: Request?) =
            expected == argument || (
            expected != null && argument != null &&
                    expected.isHttps == argument.isHttps &&
                    expected.headers() == argument.headers() &&
                    expected.url() == argument.url() &&
                    expected.method() == argument.method()
            )

    @Before
    fun prepare() {
        authLock = ReentrantLock()
        authService = mock()
        socialAuthService = mock()

        config = mock()
        authPreferences = mock()

        logoutHelper = mock()
    }

    @Test
    fun correctAccessTokenTest() {
        val oAuthResponse = OAuthResponse("", 36000, "", "1234", "Bearer")
        whenever(authPreferences.oAuthResponse) doReturn oAuthResponse
        whenever(authPreferences.authResponseDeadline) doReturn DateTime.now(DateTimeZone.UTC).millis + 100000

        val request = Request.Builder().url("http://stepik.org").build()
        val response = Response.Builder().code(200).protocol(Protocol.HTTP_2).message("OK").request(request).build()
        val chain = mock<Interceptor.Chain> {
            on { request() } doReturn request
            on { proceed(any()) } doReturn response
        }

        val authInterceptor = AuthInterceptor(userAgent, authLock, authService, socialAuthService, config, authPreferences, logoutHelper)
        authInterceptor.intercept(chain)

        val targetRequest = request.newBuilder()
                .addHeader(AppConstants.userAgentName, userAgent)
                .addHeader(AppConstants.authorizationHeaderName, oAuthResponse.tokenType + " " + oAuthResponse.accessToken)
                .build()

        verify(chain).request()
        verify(chain).proceed(argThat {
            isRequestsEquals(this, targetRequest)
        })
        verify(authPreferences)::authResponseDeadline.get()
        verify(authPreferences)::oAuthResponse.get()

        verifyNoMoreInteractions(authPreferences)

        verifyZeroInteractions(logoutHelper)
        verifyZeroInteractions(config)
        verifyZeroInteractions(authService)
        verifyZeroInteractions(socialAuthService)
    }

    @Test
    fun expiredAccessToken() {
        val oAuthResponse = OAuthResponse("1111", 36000, "", "1234", "Bearer")
        whenever(authPreferences.oAuthResponse) doReturn oAuthResponse
        whenever(authPreferences.authResponseDeadline) doReturn DateTime.now(DateTimeZone.UTC).millis - 100000
        whenever(authPreferences.isAuthTokenSocial) doReturn false

        val refreshResponse = retrofit2.Response.success(oAuthResponse)
        val refreshCall = mock<retrofit2.Call<OAuthResponse>> { on { execute() } doReturn refreshResponse }

        val refreshGrantType = "refresh"
        whenever(config.refreshGrantType) doReturn refreshGrantType
        whenever(authService.refreshAccessToken(refreshGrantType, oAuthResponse.refreshToken)) doReturn refreshCall

        val request = Request.Builder().url("http://stepik.org").build()
        val response = Response.Builder().code(200).protocol(Protocol.HTTP_2).message("OK").request(request).build()
        val chain = mock<Interceptor.Chain> {
            on { request() } doReturn request
            on { proceed(any()) } doReturn response
        }
        val targetRequest = request.newBuilder()
                .addHeader(AppConstants.userAgentName, userAgent)
                .addHeader(AppConstants.authorizationHeaderName, oAuthResponse.tokenType + " " + oAuthResponse.accessToken)
                .build()


        val authInterceptor = AuthInterceptor(userAgent, authLock, authService, socialAuthService, config, authPreferences, logoutHelper)
        authInterceptor.intercept(chain)

        verify(authPreferences)::oAuthResponse.set(oAuthResponse)

        verify(chain).request()
        verify(chain).proceed(argThat {
            isRequestsEquals(this, targetRequest)
        })
        verify(authService).refreshAccessToken(refreshGrantType, oAuthResponse.refreshToken)
        verify(config)::refreshGrantType.get()

        verifyZeroInteractions(logoutHelper)
        verifyNoMoreInteractions(config)
        verifyNoMoreInteractions(authService)
        verifyZeroInteractions(socialAuthService)
    }

}