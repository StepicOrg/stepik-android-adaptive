package org.stepik.android.adaptive.pdd.api;

import com.vk.sdk.VKSdk;

import org.stepik.android.adaptive.pdd.Config;
import org.stepik.android.adaptive.pdd.api.oauth.AuthenticationInterceptor;
import org.stepik.android.adaptive.pdd.api.oauth.OAuthService;
import org.stepik.android.adaptive.pdd.api.oauth.OAuthResponse;
import org.stepik.android.adaptive.pdd.api.login.SocialManager;
import org.stepik.android.adaptive.pdd.data.SharedPreferenceMgr;
import org.stepik.android.adaptive.pdd.data.model.EnrollmentWrapper;
import org.stepik.android.adaptive.pdd.data.model.RecommendationReaction;
import org.stepik.android.adaptive.pdd.data.model.Submission;

import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public final class API {
    private final static String TAG = "API";
    private static final String HOST = "https://stepik.org/";

    public enum TokenType {
        SOCIAL,
        PASSWORD,
        DEFAULT,
        REFRESH
    }

    private static API instance;


    private final Retrofit.Builder builder =
            new Retrofit.Builder()
            .baseUrl(API.HOST)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create());

    private OAuthService authService;
    private StepikService stepikService;

    public void initServices(final TokenType tokenType) {
        final OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        final String token = getAuthToken(tokenType);
        if (token != null) {
            final AuthenticationInterceptor interceptor = new AuthenticationInterceptor(token);
            httpClient.addInterceptor(interceptor);
        }

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClient.addInterceptor(logging);
        httpClient.connectTimeout(60, TimeUnit.SECONDS);
        httpClient.readTimeout(60, TimeUnit.SECONDS);

        final Retrofit retrofit = builder.client(httpClient.build()).build();

        this.authService = retrofit.create(OAuthService.class);
        this.stepikService = retrofit.create(StepikService.class);
    }

    private String getAuthToken(final TokenType tokenType) {
        final OAuthResponse response = SharedPreferenceMgr.getInstance().getOAuthResponse();
        final long expire = SharedPreferenceMgr.getInstance().getLong(SharedPreferenceMgr.OAUTH_RESPONSE_DEADLINE);
        switch (tokenType) {
            case PASSWORD:
                return Credentials.basic(
                        Config.getInstance().getOAuthClientId(),
                        Config.getInstance().getOAuthClientSecret());
            case SOCIAL:
                return Credentials.basic(
                        Config.getInstance().getOAuthClientIdSocial(),
                        Config.getInstance().getOAuthClientSecretSocial());
            case REFRESH:
                if (response != null)
                    return response.getTokenType() + " " + response.getAccessToken();
            default:
                if (response != null && expire > System.currentTimeMillis()) {
                    return response.getTokenType() + " " + response.getAccessToken();
                } else {
                    return null;
                }
        }
    }

    public synchronized static void init() {
        if (instance == null) {
            instance = new API();
        }
    }

    public synchronized static API getInstance() {
        return instance;
    }

    public Observable<OAuthResponse> authWithLoginPassword(final String login, final String password) {
        initServices(TokenType.PASSWORD);
        return authService.authWithLoginPassword(Config.getInstance().getGrantType(), login, password);
    }

    public Observable<OAuthResponse> authWithCode(final String code) {
        return authService.getAccessTokenByCode(Config.getInstance().getGrantTypeSocial(), code, Config.getInstance().getRedirectUri());
    }

    public Call<OAuthResponse> authWithRefreshToken(final String refresh_token) {
        initServices(TokenType.REFRESH);
        return authService.refreshAccessToken(Config.getInstance().getRefreshGrantType(), refresh_token);
    }

    public Observable<OAuthResponse> authWithNativeCode(final String code, final SocialManager.SocialType type) {
        initServices(TokenType.SOCIAL);
        String codeType = null;
        if (type.needUseAccessTokenInsteadOfCode()) {
            codeType = "access_token";
        }
        return authService.getTokenByNativeCode(
                type.getIdentifier(),
                code,
                Config.getInstance().getGrantTypeSocial(),
                Config.getInstance().getRedirectUri(),
                codeType);
    }

    public Completable remindPassword(final String email) {
        initServices(TokenType.DEFAULT);
        return authService.remindPassword(email);
    }

    public void updateAuthState(final OAuthResponse response) {
        if (response == null) {
            SharedPreferenceMgr.getInstance().removeProfile();
            VKSdk.logout();
        } else {
            SharedPreferenceMgr.getInstance().saveOAuthResponse(response);
            initServices(TokenType.DEFAULT);
        }
    }

    public Completable joinCourse(final long course) {
        return stepikService.joinCourse(new EnrollmentWrapper(course));
    }

    public Observable<RecommendationsResponse> getNextRecommendations() {
        return stepikService.getNextRecommendations(Config.getInstance().getCourseId());
    }

    public Call<StepsResponse> getSteps(final long lesson) {
        return stepikService.getSteps(lesson);
    }

    public Observable<AttemptResponse> createAttempt(final long step) {
        return stepikService.createAttempt(new AttemptRequest(step));
    }

    public Observable<AttemptResponse> getAttempts(final long step) {
        return stepikService.getAttempts(step, SharedPreferenceMgr.getInstance().getLong(SharedPreferenceMgr.PROFILE_ID));
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
}
