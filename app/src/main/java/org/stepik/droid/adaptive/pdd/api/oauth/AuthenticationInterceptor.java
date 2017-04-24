package org.stepik.droid.adaptive.pdd.api.oauth;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthenticationInterceptor implements Interceptor {
    private final String authToken;

    public AuthenticationInterceptor(final String token) {
        this.authToken = token;
    }

    @Override
    public Response intercept(final Chain chain) throws IOException {
        final Request original = chain.request();
        final Request.Builder builder = original.newBuilder()
                .header("Authorization", authToken);
        final Request request = builder.build();
        return chain.proceed(request);
    }
}
