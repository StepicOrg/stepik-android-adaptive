package org.stepik.android.adaptive.pdd.api.oauth;

public class OAuthResponse {
    private String refresh_token;
    private long expires_in;
    private String scope;
    private String access_token;
    private String token_type;
    private String error;
    private String error_description;

    public String getRefreshToken() {
        return refresh_token;
    }

    public long getExpiresIn() {
        return expires_in;
    }

    public String getScope() {
        return scope;
    }

    public String getAccessToken() {
        return access_token;
    }

    public String getTokenType() {
        return token_type;
    }

    public String getError() {
        return error;
    }

    public String getErrorDescription() {
        return error_description;
    }
}