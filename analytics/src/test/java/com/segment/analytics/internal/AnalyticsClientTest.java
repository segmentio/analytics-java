package com.segment.analytics.internal;

import com.segment.analytics.Callback;
import com.segment.analytics.Log;
import com.segment.analytics.TestUtils.MessageBuilderTest;
import com.segment.analytics.http.SegmentService;
import com.segment.analytics.internal.AnalyticsClient.BatchUploadTask;
import com.segment.analytics.messages.Batch;
import com.segment.analytics.messages.Message;
import com.segment.analytics.messages.TrackMessage;
import com.segment.backo.Backo;
import com.squareup.burst.BurstJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(BurstJUnit4.class) //
public class AnalyticsClientTest {
  // Backo instance for testing which trims down the wait times.
  private static final Backo BACKO =
      Backo.builder().base(TimeUnit.NANOSECONDS, 1).factor(1).build();

  Log log = Log.NONE;
  ThreadFactory threadFactory;
  @Mock BlockingQueue<Message> messageQueue;
  @Mock SegmentService segmentService;
  @Mock ExecutorService networkExecutor;
  @Mock Callback callback;

  @Before public void setUp() {
    initMocks(this);
    threadFactory = Executors.defaultThreadFactory();
  }

  // Defers loading the client until tests can initialize all required dependencies.
  AnalyticsClient newClient() {
    return new AnalyticsClient(messageQueue, segmentService, 50, TimeUnit.HOURS.toMillis(1), log,
        threadFactory, networkExecutor, Collections.singletonList(callback));
  }

  @Test public void enqueueAddsToQueue(MessageBuilderTest builder) throws InterruptedException {
    AnalyticsClient client = newClient();

    Message message = builder.get().userId("prateek").build();
    client.enqueue(message);

    verify(messageQueue).put(message);
  }

  @Test public void shutdown() {
    AnalyticsClient client = newClient();

    client.shutdown();

    verify(messageQueue).clear();
    verify(networkExecutor).shutdown();
  }

  @Test public void flushInsertsPoison() throws InterruptedException {
    AnalyticsClient client = newClient();

    client.flush();

    verify(messageQueue).put(FlushMessage.POISON);
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

//  @Test public void batchRetriesForNetworkErrors() {
//    AnalyticsClient client = newClient();
//    TrackMessage trackMessage = TrackMessage.builder("foo").userId("bar").build();
//    Batch batch = batchFor(trackMessage);
//
//    // Throw a network error 3 times.
//    RetrofitError retrofitError = RetrofitError.networkError(null, new IOException());
//    when(segmentService.upload(batch)).thenThrow(retrofitError)
//        .thenThrow(retrofitError)
//        .thenThrow(retrofitError)
//        .thenReturn(null);
//
//    BatchUploadTask batchUploadTask = new BatchUploadTask(client, BACKO, batch);
//    batchUploadTask.run();
//
//    // Verify that we tried to upload 4 times, 3 failed and 1 succeeded.
//    verify(segmentService, times(4)).upload(batch);
//    verify(callback).success(trackMessage);
//  }

//  @Test public void batchDoesNotRetryForNonNetworkErrors() {
//    AnalyticsClient client = newClient();
//    TrackMessage trackMessage = TrackMessage.builder("foo").userId("bar").build();
//    Batch batch = batchFor(trackMessage);
//    RetrofitError retrofitError =
//        RetrofitError.conversionError(null, null, null, null, new ConversionException("fake"));
//    doThrow(retrofitError).when(segmentService).upload(batch);
//
//    BatchUploadTask batchUploadTask = new BatchUploadTask(client, BACKO, batch);
//    batchUploadTask.run();
//
//    // Verify we only tried to upload once.
//    verify(segmentService).upload(batch);
//    verify(callback).failure(trackMessage, retrofitError);
//  }

//  @Test public void givesUpAfterMaxRetries() {
//    AnalyticsClient client = newClient();
//    TrackMessage trackMessage = TrackMessage.builder("foo").userId("bar").build();
//    Batch batch = batchFor(trackMessage);
//    RetrofitError retrofitError = RetrofitError.networkError(null, new IOException());
//    when(segmentService.upload(batch)).thenThrow(retrofitError);
//
//    BatchUploadTask batchUploadTask = new BatchUploadTask(client, BACKO, batch);
//    batchUploadTask.run();
//
//    // 50 == MAX_ATTEMPTS in AnalyticsClient.java
//    verify(segmentService, times(50)).upload(batch);
//    verify(callback).failure(eq(trackMessage), argThat(new TypeSafeMatcher<Throwable>() {
//      @Override public void describeTo(Description description) {
//        description.appendText("expected IOException");
//      }
//
//      @Override protected boolean matchesSafely(Throwable item) {
//        IOException exception = (IOException) item;
//        return exception.getMessage().equals("50 retries exhausted");
//      }
//    }));
//  }
}
