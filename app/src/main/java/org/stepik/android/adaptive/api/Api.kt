package org.stepik.android.adaptive.api

import android.net.Uri
import org.stepik.android.adaptive.configuration.Config
import org.stepik.android.adaptive.Util
import org.stepik.android.adaptive.api.login.SocialManager
import org.stepik.android.adaptive.data.SharedPreferenceHelper
import org.stepik.android.adaptive.data.model.EnrollmentWrapper
import org.stepik.android.adaptive.data.model.AccountCredentials
import org.stepik.android.adaptive.data.model.Profile
import org.stepik.android.adaptive.data.model.RecommendationReaction
import org.stepik.android.adaptive.data.model.Submission
import org.stepik.android.adaptive.di.AppSingleton
import org.stepik.android.adaptive.util.AppConstants

import java.net.URLEncoder

import javax.inject.Inject

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.stepik.android.adaptive.api.auth.*
import org.stepik.android.adaptive.content.questions.QuestionsPacksManager
import org.stepik.android.adaptive.util.addUserAgent
import org.stepik.android.adaptive.util.setTimeoutsInSeconds
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
        private val questionsPacksManager: QuestionsPacksManager,

        @Named(AppConstants.userAgentName)
        private val userAgent: String,

        private val cookieHelper: CookieHelper,
        private val authInterceptor: AuthInterceptor
) {
    companion object {
        private const val FAKE_MAIL_PATTERN = "adaptive_%s_android_%d%s@stepik.org"
        private const val TIMEOUT_IN_SECONDS = 60L
    }

    private val stepikService: StepikService
    private val ratingService: RatingService

    val profile: Observable<ProfileResponse>
        get() = stepikService.profile

    init {
        stepikService = initStepikService()
        ratingService = initRatingService()
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
        okHttpBuilder.setTimeoutsInSeconds(TIMEOUT_IN_SECONDS)
        val retrofit = buildRetrofit(okHttpBuilder.build(), config.ratingHost)

        return retrofit.create(RatingService::class.java)
    }

    private fun initStepikService(): StepikService {
        val okHttpBuilder = OkHttpClient.Builder()
        okHttpBuilder.addInterceptor(authInterceptor)

        okHttpBuilder.setTimeoutsInSeconds(TIMEOUT_IN_SECONDS)
        val retrofit = buildRetrofit(okHttpBuilder.build(), config.host)

        return retrofit.create(StepikService::class.java)
    }

    fun remindPassword(email: String): Completable {
        val encodedEmail = URLEncoder.encode(email, Charsets.UTF_8.name())
        val interceptor = Interceptor { chain ->
            var newRequest = chain.addUserAgent(userAgent)

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
        okHttpBuilder.setTimeoutsInSeconds(TIMEOUT_IN_SECONDS)
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
