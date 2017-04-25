package org.stepik.droid.adaptive.pdd.api;

import org.stepik.droid.adaptive.pdd.Config;
import org.stepik.droid.adaptive.pdd.api.oauth.AuthenticationInterceptor;
import org.stepik.droid.adaptive.pdd.api.oauth.OAuthService;
import org.stepik.droid.adaptive.pdd.api.oauth.OAuthResponse;
import org.stepik.droid.adaptive.pdd.data.SharedPreferenceMgr;
import org.stepik.droid.adaptive.pdd.data.model.RecommendationReaction;
import org.stepik.droid.adaptive.pdd.data.model.Submission;

import java.util.concurrent.TimeUnit;

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

    private static final String CLIENT_ID = "8030bQmbcsVo3j70ImSe1x4jujlZr9Lm9FOwZ5Pb";
    private static final String CLIENT_SECRET = "1AphQOnuKmM4ROsNAfvo3GZoeUfSwEUsOfvDmibcBygd4IxAGQHbtPmZj3t6Yjr4X8Iz1dO231lrstzooofa07TliQxWljDlrlduuSNgD2BzhCyqxsOX0LWvxwZNVslN";
    public static final String REDIRECT_URI = "http://test/success/";

    private static final String HOST = "https://stepik.org/";

    public static final String AUTH_CODE = "code";
    public static final String REFRESH_GRANT_TYPE = "refresh_token";
    public static final String AUTH_CODE_GRANT_TYPE = "authorization_code";

    private static API instance;


    private final Retrofit.Builder builder =
            new Retrofit.Builder()
            .baseUrl(API.HOST)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create());

    private OAuthService authService;
    private StepikService stepikService;

    private API() {
        initServices();
    }

    private void initServices() {
        final OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        final AuthenticationInterceptor interceptor = new AuthenticationInterceptor(getAuthToken());
        httpClient.addInterceptor(interceptor);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClient.addInterceptor(logging);
        httpClient.connectTimeout(60, TimeUnit.SECONDS);
        httpClient.readTimeout(60, TimeUnit.SECONDS);

        final Retrofit retrofit = builder.client(httpClient.build()).build();

        this.authService = retrofit.create(OAuthService.class);
        this.stepikService = retrofit.create(StepikService.class);
    }

    private String getAuthToken() {
        final OAuthResponse response = SharedPreferenceMgr.getInstance().getOAuthResponse();
        final long expire = SharedPreferenceMgr.getInstance().getLong(SharedPreferenceMgr.OAUTH_RESPONSE_DEADLINE);
        if (response != null && expire > System.currentTimeMillis()) {
            return response.getTokenType() + " " + response.getAccessToken();
        } else {
            return Credentials.basic(API.CLIENT_ID, API.CLIENT_SECRET);
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

    public static String getAuthURL() {
        return API.HOST + "oauth2/authorize/?response_type=code&client_id="
                + API.CLIENT_ID + "&redirect_uri=" + API.REDIRECT_URI;
    }

    public Observable<OAuthResponse> authWithCode(final String code) {
        return authService.getAccessTokenByCode(API.AUTH_CODE_GRANT_TYPE, code, API.REDIRECT_URI);
    }

    public Call<OAuthResponse> authWithRefreshToken(final String refresh_token) {
        return authService.refreshAccessToken(API.REFRESH_GRANT_TYPE, refresh_token);
    }

    public void updateAuthState(final OAuthResponse response) {
        SharedPreferenceMgr.getInstance().saveOAuthResponse(response);
        initServices();
    }

    public Call<RecommendationsResponse> getNextRecommendations() {
        return stepikService.getNextRecommendations(Config.COURSE_ID);
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

    public Observable<SubmissionResponse> createSubmission(final Submission submission) {
        return stepikService.createSubmission(new SubmissionRequest(submission));
    }

    public Observable<SubmissionResponse> getSubmissions(final long attempt) {
        return stepikService.getSubmissions(attempt, "desc");
    }

    public Observable<ProfileResponse> getProfile() {
        return stepikService.getProfile();
    }

    public Call<RecommendationReactionsResponse> createReaction(final RecommendationReaction reaction) {
        return stepikService.createRecommendationReaction(new RecommendationReactionsRequest(reaction));
    }
}
