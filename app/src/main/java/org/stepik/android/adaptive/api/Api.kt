package org.stepik.android.adaptive.api

import android.net.Uri
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.stepik.android.adaptive.configuration.Config
import org.stepik.android.adaptive.Util
import org.stepik.android.adaptive.api.auth.EmptyAuthService
import org.stepik.android.adaptive.api.auth.OAuthService
import org.stepik.android.adaptive.api.auth.OAuthResponse
import org.stepik.android.adaptive.api.login.SocialManager
import org.stepik.android.adaptive.core.LogoutHelper
import org.stepik.android.adaptive.core.ScreenManager
import org.stepik.android.adaptive.data.SharedPreferenceHelper
import org.stepik.android.adaptive.data.model.EnrollmentWrapper
import org.stepik.android.adaptive.data.model.AccountCredentials
import org.stepik.android.adaptive.data.model.Profile
import org.stepik.android.adaptive.data.model.RecommendationReaction
import org.stepik.android.adaptive.data.model.Submission
import org.stepik.android.adaptive.di.AppSingleton
import org.stepik.android.adaptive.util.AppConstants

import java.io.IOException
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

import javax.inject.Inject

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import org.stepik.android.adaptive.api.auth.CookieHelper
import org.stepik.android.adaptive.content.questions.QuestionsPacksManager
import org.stepik.android.adaptive.di.qualifiers.AuthLock
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named

@AppSingleton
class Api
@Inject
constructor(
        private val config: Config,
        private val sharedPreferenceHelper: SharedPreferenceHelper,
        private val logoutHelper: LogoutHelper,
        private val questionsPacksManager: QuestionsPacksManager,

        @Named(AppConstants.userAgentName)
        private val userAgent: String,

        @AuthLock
        private val authLock: ReentrantLock,

        private val cookieHelper: CookieHelper
) {
    companion object {
        private const val FAKE_MAIL_PATTERN = "adaptive_%s_android_%d%s@stepik.org"
        private const val TIMEOUT_IN_SECONDS = 60
    }

    private var authServiceTokenType: TokenType? = null
    private var authService: OAuthService? = null

    private val stepikService: StepikService
    private val ratingService: RatingService

    private val authInterceptor = Interceptor { chain ->
        val request = addUserAgentTo(chain)

        var response = addAuthHeaderAndProceed(chain, request)
        if (response.code() == 400) { // was bug when user has incorrect token deadline due to wrong datetime had been set on phone
            sharedPreferenceHelper.resetAuthResponseDeadline()
            response = addAuthHeaderAndProceed(chain, request)
        }

        response
    }

    private val isUpdateNeeded: Boolean
        get() {
            val expireAt = sharedPreferenceHelper.authResponseDeadline
            return DateTime.now(DateTimeZone.UTC).millis > expireAt
        }

    val profile: Observable<ProfileResponse>
        get() = stepikService.profile

    private enum class TokenType {
        SOCIAL,
        PASSWORD
    }

    init {
        stepikService = initStepikService()
        ratingService = initRatingService()
    }

    /**
     * Returns new authService with specified token type
     * @param tokenType - SOCIAL or PASSWORD
     * @return new authService
     */
    private fun initAuthService(tokenType: TokenType): OAuthService {
        val okHttpBuilder = OkHttpClient.Builder()

        val credentials = if (tokenType == TokenType.SOCIAL) {
            Credentials.basic(config.oAuthClientIdSocial, config.oAuthClientSecretSocial)
        } else {
            Credentials.basic(config.oAuthClientId, config.oAuthClientSecret)
        }

        okHttpBuilder.addInterceptor { chain ->
            chain.proceed(addUserAgentTo(chain).newBuilder().header(AppConstants.authorizationHeaderName, credentials).build())
        }

        setTimeout(okHttpBuilder, TIMEOUT_IN_SECONDS)

        val retrofit = buildRetrofit(okHttpBuilder.build(), config.host)

        return retrofit.create(OAuthService::class.java)
    }

    @Synchronized
    private fun getAuthService(tokenType: TokenType): OAuthService {
        if (authService == null || tokenType != authServiceTokenType) {
            authServiceTokenType = tokenType
            authService = initAuthService(tokenType)
        }
        return authService!!
    }

    private fun authWithRefreshToken(refreshToken: String): Call<OAuthResponse> {
        return getAuthService(if (sharedPreferenceHelper.isAuthTokenSocial) TokenType.SOCIAL else TokenType.PASSWORD)
                .refreshAccessToken(config.refreshGrantType, refreshToken)
    }

    fun createFakeAccount(): AccountCredentials {
        val email = String.format(FAKE_MAIL_PATTERN, config.courseId, System.currentTimeMillis(), Util.randomString(5))
        val password = Util.randomString(16)
        val firstName = Util.randomString(10)
        val lastName = Util.randomString(10)
        return AccountCredentials(email, password, firstName, lastName)
    }

    private fun initRatingService(): RatingService {
        val okHttpBuilder = OkHttpClient.Builder()
        okHttpBuilder.addInterceptor(authInterceptor)

        setTimeout(okHttpBuilder, TIMEOUT_IN_SECONDS)
        val retrofit = buildRetrofit(okHttpBuilder.build(), config.ratingHost)

        return retrofit.create(RatingService::class.java)
    }

    private fun initStepikService(): StepikService {
        val okHttpBuilder = OkHttpClient.Builder()
        okHttpBuilder.addInterceptor(authInterceptor)

        setTimeout(okHttpBuilder, TIMEOUT_IN_SECONDS)
        val retrofit = buildRetrofit(okHttpBuilder.build(), config.host)

        return retrofit.create(StepikService::class.java)
    }

    @Throws(IOException::class)
    private fun addAuthHeaderAndProceed(chain: Interceptor.Chain, req: Request): okhttp3.Response {
        var request = req
        try {
            authLock.lock()
            var response = sharedPreferenceHelper.oAuthResponse

            if (response != null) {
                if (isUpdateNeeded) {
                    val oAuthResponse: Response<OAuthResponse>
                    try {
                        oAuthResponse = authWithRefreshToken(response.refreshToken).execute()
                        response = oAuthResponse.body()
                    } catch (e: IOException) {
                        e.printStackTrace()
                        return chain.proceed(request)
                    }

                    if (response == null || !oAuthResponse.isSuccessful) {
                        if (oAuthResponse.code() == 401) {
                            logoutHelper.logout {
                                ScreenManager.getInstance().showOnboardingScreen()
                            }
                        }
                        return chain.proceed(request)
                    }

                    sharedPreferenceHelper.oAuthResponse = response
                }
                request = request.newBuilder()
                        .addHeader(AppConstants.authorizationHeaderName, response.tokenType + " " + response.accessToken)
                        .build()
            }
        } finally {
            authLock.unlock()
        }

        return chain.proceed(request)
    }

    fun remindPassword(email: String): Completable {
        val encodedEmail = URLEncoder.encode(email)
        val interceptor = Interceptor { chain ->
            var newRequest = addUserAgentTo(chain)

            val cookies = cookieHelper.getCookiesForBaseUrl() ?: return@Interceptor chain.proceed(newRequest)
            var csrftoken: String? = null
            var sessionId: String? = null
            for (item in cookies) {
                if (item.name == AppConstants.csrfTokenCookieName) {
                    csrftoken = item.value
                    continue
                }
                if (item.name == AppConstants.sessionCookieName) {
                    sessionId = item.value
                }
            }

            val cookieResult = AppConstants.csrfTokenCookieName + "=" + csrftoken + "; " + AppConstants.sessionCookieName + "=" + sessionId
            if (csrftoken == null) return@Interceptor chain.proceed(newRequest)
            val url = newRequest
                    .url()
                    .newBuilder()
                    .addQueryParameter("csrfmiddlewaretoken", csrftoken)
                    .addQueryParameter("csrfmiddlewaretoken", csrftoken)
                    .build()
            newRequest = newRequest.newBuilder()
                    .addHeader("referer", config.host)
                    .addHeader("X-CSRFToken", csrftoken)
                    .addHeader("Cookie", cookieResult)
                    .url(url)
                    .build()
            chain.proceed(newRequest)
        }
        val okHttpBuilder = OkHttpClient.Builder()
        okHttpBuilder.addNetworkInterceptor(interceptor)
        //        okHttpBuilder.addNetworkInterceptor(this.stethoInterceptor);
        setTimeout(okHttpBuilder, TIMEOUT_IN_SECONDS)
        val notLogged = buildRetrofit(okHttpBuilder.build(), config.host)

        val tempService = notLogged.create(EmptyAuthService::class.java)
        return tempService.remindPassword(encodedEmail)
    }

    fun getCourses(ids: LongArray): Single<CoursesResponse> =
            stepikService.getCourses(ids)

    fun joinCourse(course: Long): Completable =
            stepikService.joinCourse(EnrollmentWrapper(course))

    fun getNextRecommendations(count: Int): Observable<RecommendationsResponse> =
            stepikService.getNextRecommendations(questionsPacksManager.currentCourseId, count)

    fun getSteps(lesson: Long): Observable<StepsResponse> =
            stepikService.getSteps(lesson)

    fun createAttempt(step: Long): Observable<AttemptResponse> =
            stepikService.createAttempt(AttemptRequest(step))

    fun getAttempts(step: Long): Observable<AttemptResponse> =
            stepikService.getAttempts(step, sharedPreferenceHelper.profileId)

    fun createSubmission(submission: Submission): Completable =
            stepikService.createSubmission(SubmissionRequest(submission))

    fun getSubmissions(attempt: Long): Observable<SubmissionResponse> =
            stepikService.getSubmissions(attempt, "desc")

    fun createReaction(reaction: RecommendationReaction): Completable =
            stepikService.createRecommendationReaction(RecommendationReactionsRequest(reaction))

    fun getLessons(lesson: Long): Observable<LessonsResponse> = stepikService.getLessons(lesson)

    fun setProfile(profile: Profile): Completable =
            stepikService.setProfile(profile.id, ProfileRequest(profile))

    fun getUnits(lesson: Long): Observable<UnitsResponse> =
            stepikService.getUnits(config.courseId, lesson)

    fun reportView(assignment: Long, step: Long): Completable =
            stepikService.reportView(ViewRequest(assignment, step))

    fun getRating(count: Int, days: Int): Observable<RatingResponse> =
            ratingService.getRating(config.courseId, count.toLong(), days.toLong(), sharedPreferenceHelper.profileId)

    fun putRating(exp: Long): Completable =
            ratingService.putRating(RatingRequest(exp, config.courseId, sharedPreferenceHelper.oAuthResponse?.accessToken))

    private fun setTimeout(builder: OkHttpClient.Builder, seconds: Int) {
        builder.connectTimeout(seconds.toLong(), TimeUnit.SECONDS)
        builder.readTimeout(seconds.toLong(), TimeUnit.SECONDS)
    }

    private fun addUserAgentTo(chain: Interceptor.Chain): Request {
        return chain
                .request()
                .newBuilder()
                .header(AppConstants.userAgentName, userAgent)
                .build()
    }

    private fun buildRetrofit(client: OkHttpClient, baseUrl: String): Retrofit {
        return Retrofit.Builder()
                .baseUrl(baseUrl)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
    }

    fun getUriForSocialAuth(type: SocialManager.SocialType): Uri {
        val socialIdentifier = type.identifier
        val url = config.host + "accounts/" + socialIdentifier + "/login?next=/oauth2/authorize/?" + Uri.encode("client_id=" + config.oAuthClientIdSocial + "&response_type=code")
        return Uri.parse(url)
    }
}
