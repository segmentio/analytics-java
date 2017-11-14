package com.segment.analytics;

import okhttp3.Credentials;
import retrofit.RequestInterceptor;

import javax.annotation.Nonnull;

class AnalyticsRequestInterceptor implements RequestInterceptor {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String USER_AGENT_HEADER = "User-Agent";

    private final @Nonnull String writeKey;
    private final @Nonnull String userAgent;

    AnalyticsRequestInterceptor(@Nonnull String writeKey, @Nonnull String userAgent) {
        this.writeKey = writeKey;
        this.userAgent = userAgent;
    }

    @Override
    public void intercept(RequestFacade request) {
        request.addHeader(AUTHORIZATION_HEADER, Credentials.basic(writeKey, ""));
        request.addHeader(USER_AGENT_HEADER, userAgent);
    }
}
