package com.segment.analytics;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import okhttp3.Interceptor.Chain;
import okhttp3.Request;
import okhttp3.Request.Builder;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class AnalyticsRequestInterceptorTest {

  @Test
  public void interceptor() throws IOException {
    Chain chain = mock(Chain.class);

    Request request = new Builder().url("http://localhost").build();

    when(chain.request()).thenReturn(request);

    AnalyticsRequestInterceptor interceptor = new AnalyticsRequestInterceptor("writeKey", "userAgent");
    interceptor.intercept(chain);

    ArgumentCaptor<Request> captor = ArgumentCaptor.forClass(Request.class);
    verify(chain).proceed(captor.capture());

    Assert.assertEquals(captor.getValue().headers().size(), 2);
  }
}
