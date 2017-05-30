package org.stepik.android.adaptive.pdd.api;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.stepik.android.adaptive.pdd.Config;
import org.stepik.android.adaptive.pdd.api.oauth.OAuthService;
import org.stepik.android.adaptive.pdd.api.oauth.OAuthResponse;
import org.stepik.android.adaptive.pdd.api.login.SocialManager;
import org.stepik.android.adaptive.pdd.data.SharedPreferenceMgr;
import org.stepik.android.adaptive.pdd.data.model.EnrollmentWrapper;
import org.stepik.android.adaptive.pdd.data.model.RecommendationReaction;
import org.stepik.android.adaptive.pdd.data.model.Submission;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import io.reactivex.Completable;
import io.reactivex.Observable;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public final class API {
    private final static String TAG = "API";
    private final static String HOST = "https://stepik.org/";

    private final static String AUTH_HEADER = "Authorization";

    public final static ReentrantLock authLock = new ReentrantLock();

    private enum TokenType {
        SOCIAL,
        PASSWORD
    }

    private static API instance;


    private TokenType authServiceTokenType;
    private OAuthService authService;

    private final StepikService stepikService;

    private API() {
        stepikService = initStepikService();
    }

    public synchronized static void init() {
        if (instance == null) {
            instance = new API();
        }
    }

    public synchronized static API getInstance() {
        return instance;
    }


    /**
     * Returns new authService with specified token type
     * @param tokenType - SOCIAL or PASSWORD
     * @return new authService
     */
    private OAuthService initAuthService(final TokenType tokenType) {
        final OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        httpClient.addInterceptor(chain ->
                chain.proceed(chain.request().newBuilder().header(AUTH_HEADER,
                        Credentials.basic(
                            tokenType == TokenType.SOCIAL ?
                                    Config.getInstance().getOAuthClientIdSocial() :
                                    Config.getInstance().getOAuthClientId(),
                            tokenType == TokenType.SOCIAL ?
                                    Config.getInstance().getOAuthClientSecretSocial() :
                                    Config.getInstance().getOAuthClientSecret())
                ).build()));

        httpClient.connectTimeout(60, TimeUnit.SECONDS);
        httpClient.readTimeout(60, TimeUnit.SECONDS);

        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API.HOST)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create()).build();

        return retrofit.create(OAuthService.class);
    }

    private synchronized OAuthService getAuthService(final TokenType tokenType) {
        if (authService == null || tokenType != authServiceTokenType) {
            authServiceTokenType = tokenType;
            return authService = initAuthService(tokenType);
        } else {
            return authService;
        }
    }

    public Observable<OAuthResponse> authWithLoginPassword(final String login, final String password) {
        return getAuthService(TokenType.PASSWORD)
                .authWithLoginPassword(Config.getInstance().getGrantType(), login, password)
                .doOnNext(response -> {
                    authLock.lock();
                    SharedPreferenceMgr.getInstance().saveOAuthResponse(response);
                    SharedPreferenceMgr.getInstance().setIsOauthTokenSocial(false);
                    authLock.unlock();
                });
    }

    public Observable<OAuthResponse> authWithNativeCode(final String code, final SocialManager.SocialType type) {
        String codeType = null;
        if (type.needUseAccessTokenInsteadOfCode()) {
            codeType = "access_token";
        }
        return getAuthService(TokenType.SOCIAL).getTokenByNativeCode(
                type.getIdentifier(),
                code,
                Config.getInstance().getGrantTypeSocial(),
                Config.getInstance().getRedirectUri(),
                codeType)
                .doOnNext(response -> {
                    authLock.lock();
                    SharedPreferenceMgr.getInstance().saveOAuthResponse(response);
                    SharedPreferenceMgr.getInstance().setIsOauthTokenSocial(true);
                    authLock.unlock();
                });
    }

    private Call<OAuthResponse> authWithRefreshToken(final String refreshToken) {
        return getAuthService(SharedPreferenceMgr.getInstance().isAuthTokenSocial() ? TokenType.SOCIAL : TokenType.PASSWORD)
                .refreshAccessToken(Config.getInstance().getRefreshGrantType(), refreshToken);
    }

    private StepikService initStepikService() {
        final OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(chain -> {
            Request request = chain.request();

            try {
                authLock.lock();
                OAuthResponse response = SharedPreferenceMgr.getInstance().getOAuthResponse();

                if (response != null) {
                    if (isUpdateNeeded()) {
                        Response<OAuthResponse> oAuthResponse;
                        try {
                            oAuthResponse = authWithRefreshToken(response.getRefreshToken()).execute();
                            response = oAuthResponse.body();
                        } catch (IOException e) {
                            e.printStackTrace();
                            return chain.proceed(request);
                        }

                        if (response == null || !oAuthResponse.isSuccessful()) {
                            if (oAuthResponse.code() == 401) {
                                // todo logout
                            }
                            return chain.proceed(request);
                        }

                        SharedPreferenceMgr.getInstance().saveOAuthResponse(response);
                    }
                    request = request.newBuilder()
                            .addHeader(AUTH_HEADER, response.getTokenType() + " " + response.getAccessToken())
                            .build();
                }
            } finally {
                authLock.unlock();
            }

            return chain.proceed(request);
        });

        httpClient.connectTimeout(60, TimeUnit.SECONDS);
        httpClient.readTimeout(60, TimeUnit.SECONDS);

        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API.HOST)
                .client(httpClient.build())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create()).build();

        return retrofit.create(StepikService.class);
    }

    private boolean isUpdateNeeded() {
        final long expireAt = SharedPreferenceMgr.getInstance().getAuthResponseDeadline();
        return DateTime.now(DateTimeZone.UTC).getMillis() > expireAt;
    }

    public Completable remindPassword(final String email) {
        return getAuthService(SharedPreferenceMgr.getInstance().isAuthTokenSocial() ? TokenType.SOCIAL : TokenType.PASSWORD)
                .remindPassword(email);
    }

    public Completable joinCourse(final long course) {
        return stepikService.joinCourse(new EnrollmentWrapper(course));
    }

    public Observable<RecommendationsResponse> getNextRecommendations() {
        return stepikService.getNextRecommendations(Config.getInstance().getCourseId());
    }

    public Observable<StepsResponse> getSteps(final long lesson) {
        return stepikService.getSteps(lesson);
    }

    public Observable<AttemptResponse> createAttempt(final long step) {
        return stepikService.createAttempt(new AttemptRequest(step));
    }

    public Observable<AttemptResponse> getAttempts(final long step) {
        return stepikService.getAttempts(step, SharedPreferenceMgr.getInstance().getProfileId());
    }

    public Completable createSubmission(final Submission submission) {
        return stepikService.createSubmission(new SubmissionRequest(submission));
    }

    public Observable<SubmissionResponse> getSubmissions(final long attempt) {
        return stepikService.getSubmissions(attempt, "desc");
    }

    public Observable<ProfileResponse> getProfile() {
        return stepikService.getProfile();
    }

    public Completable createReaction(final RecommendationReaction reaction) {
        return stepikService.createRecommendationReaction(new RecommendationReactionsRequest(reaction));
    }

    public Observable<LessonsResponse> getLessons(final long lesson) {
        return stepikService.getLessons(lesson);
    }
}
