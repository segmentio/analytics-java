package com.segment.analytics.internal;

import static com.segment.analytics.internal.FlushMessage.POISON;
import static com.segment.analytics.internal.StopMessage.STOP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
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
import java.util.concurrent.atomic.AtomicBoolean;
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

  private int DEFAULT_RETRIES = 10;
  private int MAX_BYTE_SIZE = 1024 * 500; // 500kb

  Log log = Log.NONE;

  ThreadFactory threadFactory;
  @Mock BlockingQueue<Message> messageQueue;
  @Mock SegmentService segmentService;
  @Mock ExecutorService networkExecutor;
  @Mock Callback callback;
  @Mock UploadResponse response;

  AtomicBoolean isShutDown;

  @Before
  public void setUp() {
    initMocks(this);

    isShutDown = new AtomicBoolean(false);
    messageQueue = spy(new LinkedBlockingQueue<Message>());
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
        0,
        MAX_BYTE_SIZE,
        log,
        threadFactory,
        networkExecutor,
        Collections.singletonList(callback),
        isShutDown);
  }

  @Test
  public void enqueueAddsToQueue(MessageBuilderTest builder) throws InterruptedException {
    AnalyticsClient client = newClient();

    Message message = builder.get().userId("prateek").build();
    client.enqueue(message);

    verify(messageQueue).put(message);
  }

  @Test
  public void shutdown() throws InterruptedException {
    messageQueue = new LinkedBlockingQueue<>();
    AnalyticsClient client = newClient();

    client.shutdown();

    verify(networkExecutor).shutdown();
    verify(networkExecutor).awaitTermination(1, TimeUnit.SECONDS);
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

  private static String generateMassDataOfSize(int msgSize) {
    char[] chars = new char[msgSize];
    Arrays.fill(chars, 'a');

    return new String(chars);
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

  @Test
  public void shouldBeAbleToCalculateMessageSize() {
    AnalyticsClient client = newClient();
    Map<String, String> properties = new HashMap<String, String>();

    properties.put("property1", generateMassDataOfSize(1024 * 33));

    TrackMessage bigMessage =
        TrackMessage.builder("Big Event").userId("bar").properties(properties).build();
    client.enqueue(bigMessage);

    // can't test for exact size cause other attributes come in play
    assertThat(client.messageSizeInBytes(bigMessage)).isGreaterThan(1024 * 33);
  }

  @Test
  public void dontFlushUntilReachesMaxSize() throws InterruptedException {
    AnalyticsClient client = newClient();
    Map<String, String> properties = new HashMap<String, String>();

    properties.put("property2", generateMassDataOfSize(MAX_BYTE_SIZE - 200));

    TrackMessage bigMessage =
        TrackMessage.builder("Big Event").userId("bar").properties(properties).build();
    client.enqueue(bigMessage);

    wait(messageQueue);

    verify(networkExecutor, never()).submit(any(Runnable.class));
  }

  @Test
  public void flushWhenReachesMaxSize() throws InterruptedException {
    AnalyticsClient client = newClient();
    Map<String, String> properties = new HashMap<String, String>();

    properties.put("property3", generateMassDataOfSize(MAX_BYTE_SIZE));

    for (int i = 0; i < 10; i++) {
      TrackMessage bigMessage =
          TrackMessage.builder("Big Event").userId("bar").properties(properties).build();
      client.enqueue(bigMessage);
    }

    wait(messageQueue);

    verify(networkExecutor, times(10)).submit(any(Runnable.class));
  }

  @Test
  public void flushHowManyTimesNecessaryToStayWithinLimit() throws InterruptedException {
    AnalyticsClient client =
        new AnalyticsClient(
            messageQueue,
            segmentService,
            50,
            TimeUnit.HOURS.toMillis(1),
            0,
            MAX_BYTE_SIZE * 4,
            log,
            threadFactory,
            networkExecutor,
            Collections.singletonList(callback),
            isShutDown);

    Map<String, String> properties = new HashMap<String, String>();

    properties.put("property3", generateMassDataOfSize(MAX_BYTE_SIZE));

    for (int i = 0; i < 4; i++) {
      TrackMessage bigMessage =
          TrackMessage.builder("Big Event").userId("bar").properties(properties).build();
      client.enqueue(bigMessage);
    }

    wait(messageQueue);

    verify(networkExecutor, times(4)).submit(any(Runnable.class));
  }

  @Test
  public void flushWhenMultipleMessagesReachesMaxSize() throws InterruptedException {
    AnalyticsClient client = newClient();
    Map<String, String> properties = new HashMap<String, String>();
    properties.put("property3", generateMassDataOfSize(MAX_BYTE_SIZE / 9));

    for (int i = 0; i < 10; i++) {
      TrackMessage bigMessage =
          TrackMessage.builder("Big Event").userId("bar").properties(properties).build();
      client.enqueue(bigMessage);
    }

    wait(messageQueue);

    verify(networkExecutor, times(1)).submit(any(Runnable.class));
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

    BatchUploadTask batchUploadTask = new BatchUploadTask(client, BACKO, batch, DEFAULT_RETRIES);
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

    BatchUploadTask batchUploadTask = new BatchUploadTask(client, BACKO, batch, DEFAULT_RETRIES);
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

    BatchUploadTask batchUploadTask = new BatchUploadTask(client, BACKO, batch, DEFAULT_RETRIES);
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

    BatchUploadTask batchUploadTask = new BatchUploadTask(client, BACKO, batch, DEFAULT_RETRIES);
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

    BatchUploadTask batchUploadTask = new BatchUploadTask(client, BACKO, batch, DEFAULT_RETRIES);
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

    BatchUploadTask batchUploadTask = new BatchUploadTask(client, BACKO, batch, 10);
    batchUploadTask.run();

    // DEFAULT_RETRIES == maxRetries
    // tries 11(one normal run + 10 retries) even though default is 50 in AnalyticsClient.java
    verify(segmentService, times(11)).upload(batch);
    verify(callback)
        .failure(
            eq(trackMessage),
            argThat(
                new ArgumentMatcher<IOException>() {
                  @Override
                  public boolean matches(IOException exception) {
                    return exception.getMessage().equals("11 retries exhausted");
                  }
                }));
  }

  @Test
  public void hasDefaultRetriesSetTo3() {
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

    BatchUploadTask batchUploadTask = new BatchUploadTask(client, BACKO, batch, 3);
    batchUploadTask.run();

    // DEFAULT_RETRIES == maxRetries
    // tries 11(one normal run + 10 retries)
    verify(segmentService, times(4)).upload(batch);
    verify(callback)
        .failure(
            eq(trackMessage),
            argThat(
                new ArgumentMatcher<IOException>() {
                  @Override
                  public boolean matches(IOException exception) {
                    return exception.getMessage().equals("4 retries exhausted");
                  }
                }));
  }

  @Test
  public void flushWhenNotShutDown() throws InterruptedException {
    AnalyticsClient client = newClient();

    client.flush();
    verify(messageQueue).put(POISON);
  }

  @Test
  public void flushWhenShutDown() throws InterruptedException {
    AnalyticsClient client = newClient();
    isShutDown.set(true);

    client.flush();

    verify(messageQueue, times(0)).put(any(Message.class));
  }

  @Test
  public void enqueueWithRegularMessageWhenNotShutdown(MessageBuilderTest builder)
      throws InterruptedException {
    AnalyticsClient client = newClient();

    final Message message = builder.get().userId("foo").build();
    client.enqueue(message);

    verify(messageQueue).put(message);
  }

  @Test
  public void enqueueWithRegularMessageWhenShutdown(MessageBuilderTest builder)
      throws InterruptedException {
    AnalyticsClient client = newClient();
    isShutDown.set(true);

    client.enqueue(builder.get().userId("foo").build());

    verify(messageQueue, times(0)).put(any(Message.class));
  }

  @Test
  public void enqueueWithStopMessageWhenShutdown() throws InterruptedException {
    AnalyticsClient client = newClient();
    isShutDown.set(true);

    client.enqueue(STOP);

    verify(messageQueue).put(STOP);
  }

  @Test
  public void shutdownWhenAlreadyShutDown() throws InterruptedException {
    AnalyticsClient client = newClient();
    isShutDown.set(true);

    client.shutdown();

    verify(messageQueue, times(0)).put(any(Message.class));
    verifyZeroInteractions(networkExecutor, callback, segmentService);
  }

  @Test
  public void shutdownWithNoMessageInTheQueue() throws InterruptedException {
    AnalyticsClient client = newClient();
    client.shutdown();

    verify(messageQueue).put(STOP);
    verify(networkExecutor).shutdown();
    verify(networkExecutor).awaitTermination(1, TimeUnit.SECONDS);
    verifyNoMoreInteractions(networkExecutor);
  }

  @Test
  public void shutdownWithMessagesInTheQueue(MessageBuilderTest builder)
      throws InterruptedException {
    AnalyticsClient client = newClient();

    client.enqueue(builder.get().userId("foo").build());
    client.shutdown();

    verify(messageQueue).put(STOP);
    verify(networkExecutor).shutdown();
    verify(networkExecutor).awaitTermination(1, TimeUnit.SECONDS);
    verify(networkExecutor).submit(any(AnalyticsClient.BatchUploadTask.class));
  }

  @Test
  public void neverRetries() {
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

    BatchUploadTask batchUploadTask = new BatchUploadTask(client, BACKO, batch, 0);
    batchUploadTask.run();

    // runs once but never retries
    verify(segmentService, times(1)).upload(batch);
    verify(callback)
        .failure(
            eq(trackMessage),
            argThat(
                new ArgumentMatcher<IOException>() {
                  @Override
                  public boolean matches(IOException exception) {
                    return exception.getMessage().equals("1 retries exhausted");
                  }
                }));
  }
}
