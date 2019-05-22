package com.segment.analytics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.jakewharton.retrofit.Ok3Client;
import com.segment.analytics.TestUtils.MessageBuilderTest;
import com.segment.analytics.internal.AnalyticsClient;
import com.segment.analytics.messages.Message;
import com.segment.analytics.messages.MessageBuilder;
import com.segment.analytics.messages.TrackMessage;
import com.squareup.burst.BurstJUnit4;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

@RunWith(BurstJUnit4.class)
public class AnalyticsTest {
  @Mock AnalyticsClient client;
  @Mock Log log;
  @Mock MessageTransformer messageTransformer;
  @Mock MessageInterceptor messageInterceptor;
  Analytics analytics;

  @Before
  public void setUp() {
    initMocks(this);

    analytics =
        new Analytics(
            client,
            Collections.singletonList(messageTransformer),
            Collections.singletonList(messageInterceptor),
            log);
  }

  @Test
  public void enqueueIsDispatched(MessageBuilderTest builder) {
    MessageBuilder messageBuilder = builder.get().userId("prateek");
    Message message = messageBuilder.build();
    when(messageTransformer.transform(messageBuilder)).thenReturn(true);
    when(messageInterceptor.intercept(any(Message.class))).thenReturn(message);

    analytics.enqueue(messageBuilder);

    verify(messageTransformer).transform(messageBuilder);
    verify(messageInterceptor).intercept(any(Message.class));
    verify(client).enqueue(message);
  }

  @Test
  public void doesNotEnqueueWhenTransformerReturnsFalse(MessageBuilderTest builder) {
    MessageBuilder messageBuilder = builder.get().userId("prateek");
    when(messageTransformer.transform(messageBuilder)).thenReturn(false);

    analytics.enqueue(messageBuilder);

    verify(messageTransformer).transform(messageBuilder);
    verify(messageInterceptor, never()).intercept(any(Message.class));
    verify(client, never()).enqueue(any(Message.class));
  }

  @Test
  public void doesNotEnqueueWhenInterceptorReturnsNull(MessageBuilderTest builder) {
    MessageBuilder messageBuilder = builder.get().userId("prateek");
    when(messageTransformer.transform(messageBuilder)).thenReturn(true);

    analytics.enqueue(messageBuilder);

    verify(messageTransformer).transform(messageBuilder);
    verify(messageInterceptor).intercept(any(Message.class));
    verify(client, never()).enqueue(any(Message.class));
  }

  @Test
  public void shutdownIsDispatched() {
    analytics.shutdown();

    verify(client).shutdown();
  }

  @Test
  public void flushIsDispatched() {
    analytics.flush();

    verify(client).flush();
  }

  @Rule public MockWebServer server = new MockWebServer();

  @Test
  public void testClient() throws InterruptedException {
    server.enqueue(new MockResponse().setBody("hello, world!"));

    Analytics analytics =
        new Analytics.Builder("writeKey")
            .client(
                new Ok3Client(
                    new OkHttpClient.Builder()
                        .connectTimeout(15, TimeUnit.SECONDS)
                        .readTimeout(15, TimeUnit.SECONDS)
                        .writeTimeout(15, TimeUnit.SECONDS)
                        .addInterceptor(
                            new Interceptor() {
                              @Override
                              public Response intercept(Chain chain) throws IOException {
                                Request newRequest =
                                    chain
                                        .request()
                                        .newBuilder()
                                        .url(server.url("/v2/import"))
                                        .build();
                                return chain.proceed(newRequest);
                              }
                            })
                        .build()))
            .build();

    analytics.enqueue(TrackMessage.builder("test").userId("prateek"));
    analytics.flush();

    RecordedRequest request1 = server.takeRequest();
    assertThat(request1.getPath()).isEqualTo("/v2/import");
  }
}
