package com.segment.analytics;

import com.segment.analytics.TestUtils.MessageBuilderTest;
import com.segment.analytics.internal.AnalyticsClient;
import com.segment.analytics.messages.Message;
import com.segment.analytics.messages.MessageBuilder;
import com.squareup.burst.BurstJUnit4;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(BurstJUnit4.class) public class AnalyticsTest {
  @Mock AnalyticsClient client;
  @Mock Log log;
  @Mock MessageTransformer messageTransformer;
  @Mock MessageInterceptor messageInterceptor;
  Analytics analytics;

  @Before public void setUp() {
    initMocks(this);

    analytics = new Analytics(client, Collections.singletonList(messageTransformer),
        Collections.singletonList(messageInterceptor), log);
  }

  @Test public void enqueueIsDispatched(MessageBuilderTest builder) {
    MessageBuilder messageBuilder = builder.get().userId("prateek");
    Message message = messageBuilder.build();
    when(messageTransformer.transform(messageBuilder)).thenReturn(true);
    when(messageInterceptor.intercept(any(Message.class))).thenReturn(message);

    analytics.enqueue(messageBuilder);

    verify(messageTransformer).transform(messageBuilder);
    verify(messageInterceptor).intercept(any(Message.class));
    verify(client).enqueue(message);
  }

  @Test public void doesNotEnqueueWhenTransformerReturnsFalse(MessageBuilderTest builder) {
    MessageBuilder messageBuilder = builder.get().userId("prateek");
    when(messageTransformer.transform(messageBuilder)).thenReturn(false);

    analytics.enqueue(messageBuilder);

    verify(messageTransformer).transform(messageBuilder);
    verify(messageInterceptor, never()).intercept(any(Message.class));
    verify(client, never()).enqueue(any(Message.class));
  }

  @Test public void doesNotEnqueueWhenInterceptorReturnsNull(MessageBuilderTest builder) {
    MessageBuilder messageBuilder = builder.get().userId("prateek");
    when(messageTransformer.transform(messageBuilder)).thenReturn(true);

    analytics.enqueue(messageBuilder);

    verify(messageTransformer).transform(messageBuilder);
    verify(messageInterceptor).intercept(any(Message.class));
    verify(client, never()).enqueue(any(Message.class));
  }

  @Test public void shutdownIsDispatched() {
    analytics.shutdown();

    verify(client).shutdown();
  }

  @Test public void flushIsDispatched() {
    analytics.flush();

    verify(client).flush();
  }
}
