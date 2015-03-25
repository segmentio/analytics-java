package com.segment.analytics.internal;

import com.segment.analytics.Log;
import com.segment.analytics.TestUtils.MessageBuilder;
import com.segment.analytics.internal.http.SegmentService;
import com.segment.analytics.messages.Message;
import com.squareup.burst.BurstJUnit4;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(BurstJUnit4.class) public class AnalyticsClientTest {

  AnalyticsClient client;
  @Mock BlockingQueue<Message> blockingQueue;
  @Mock SegmentService segmentService;
  @Mock Log log;
  @Mock ThreadFactory threadFactory;
  @Mock ExecutorService executorService;
  @Mock Thread thread;

  @Before public void setUp() {
    initMocks(this);

    when(threadFactory.newThread(any(Runnable.class))).thenReturn(thread);
    client =
        new AnalyticsClient(blockingQueue, segmentService, 25, log, threadFactory, executorService);
  }

  @Test public void enqueueAddsToQueue(MessageBuilder builder) {
    Message message = builder.get().userId("prateek").build();

    client.enqueue(message);

    verify(blockingQueue).add(message);
  }

  @Test public void shutdown() {
    client.shutdown();

    verify(thread).interrupt();
    verify(blockingQueue).clear();
  }
}
