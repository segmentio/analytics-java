package com.segment.analytics;

import com.segment.analytics.messages.*;
import org.junit.Test;
import retrofit.RequestInterceptor;
import retrofit.RequestInterceptor.RequestFacade;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class AnalyticsRequestInterceptorTest {
  @Test public void interceptor() {
    RequestFacade requestFacade = mock(RequestFacade.class);
    AnalyticsRequestInterceptor interceptor = new AnalyticsRequestInterceptor("writeKey", "userAgent");

    interceptor.intercept(requestFacade);

    verify(requestFacade).addHeader("Authorization", "Basic d3JpdGVLZXk6");
    verify(requestFacade).addHeader("User-Agent", "userAgent");
  }
}
