package com.segment.analytics.internal;

import com.segment.analytics.Log;
import com.segment.analytics.TestUtils.MessageBuilderTest;
import com.segment.analytics.internal.http.SegmentService;
import com.segment.analytics.messages.Message;
import com.segment.analytics.messages.TrackMessage;
import com.squareup.burst.BurstJUnit4;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(BurstJUnit4.class) public class AnalyticsClientTest {

  AnalyticsClient client;
  @Mock BlockingQueue<Message> messageQueue;
  @Mock SegmentService segmentService;
  @Mock Log log;
  @Mock ThreadFactory threadFactory;
  @Mock ExecutorService networkExecutor;
  @Mock Thread thread;

  @Before public void setUp() {
    initMocks(this);

    when(threadFactory.newThread(any(Runnable.class))).thenReturn(thread);
  }

  @Test public void enqueueAddsToQueue(MessageBuilderTest builder) {
    client = new AnalyticsClient(messageQueue, segmentService, 50, TimeUnit.HOURS.toMillis(1), log,
        threadFactory, networkExecutor);

    Message message = builder.get().userId("prateek").build();

    client.enqueue(message);

    verify(messageQueue).add(message);
  }

  @Test public void shutdown() {
    client = new AnalyticsClient(messageQueue, segmentService, 50, TimeUnit.HOURS.toMillis(1), log,
        threadFactory, networkExecutor);

    client.shutdown();

    verify(thread).interrupt();
    verify(messageQueue).clear();
    verify(networkExecutor).shutdown();
  }

  @Test public void flushInsertsPoison() {
    client = new AnalyticsClient(messageQueue, segmentService, 50, TimeUnit.HOURS.toMillis(1), log,
        threadFactory, networkExecutor);

    client.flush();

    verify(messageQueue).add(FlushMessage.POISON);
  }

  @Test public void flushSubmitsToExecutor() {
    threadFactory = Executors.defaultThreadFactory();
    messageQueue = new LinkedBlockingQueue<>();
    client = new AnalyticsClient(messageQueue, segmentService, 50, TimeUnit.HOURS.toMillis(1), log,
        threadFactory, networkExecutor);

    TrackMessage first = TrackMessage.builder("foo").userId("bar").build();
    TrackMessage second = TrackMessage.builder("qaz").userId("qux").build();
    client.enqueue(first);
    client.enqueue(second);
    client.flush();

    while (messageQueue.size() > 0) {
      // wait to make sure looper reads the flush message
    }

    final ArgumentCaptor<Runnable> runnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);
    verify(networkExecutor, Mockito.timeout(1 * 1000)).submit(runnableArgumentCaptor.capture());

    final BatchUploadTask task = (BatchUploadTask) runnableArgumentCaptor.getValue();
    assertThat(task.batch.batch()).containsExactly(first, second);
  }
}
