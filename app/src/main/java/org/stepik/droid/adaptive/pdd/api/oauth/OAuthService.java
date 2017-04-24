package org.stepik.droid.adaptive.pdd.api.oauth;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface OAuthService {

    @FormUrlEncoded
    @POST("oauth2/token/")
    Observable<OAuthResponse> getAccessTokenByCode(
            @Field("grant_type") final String grant_type,
            @Field("code") final String code,
            @Field("redirect_uri") final String redirect_uri
    );

    @FormUrlEncoded
    @POST("oauth2/token/")
    Call<OAuthResponse> refreshAccessToken(
            @Field("grant_type") final String grant_type,
            @Field("refresh_token") final String refresh_token
    );


}
