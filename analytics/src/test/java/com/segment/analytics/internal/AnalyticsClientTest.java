package com.segment.analytics.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.segment.analytics.Callback;
import com.segment.analytics.Log;
import com.segment.analytics.TestUtils.MessageBuilderTest;
import com.segment.analytics.http.SegmentService;
import com.segment.analytics.http.UploadResponse;
import com.segment.analytics.internal.AnalyticsClient.BatchUploadTask;
import com.segment.analytics.messages.Batch;
import com.segment.analytics.messages.Message;
import com.segment.analytics.messages.TrackMessage;
import com.segment.backo.Backo;
import com.squareup.burst.BurstJUnit4;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import okhttp3.ResponseBody;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.mock.Calls;

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
  @Mock UploadResponse response;

  @Before
  public void setUp() {
    initMocks(this);
    threadFactory = Executors.defaultThreadFactory();
  }

  // Defers loading the client until tests can initialize all required
  // dependencies.
  AnalyticsClient newClient() {
    return new AnalyticsClient(
        messageQueue,
        segmentService,
        50,
        TimeUnit.HOURS.toMillis(1),
        log,
        threadFactory,
        networkExecutor,
        Collections.singletonList(callback));
  }

  @Test
  public void enqueueAddsToQueue(MessageBuilderTest builder) throws InterruptedException {
    AnalyticsClient client = newClient();

    Message message = builder.get().userId("prateek").build();
    client.enqueue(message);

    verify(messageQueue).put(message);
  }

  @Test
  public void shutdown() {
    AnalyticsClient client = newClient();

    client.shutdown();

    verify(messageQueue).clear();
    verify(networkExecutor).shutdown();
  }

  @Test
  public void flushInsertsPoison() throws InterruptedException {
    AnalyticsClient client = newClient();

    client.flush();

    verify(messageQueue).put(FlushMessage.POISON);
  }

  /** Wait until the queue is drained. */
  static void wait(Queue<?> queue) {
    // noinspection StatementWithEmptyBody
    while (queue.size() > 0) {}
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

  @Test
  public void flushSubmitsToExecutor() {
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

  @Test
  public void enqueueMaxTriggersFlush() {
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

  private static String createDataSize(int msgSize) {
    char[] chars = new char[msgSize];
    Arrays.fill(chars, 'a');

    return new String(chars);
  }

  @Test
  public void calculatesMessageByteSize() {
    AnalyticsClient client = newClient();
    Map<String, String> properties = new HashMap<String, String>();

    properties.put("dummy-property", createDataSize(1024 * 33));

    TrackMessage bigMessage =
        TrackMessage.builder("Big Event").userId("bar").properties(properties).build();
    client.enqueue(bigMessage);

    // can't test for exact size cause other attributes come in play
    assertThat(client.messageSizeInBytes(bigMessage)).isGreaterThan(1024 * 33);
  }

  @Test
  public void dontEnqueueWhenReachesMaxSize() throws InterruptedException {
    AnalyticsClient client = newClient();
    Map<String, String> properties = new HashMap<String, String>();

    properties.put("dummy-property", createDataSize(1024 * 33));

    TrackMessage bigMessage =
        TrackMessage.builder("Big Event").userId("bar").properties(properties).build();
    client.enqueue(bigMessage);

    // assertThat(client.messageSizeInBytes(bigMessage)).isEqualTo(30);

    Message tinyMessage = TrackMessage.builder("Tinny Event").userId("bar").build();
    client.enqueue(tinyMessage);
    wait(messageQueue);

    verify(messageQueue, times(2)).put(any(Message.class));
  }

  @Test
  public void enqueueBeforeMaxDoesNotTriggerFlush() {
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

  @Test
  public void batchRetriesForNetworkErrors() {
    AnalyticsClient client = newClient();
    TrackMessage trackMessage = TrackMessage.builder("foo").userId("bar").build();
    Batch batch = batchFor(trackMessage);

    Response<UploadResponse> successResponse = Response.success(200, response);
    Response<UploadResponse> failureResponse = Response.error(429, ResponseBody.create(null, ""));

    // Throw a network error 3 times.
    when(segmentService.upload(batch))
        .thenReturn(Calls.response(failureResponse))
        .thenReturn(Calls.response(failureResponse))
        .thenReturn(Calls.response(failureResponse))
        .thenReturn(Calls.response(successResponse));

    BatchUploadTask batchUploadTask = new BatchUploadTask(client, BACKO, batch);
    batchUploadTask.run();

    // Verify that we tried to upload 4 times, 3 failed and 1 succeeded.
    verify(segmentService, times(4)).upload(batch);
    verify(callback).success(trackMessage);
  }

  @Test
  public void batchRetriesForHTTP5xxErrors() {
    AnalyticsClient client = newClient();
    TrackMessage trackMessage = TrackMessage.builder("foo").userId("bar").build();
    Batch batch = batchFor(trackMessage);

    // Throw a HTTP error 3 times.

    Response<UploadResponse> successResponse = Response.success(200, response);
    Response<UploadResponse> failResponse =
        Response.error(500, ResponseBody.create(null, "Server Error"));
    when(segmentService.upload(batch))
        .thenReturn(Calls.response(failResponse))
        .thenReturn(Calls.response(failResponse))
        .thenReturn(Calls.response(failResponse))
        .thenReturn(Calls.response(successResponse));

    BatchUploadTask batchUploadTask = new BatchUploadTask(client, BACKO, batch);
    batchUploadTask.run();

    // Verify that we tried to upload 4 times, 3 failed and 1 succeeded.
    verify(segmentService, times(4)).upload(batch);
    verify(callback).success(trackMessage);
  }

  @Test
  public void batchRetriesForHTTP429Errors() {
    AnalyticsClient client = newClient();
    TrackMessage trackMessage = TrackMessage.builder("foo").userId("bar").build();
    Batch batch = batchFor(trackMessage);

    // Throw a HTTP error 3 times.
    Response<UploadResponse> successResponse = Response.success(200, response);
    Response<UploadResponse> failResponse =
        Response.error(429, ResponseBody.create(null, "Rate Limited"));
    when(segmentService.upload(batch))
        .thenReturn(Calls.response(failResponse))
        .thenReturn(Calls.response(failResponse))
        .thenReturn(Calls.response(failResponse))
        .thenReturn(Calls.response(successResponse));

    BatchUploadTask batchUploadTask = new BatchUploadTask(client, BACKO, batch);
    batchUploadTask.run();

    // Verify that we tried to upload 4 times, 3 failed and 1 succeeded.
    verify(segmentService, times(4)).upload(batch);
    verify(callback).success(trackMessage);
  }

  @Test
  public void batchDoesNotRetryForNon5xxAndNon429HTTPErrors() {
    AnalyticsClient client = newClient();
    TrackMessage trackMessage = TrackMessage.builder("foo").userId("bar").build();
    Batch batch = batchFor(trackMessage);

    // Throw a HTTP error that should not be retried.
    Response<UploadResponse> failResponse =
        Response.error(404, ResponseBody.create(null, "Not Found"));
    when(segmentService.upload(batch)).thenReturn(Calls.response(failResponse));

    BatchUploadTask batchUploadTask = new BatchUploadTask(client, BACKO, batch);
    batchUploadTask.run();

    // Verify we only tried to upload once.
    verify(segmentService).upload(batch);
    verify(callback).failure(eq(trackMessage), any(IOException.class));
  }

  @Test
  public void batchDoesNotRetryForNonNetworkErrors() {
    AnalyticsClient client = newClient();
    TrackMessage trackMessage = TrackMessage.builder("foo").userId("bar").build();
    Batch batch = batchFor(trackMessage);

    Call<UploadResponse> networkFailure = Calls.failure(new RuntimeException());
    when(segmentService.upload(batch)).thenReturn(networkFailure);

    BatchUploadTask batchUploadTask = new BatchUploadTask(client, BACKO, batch);
    batchUploadTask.run();

    // Verify we only tried to upload once.
    verify(segmentService).upload(batch);
    verify(callback).failure(eq(trackMessage), any(RuntimeException.class));
  }

  @Test
  public void givesUpAfterMaxRetries() {
    AnalyticsClient client = newClient();
    TrackMessage trackMessage = TrackMessage.builder("foo").userId("bar").build();
    Batch batch = batchFor(trackMessage);

    when(segmentService.upload(batch))
        .thenAnswer(
            new Answer<Call<UploadResponse>>() {
              public Call<UploadResponse> answer(InvocationOnMock invocation) {
                Response<UploadResponse> failResponse =
                    Response.error(429, ResponseBody.create(null, "Not Found"));
                return Calls.response(failResponse);
              }
            });

    BatchUploadTask batchUploadTask = new BatchUploadTask(client, BACKO, batch);
    batchUploadTask.run();

    // 50 == MAX_ATTEMPTS in AnalyticsClient.java
    verify(segmentService, times(50)).upload(batch);
    verify(callback)
        .failure(
            eq(trackMessage),
            argThat(
                new ArgumentMatcher<IOException>() {
                  @Override
                  public boolean matches(IOException exception) {
                    return exception.getMessage().equals("50 retries exhausted");
                  }
                }));
  }
}
