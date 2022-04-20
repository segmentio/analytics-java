package com.segment.analytics;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.segment.analytics.TestUtils.MessageBuilderTest;
import com.segment.analytics.internal.AnalyticsClient;
import com.segment.analytics.messages.Message;
import com.segment.analytics.messages.MessageBuilder;
import com.squareup.burst.BurstJUnit4;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Before;
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

  @Test
  public void offerIsDispatched(MessageBuilderTest builder) {
    MessageBuilder messageBuilder = builder.get().userId("dummy");
    Message message = messageBuilder.build();
    when(messageTransformer.transform(messageBuilder)).thenReturn(true);
    when(messageInterceptor.intercept(any(Message.class))).thenReturn(message);

    analytics.offer(messageBuilder);

    verify(messageTransformer).transform(messageBuilder);
    verify(messageInterceptor).intercept(any(Message.class));
    verify(client).offer(message);
  }

  @Test
  public void threadSafeTest(MessageBuilderTest builder)
      throws NoSuchFieldException, IllegalAccessException, InterruptedException {
    // we want to test if msgs get lost during a multithreaded env
    Analytics analytics = Analytics.builder("testWriteKeyForIssue321").build();
    // So we just want to spy on the client of an Analytics  object created normally
    Field clientField = analytics.getClass().getDeclaredField("client");
    clientField.setAccessible(true);
    AnalyticsClient spy = spy((AnalyticsClient) clientField.get(analytics));
    clientField.set(analytics, spy);

    // we are going to run this test for a specific amount of seconds
    int millisRunning = 200;
    LocalDateTime initialTime = LocalDateTime.now();
    LocalDateTime now;

    // and a set number of threads will be using the library
    ExecutorService service = Executors.newFixedThreadPool(20);
    AtomicInteger counter = new AtomicInteger();

    MessageBuilder messageBuilder = builder.get().userId("jorgen25");

    do {
      service.submit(
          () -> {
            analytics.enqueue(messageBuilder);
            counter.incrementAndGet();
          });
      now = LocalDateTime.now();
    } while (initialTime.until(now, ChronoUnit.MILLIS) < millisRunning);

    service.shutdown();
    while (!service.isShutdown() || !service.isTerminated()) {}

    verify(spy, times(counter.get())).enqueue(any(Message.class));
  }
}
