package org.stepik.android.adaptive.pdd.api;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.webkit.CookieManager;

import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.stepik.android.adaptive.pdd.Config;
import org.stepik.android.adaptive.pdd.api.oauth.EmptyAuthService;
import org.stepik.android.adaptive.pdd.api.oauth.OAuthService;
import org.stepik.android.adaptive.pdd.api.oauth.OAuthResponse;
import org.stepik.android.adaptive.pdd.api.login.SocialManager;
import org.stepik.android.adaptive.pdd.core.LogoutHelper;
import org.stepik.android.adaptive.pdd.core.ScreenManager;
import org.stepik.android.adaptive.pdd.data.SharedPreferenceMgr;
import org.stepik.android.adaptive.pdd.data.model.EnrollmentWrapper;
import org.stepik.android.adaptive.pdd.data.model.RecommendationReaction;
import org.stepik.android.adaptive.pdd.data.model.RegistrationUser;
import org.stepik.android.adaptive.pdd.data.model.Submission;
import org.stepik.android.adaptive.pdd.util.AppConstants;

import java.io.IOException;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import io.reactivex.Completable;
import io.reactivex.Observable;
import okhttp3.Credentials;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
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

    private final static int TIMEOUT_IN_SECONDS = 60;

    public final static ReentrantLock authLock = new ReentrantLock();

    private enum TokenType {
        SOCIAL,
        PASSWORD
    }

    private static API instance;


    private TokenType authServiceTokenType;
    private OAuthService authService;

    private final StepikService stepikService;
    private final EmptyAuthService emptyAuthService;

    private API() {
        stepikService = initStepikService();
        emptyAuthService = initEmptyAuthService();
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
        final OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder();

        okHttpBuilder.addInterceptor(chain ->
                chain.proceed(addUserAgentTo(chain).newBuilder().header(AppConstants.authorizationHeaderName,
                        Credentials.basic(
                            tokenType == TokenType.SOCIAL ?
                                    Config.getInstance().getOAuthClientIdSocial() :
                                    Config.getInstance().getOAuthClientId(),
                            tokenType == TokenType.SOCIAL ?
                                    Config.getInstance().getOAuthClientSecretSocial() :
                                    Config.getInstance().getOAuthClientSecret())
                ).build()));

        setTimeout(okHttpBuilder, TIMEOUT_IN_SECONDS);

        final Retrofit retrofit = buildRetrofit(okHttpBuilder.build());

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

    public Observable<OAuthResponse> authWithCode(final String code) {
        return getAuthService(TokenType.SOCIAL).getTokenByCode(
                Config.getInstance().getGrantTypeSocial(), code, Config.getInstance().getRedirectUri()
        ).doOnNext(response -> {
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

    public Observable<Response<RegistrationResponse>> createAccount(final String firstName, final String lastName,
                                                          final String email, final String password) {
        OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder();
        okHttpBuilder.addNetworkInterceptor(chain -> {
            Request newRequest = addUserAgentTo(chain);
            updateCookieForBaseUrl();
            String cookies = CookieManager.getInstance().getCookie(API.HOST);
            if (cookies == null)
                return chain.proceed(newRequest);

            String csrftoken = getCsrfTokenFromCookies(cookies);
            Request.Builder requestBuilder = newRequest
                    .newBuilder()
                    .addHeader(AppConstants.refererHeaderName, API.HOST)
                    .addHeader(AppConstants.csrfTokenHeaderName, csrftoken)
                    .addHeader(AppConstants.cookieHeaderName, cookies);
            newRequest = requestBuilder.build();
            return chain.proceed(newRequest);
        });
        setTimeout(okHttpBuilder, TIMEOUT_IN_SECONDS);

        Retrofit notLogged = buildRetrofit(okHttpBuilder.build());

        OAuthService tmpService = notLogged.create(OAuthService.class);

        return tmpService.createAccount(new UserRegistrationRequest(new RegistrationUser(firstName, lastName, email, password)));
    }

    private StepikService initStepikService() {
        final OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder();
        okHttpBuilder.addInterceptor(chain -> {
            Request request = addUserAgentTo(chain);

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
                                LogoutHelper.logout(ScreenManager.getInstance()::showOnboardingScreen);
                            }
                            return chain.proceed(request);
                        }

                        SharedPreferenceMgr.getInstance().saveOAuthResponse(response);
                    }
                    request = request.newBuilder()
                            .addHeader(AppConstants.authorizationHeaderName, response.getTokenType() + " " + response.getAccessToken())
                            .build();
                }
            } finally {
                authLock.unlock();
            }

            return chain.proceed(request);
        });

        setTimeout(okHttpBuilder, TIMEOUT_IN_SECONDS);
        final Retrofit retrofit = buildRetrofit(okHttpBuilder.build());

        return retrofit.create(StepikService.class);
    }

    private EmptyAuthService initEmptyAuthService() {
        final OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder();
        setTimeout(okHttpBuilder, TIMEOUT_IN_SECONDS);

        final Retrofit retrofit = buildRetrofit(okHttpBuilder.build());

        return retrofit.create(EmptyAuthService.class);
    }

    private boolean isUpdateNeeded() {
        final long expireAt = SharedPreferenceMgr.getInstance().getAuthResponseDeadline();
        return DateTime.now(DateTimeZone.UTC).getMillis() > expireAt;
    }

    public Completable remindPassword(final String email) {
        String encodedEmail = URLEncoder.encode(email);
        Interceptor interceptor = chain -> {
            Request newRequest = addUserAgentTo(chain);

            List<HttpCookie> cookies = getCookiesForBaseUrl();
            if (cookies == null)
                return chain.proceed(newRequest);
            String csrftoken = null;
            String sessionId = null;
            for (HttpCookie item : cookies) {
                if (item.getName() != null && item.getName().equals(AppConstants.csrfTokenCookieName)) {
                    csrftoken = item.getValue();
                    continue;
                }
                if (item.getName() != null && item.getName().equals(AppConstants.sessionCookieName)) {
                    sessionId = item.getValue();
                }
            }

            String cookieResult = AppConstants.csrfTokenCookieName + "=" + csrftoken + "; " + AppConstants.sessionCookieName + "=" + sessionId;
            if (csrftoken == null) return chain.proceed(newRequest);
            HttpUrl url = newRequest
                    .url()
                    .newBuilder()
                    .addQueryParameter("csrfmiddlewaretoken", csrftoken)
                    .addQueryParameter("csrfmiddlewaretoken", csrftoken)
                    .build();
            newRequest = newRequest.newBuilder()
                    .addHeader("referer", API.HOST)
                    .addHeader("X-CSRFToken", csrftoken)
                    .addHeader("Cookie", cookieResult)
                    .url(url)
                    .build();
            return chain.proceed(newRequest);
        };
        OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder();
        okHttpBuilder.addNetworkInterceptor(interceptor);
//        okHttpBuilder.addNetworkInterceptor(this.stethoInterceptor);
        setTimeout(okHttpBuilder, TIMEOUT_IN_SECONDS);
        Retrofit notLogged = buildRetrofit(okHttpBuilder.build());

        EmptyAuthService tempService = notLogged.create(EmptyAuthService.class);
        return tempService.remindPassword(encodedEmail);
    }

    public Completable joinCourse(final long course) {
        return stepikService.joinCourse(new EnrollmentWrapper(course));
    }

    public Observable<RecommendationsResponse> getNextRecommendations(final int count) {
        return stepikService.getNextRecommendations(Config.getInstance().getCourseId(), count);
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


    private void setTimeout(OkHttpClient.Builder builder, int seconds) {
        builder.connectTimeout(seconds, TimeUnit.SECONDS);
        builder.readTimeout(seconds, TimeUnit.SECONDS);
    }

    @Nullable
    private List<HttpCookie> getCookiesForBaseUrl() throws IOException {
        String lang = Locale.getDefault().getLanguage();
        retrofit2.Response response = emptyAuthService.getStepicForFun(lang).execute();
        Headers headers = response.headers();
        java.net.CookieManager cookieManager = new java.net.CookieManager();
        URI myUri;
        try {
            myUri = new URI(API.HOST);
        } catch (URISyntaxException e) {
            return null;
        }
        cookieManager.put(myUri, headers.toMultimap());
        return cookieManager.getCookieStore().get(myUri);
    }

    private void updateCookieForBaseUrl() throws IOException {
        String lang = Locale.getDefault().getLanguage();
        retrofit2.Response response = emptyAuthService.getStepicForFun(lang).execute();

        List<String> setCookieHeaders = response.headers().values(AppConstants.setCookieHeaderName);
        if (!setCookieHeaders.isEmpty()) {
            for (String value : setCookieHeaders) {
                if (value != null) {
                    CookieManager.getInstance().setCookie(API.HOST, value); //set-cookie is not empty
                }
            }
        }
    }

    @Nullable
    private String tryGetCsrfFromOnePair(String keyValueCookie) {
        List<HttpCookie> cookieList = HttpCookie.parse(keyValueCookie);
        for (HttpCookie item : cookieList) {
            if (item.getName() != null && item.getName().equals(AppConstants.csrfTokenCookieName)) {
                return item.getValue();
            }
        }
        return null;
    }

    @NonNull
    private String getCsrfTokenFromCookies(String cookies) {
        String csrftoken = null;
        String[] cookiePairs = cookies.split(";");
        for (String cookieItem : cookiePairs) {
            csrftoken = tryGetCsrfFromOnePair(cookieItem);
            if (csrftoken != null) {
                break;
            }
        }
        if (csrftoken == null) {
            csrftoken = "";
        }
        return csrftoken;
    }

    private Request addUserAgentTo(Interceptor.Chain chain) {
        return chain
                .request()
                .newBuilder()
                .header(AppConstants.userAgentName, UserAgentProvider.provideUserAgent())
                .build();
    }

    private Retrofit buildRetrofit(OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl(API.HOST)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
    }

    public Uri getUriForSocialAuth(SocialManager.SocialType type) {
        String socialIdentifier = type.getIdentifier();
        String url = API.HOST + "accounts/" + socialIdentifier + "/login?next=/oauth2/authorize/?" + Uri.encode("client_id=" + Config.getInstance().getOAuthClientIdSocial() + "&response_type=code");
        return Uri.parse(url);
    }
}
