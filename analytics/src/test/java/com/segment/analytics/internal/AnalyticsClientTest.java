package com.segment.analytics.internal;

import com.segment.analytics.Log;
import com.segment.analytics.MessageCallback;
import com.segment.analytics.TestUtils.MessageBuilderTest;
import com.segment.analytics.http.SegmentService;
import com.segment.analytics.internal.AnalyticsClient.BatchUploadTask;
import com.segment.analytics.internal.AnalyticsClient.RetriesExhaustedException;
import com.segment.analytics.messages.Batch;
import com.segment.analytics.messages.Message;
import com.segment.analytics.messages.TrackMessage;
import com.segment.backo.Backo;
import com.squareup.burst.BurstJUnit4;
import java.io.IOException;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import retrofit.RetrofitError;
import retrofit.converter.ConversionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(BurstJUnit4.class) //
public class AnalyticsClientTest {
  // Backo instance for testing which trims down the wait times.
  private static final Backo BACKO =
      Backo.builder().base(TimeUnit.NANOSECONDS, 1).factor(1).build();

  Log log = Log.NONE;
  ThreadFactory threadFactory;
  @Mock SegmentService segmentService;
  @Mock ExecutorService networkExecutor;
  @Mock MessageCallback callback;
  @Mock BlockingQueue<Message> messageQueue;

  @Before public void setUp() {
    initMocks(this);
    threadFactory = Executors.defaultThreadFactory();
  }

  // Defers loading the client until tests can initialize all required dependencies.
  AnalyticsClient newClient() {
    return new AnalyticsClient(messageQueue, segmentService, 50, TimeUnit.HOURS.toMillis(1), log,
        threadFactory, networkExecutor, callback);
  }

  @Test public void enqueueAddsToQueue(MessageBuilderTest builder) {
    AnalyticsClient client = newClient();

    Message message = builder.get().userId("prateek").build();
    client.enqueue(message);

    verify(messageQueue).add(message);
  }

  @Test public void shutdown() {
    AnalyticsClient client = newClient();

    client.shutdown();

    verify(messageQueue).clear();
    verify(networkExecutor).shutdown();
  }

  @Test public void flushInsertsPoison() {
    AnalyticsClient client = newClient();

    client.flush();

    verify(messageQueue).add(FlushMessage.POISON);
  }

  /** Wait until the queue is drained. */
  static void wait(Queue<?> queue) {
    //noinspection StatementWithEmptyBody
    while (queue.size() > 0) {
    }
  }

  /**
   * Verify that a {@link BatchUploadTask} was submitted to the executor, and return the {@link
   * BatchUploadTask#batch} it was uploading..
   */
  static Batch captureBatch(ExecutorService executor) {
    final ArgumentCaptor<Runnable> runnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);
    verify(executor, timeout(1000)).submit(runnableArgumentCaptor.capture());
    final BatchUploadTask task = (BatchUploadTask) runnableArgumentCaptor.getValue();
    return task.batch;
  }

  @Test public void flushSubmitsToExecutor() {
    messageQueue = new LinkedBlockingQueue<>();
    AnalyticsClient client = newClient();

    TrackMessage first = TrackMessage.builder("foo").userId("bar").build();
    TrackMessage second = TrackMessage.builder("qaz").userId("qux").build();
    client.enqueue(first);
    client.enqueue(second);
    client.flush();
    wait(messageQueue);

    assertThat(captureBatch(networkExecutor).batch()).containsExactly(first, second);
  }

  @Test public void enqueueMaxTriggersFlush() {
    messageQueue = new LinkedBlockingQueue<>();
    AnalyticsClient client = newClient();

    // Enqueuing 51 messages (> 50) should trigger flush.
    for (int i = 0; i < 51; i++) {
      client.enqueue(TrackMessage.builder("Event " + i).userId("bar").build());
    }
    wait(messageQueue);

    // Verify that the executor saw the batch.
    assertThat(captureBatch(networkExecutor).batch()).hasSize(50);
  }

  @Test public void enqueueBeforeMaxDoesNotTriggerFlush() {
    messageQueue = new LinkedBlockingQueue<>();
    AnalyticsClient client = newClient();

    // Enqueuing 5 messages (< 50) should not trigger flush.
    for (int i = 0; i < 5; i++) {
      client.enqueue(TrackMessage.builder("Event " + i).userId("bar").build());
    }
    wait(messageQueue);

    // Verify that the executor didn't see anything.
    verify(networkExecutor, never()).submit(any(Runnable.class));
  }

  static Batch batchFor(Message message) {
    return Batch.create(Collections.<String, Object>emptyMap(), Collections.singletonList(message));
  }

  @Test public void batchRetriesForNetworkErrors() {
    TrackMessage trackMessage = TrackMessage.builder("foo").userId("bar").build();
    Batch batch = batchFor(trackMessage);
    BatchUploadTask batchUploadTask =
        new BatchUploadTask(segmentService, batch, BACKO, log, callback);

    // Throw a network error 3 times.
    RetrofitError retrofitError = RetrofitError.networkError(null, new IOException());
    when(segmentService.upload(batch)).thenThrow(retrofitError)
        .thenThrow(retrofitError)
        .thenThrow(retrofitError)
        .thenReturn(null);

    batchUploadTask.run();

    // Verify that we tried to upload 4 times, 3 failed and 1 succeeded.
    verify(segmentService, times(4)).upload(batch);
    verify(callback).onSuccess(trackMessage);
  }

  @Test public void nullCallbackIsIgnoredOnSuccess() {
    TrackMessage trackMessage = TrackMessage.builder("foo").userId("bar").build();
    Batch batch = batchFor(trackMessage);

    BatchUploadTask batchUploadTask = new BatchUploadTask(segmentService, batch, BACKO, log, null);
    when(segmentService.upload(batch)).thenReturn(null);
    batchUploadTask.run();

    verify(segmentService).upload(batch);
  }

  @Test public void batchDoesNotRetryForNonNetworkErrors() {
    TrackMessage trackMessage = TrackMessage.builder("foo").userId("bar").build();
    Batch batch = batchFor(trackMessage);

    BatchUploadTask batchUploadTask =
        new BatchUploadTask(segmentService, batch, BACKO, log, callback);
    RetrofitError retrofitError =
        RetrofitError.conversionError(null, null, null, null, new ConversionException("fake"));
    doThrow(retrofitError).when(segmentService).upload(batch);

    batchUploadTask.run();

    // Verify we only tried to upload once.
    verify(segmentService).upload(batch);
    // And we gave up.
    verify(callback).onFailure(trackMessage, retrofitError);
  }

  @Test public void nullCallbackIsIgnoredOnFailure() {
    TrackMessage trackMessage = TrackMessage.builder("foo").userId("bar").build();
    Batch batch = batchFor(trackMessage);

    BatchUploadTask batchUploadTask = new BatchUploadTask(segmentService, batch, BACKO, log, null);
    RetrofitError retrofitError =
        RetrofitError.conversionError(null, null, null, null, new ConversionException("fake"));
    doThrow(retrofitError).when(segmentService).upload(batch);

    batchUploadTask.run();

    verify(segmentService).upload(batch);
  }

  @Test public void givesUpAfterMaxRetries() {
    TrackMessage trackMessage = TrackMessage.builder("foo").userId("bar").build();
    Batch batch = batchFor(trackMessage);

    BatchUploadTask batchUploadTask =
        new BatchUploadTask(segmentService, batch, BACKO, log, callback);
    RetrofitError retrofitError = RetrofitError.networkError(null, new IOException());
    when(segmentService.upload(batch)).thenThrow(retrofitError);

    batchUploadTask.run();

    // 50 == MAX_ATTEMPTS in AnalyticsClient.java
    verify(segmentService, times(50)).upload(batch);
    verify(callback).onFailure(eq(trackMessage), argThat(new TypeSafeMatcher<Throwable>() {
      @Override protected boolean matchesSafely(Throwable item) {
        if (!(item instanceof RetriesExhaustedException)) return false;
        RetriesExhaustedException exception = (RetriesExhaustedException) item;
        return exception.retryCount == 50;
      }

      @Override public void describeTo(Description description) {
      }
    }));
  }
}
