package com.segment.analytics;

import static org.junit.Assert.assertThat;

import java.io.IOException;
import okhttp3.Connection;
import okhttp3.Interceptor.Chain;
import okhttp3.Request;
import okhttp3.Response;
import org.hamcrest.core.Is;
import org.junit.Test;
import org.mockito.Mock;

public class AnalyticsRequestInterceptorTest {
  @Mock private Connection mockConnection;

  @Test
  public void testInterceptor() throws IOException {
    AnalyticsRequestInterceptor interceptor = new AnalyticsRequestInterceptor("userAgent");

    final Request request = new Request.Builder().url("https://api.segment.io").get().build();

    Chain chain =
        new ChainAdapter(request, mockConnection) {
          @Override
          public Response proceed(Request request) throws IOException {
            assertThat(request.header("User-Agent"), Is.is("userAgent"));
            return null;
          }
        };

    interceptor.intercept(chain);
  }
}
