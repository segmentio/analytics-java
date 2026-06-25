package com.segment.analytics;

import com.segment.analytics.internal.AnalyticsClient;
import jakarta.annotation.Nonnull;
import java.io.IOException;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.Request;

class AnalyticsRequestInterceptor implements Interceptor {
  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String USER_AGENT_HEADER = "User-Agent";
  private static final String RETRY_COUNT_HEADER = "X-Retry-Count";

  private final @Nonnull String writeKey;
  private final @Nonnull String userAgent;

  AnalyticsRequestInterceptor(@Nonnull String writeKey, @Nonnull String userAgent) {
    this.writeKey = writeKey;
    this.userAgent = userAgent;
  }

  @Override
  public okhttp3.Response intercept(Chain chain) throws IOException {
    Request request = chain.request();
    Request.Builder builder =
        request
            .newBuilder()
            .addHeader(AUTHORIZATION_HEADER, Credentials.basic(writeKey, ""))
            .addHeader(USER_AGENT_HEADER, userAgent);

    Integer retryCount = AnalyticsClient.RETRY_COUNT.get();
    if (retryCount != null) {
      builder.addHeader(RETRY_COUNT_HEADER, retryCount.toString());
    }

    return chain.proceed(builder.build());
  }
}
