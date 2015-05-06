package com.segment.analytics.internal;

import com.segment.analytics.Log;
import com.segment.analytics.TestUtils.MessageBuilderTest;
import com.segment.analytics.internal.AnalyticsClient.BatchUploadTask;
import com.segment.analytics.internal.http.SegmentService;
import com.segment.analytics.messages.Message;
import com.segment.analytics.messages.TrackMessage;
import com.segment.backo.Backo;
import com.squareup.burst.BurstJUnit4;
import java.io.IOException;
import java.util.Collections;
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
import retrofit.RetrofitError;
import retrofit.converter.ConversionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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

    //noinspection StatementWithEmptyBody
    while (messageQueue.size() > 0) {
      // wait to make sure looper reads the flush message
    }

    final ArgumentCaptor<Runnable> runnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);
    verify(networkExecutor, Mockito.timeout(1 * 1000)).submit(runnableArgumentCaptor.capture());

    final BatchUploadTask task = (BatchUploadTask) runnableArgumentCaptor.getValue();
    assertThat(task.batch.batch()).containsExactly(first, second);
  }

  @Test public void enqueueMaxTriggersFlush() {
    threadFactory = Executors.defaultThreadFactory();
    messageQueue = new LinkedBlockingQueue<>();
    client = new AnalyticsClient(messageQueue, segmentService, 5, TimeUnit.HOURS.toMillis(1), log,
        threadFactory, networkExecutor);

    for (int i = 0; i < 5; i++) {
      client.enqueue(TrackMessage.builder("Event " + i).userId("bar").build());
    }

    //noinspection StatementWithEmptyBody
    while (messageQueue.size() > 0) {
      // wait to make sure looper reads the flush message
    }

    final ArgumentCaptor<Runnable> runnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);
    verify(networkExecutor, Mockito.timeout(1 * 1000)).submit(runnableArgumentCaptor.capture());
    final BatchUploadTask task = (BatchUploadTask) runnableArgumentCaptor.getValue();
    assertThat(task.batch.batch()).hasSize(5);
  }

  @Test public void enqueueBeforeMaxDoesNotTriggerFlush() {
    threadFactory = Executors.defaultThreadFactory();
    messageQueue = new LinkedBlockingQueue<>();
    client = new AnalyticsClient(messageQueue, segmentService, 10, TimeUnit.HOURS.toMillis(1), log,
        threadFactory, networkExecutor);

    for (int i = 0; i < 5; i++) {
      client.enqueue(TrackMessage.builder("Event " + i).userId("bar").build());
    }

    //noinspection StatementWithEmptyBody
    while (messageQueue.size() > 0) {
      // wait to make sure looper reads the flush message
    }

    verify(networkExecutor, never()).submit(any(Runnable.class));
  }

  @Test public void batchRetriesForNetworkErrors() {
    TrackMessage trackMessage = TrackMessage.builder("foo").userId("bar").build();
    Batch batch = Batch.create(Collections.<Message>singletonList(trackMessage));
    BatchUploadTask batchUploadTask =
        new BatchUploadTask(segmentService, batch, Backo.builder().build(), log);
    RetrofitError retrofitError = RetrofitError.networkError(null, new IOException());

    when(segmentService.upload(batch)).thenThrow(retrofitError)
        .thenThrow(retrofitError)
        .thenThrow(retrofitError)
        .thenReturn(null);

    batchUploadTask.run();

    verify(segmentService, times(4)).upload(batch);
  }

  @Test public void batchDoesNotRetryForNonNetworkErrors() {
    TrackMessage trackMessage = TrackMessage.builder("foo").userId("bar").build();
    Batch batch = Batch.create(Collections.<Message>singletonList(trackMessage));
    BatchUploadTask batchUploadTask =
        new BatchUploadTask(segmentService, batch, Backo.builder().build(), log);
    RetrofitError retrofitError =
        RetrofitError.conversionError(null, null, null, null, new ConversionException("fake"));
    doThrow(retrofitError).when(segmentService).upload(batch);

    batchUploadTask.run();

    verify(segmentService, times(1)).upload(batch);
  }
}
