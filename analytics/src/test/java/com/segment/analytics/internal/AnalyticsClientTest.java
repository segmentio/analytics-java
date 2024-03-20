package com.segment.analytics.internal;

import static com.segment.analytics.internal.FlushMessage.POISON;
import static com.segment.analytics.internal.StopMessage.STOP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import com.google.gson.Gson;
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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
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
import org.mockito.Spy;
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
  private int MAX_BATCH_SIZE = 1024 * 500; // 500kb
  private int MAX_MSG_SIZE = 1024 * 32; // 32kb //This is the limit for a message object
  private int MSG_MAX_CREATE_SIZE =
      MAX_MSG_SIZE
          - 200; // Once we create msg object with this size it barely below 32 threshold so good
  // for tests
  private static String writeKey = "writeKey";

  Log log = Log.NONE;

  ThreadFactory threadFactory;
  @Spy LinkedBlockingQueue<Message> messageQueue;
  @Mock SegmentService segmentService;
  @Mock ExecutorService networkExecutor;
  @Mock Callback callback;
  @Mock UploadResponse response;

  AtomicBoolean isShutDown;

  @Before
  public void setUp() {
    openMocks(this);

    isShutDown = new AtomicBoolean(false);
    threadFactory = Executors.defaultThreadFactory();
  }

  // Defers loading the client until tests can initialize all required
  // dependencies.
  AnalyticsClient newClient() {
    return new AnalyticsClient(
        messageQueue,
        null,
        segmentService,
        50,
        TimeUnit.HOURS.toMillis(1),
        0,
        MAX_BATCH_SIZE,
        log,
        threadFactory,
        networkExecutor,
        Collections.singletonList(callback),
        isShutDown,
        writeKey,
        new Gson());
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

  private static String generateDataOfSize(int msgSize) {
    char[] chars = new char[msgSize];
    Arrays.fill(chars, 'a');

    return new String(chars);
  }

  private static String generateDataOfSizeSpecialChars(
      int sizeInBytes, boolean slightlyBelowLimit) {
    StringBuilder builder = new StringBuilder();
    Character[] specialChars = new Character[] {'$', '¢', 'ह', '€', '한', '©', '¶'};
    int currentSize = 0;
    String smileyFace = "\uD83D\uDE01";
    // 😁 = '\uD83D\uDE01';
    Random rand = new Random();
    int loopCount = 1;
    while (currentSize < sizeInBytes) {
      int randomNum;
      // decide if regular/special character
      if (loopCount > 3 && loopCount % 4 == 0) {
        randomNum = rand.nextInt(((specialChars.length - 1) - 0) + 1) + 0;
        builder.append(specialChars[randomNum]);
      } else if (loopCount > 9 && loopCount % 10 == 0) {
        builder.append(smileyFace);
      } else {
        // random letter from a - z
        randomNum = rand.nextInt(('z' - 'a') + 1) + 'a';
        builder.append((char) randomNum);
      }

      // check size so far
      String temp = builder.toString();
      currentSize = temp.getBytes(StandardCharsets.UTF_8).length;
      if (slightlyBelowLimit && ((sizeInBytes - currentSize) < 500)) {
        break;
      }
      loopCount++;
    }
    return builder.toString();
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

    properties.put("property1", generateDataOfSize(1024 * 33));

    TrackMessage bigMessage =
        TrackMessage.builder("Big Event").userId("bar").properties(properties).build();
    try {
      client.enqueue(bigMessage);
    } catch (IllegalArgumentException e) {
      assertThat(e).isExactlyInstanceOf(e.getClass());
    }

    // can't test for exact size cause other attributes come in play
    assertThat(client.messageSizeInBytes(bigMessage)).isGreaterThan(1024 * 33);
  }

  @Test
  public void dontFlushUntilReachesMaxSize() throws InterruptedException {
    AnalyticsClient client = newClient();
    Map<String, String> properties = new HashMap<String, String>();

    properties.put("property2", generateDataOfSize(MAX_BATCH_SIZE - 200));

    TrackMessage bigMessage =
        TrackMessage.builder("Big Event").userId("bar").properties(properties).build();
    try {
      client.enqueue(bigMessage);
    } catch (IllegalArgumentException e) {
      //    	throw new InterruptedException(e.getMessage());
    }

    wait(messageQueue);

    verify(networkExecutor, never()).submit(any(Runnable.class));
  }

  /**
   * Modified this test case since we are changing logic to NOT allow messages bigger than 32 kbs
   * individually to be enqueued, hence had to lower the size of the generated msg here. chose
   * MSG_MAX_CREATE_SIZE because it will generate a message just below the limit of 32 kb after it
   * creates a Message object modified the number of events that will be created since the batch
   * creation logic was also changed to not allow batches larger than 500 kb meaning every 15/16
   * events the queue will be backPressured and poisoned/flushed (3 times) (purpose of test) AND
   * there will be 4 batches submitted (15 msgs, 1 msg, 15 msg, 15 msg) so purpose of test case
   * stands
   *
   * @throws InterruptedException
   */
  @Test
  public void flushHowManyTimesNecessaryToStayWithinLimit() throws InterruptedException {
    AnalyticsClient client =
        new AnalyticsClient(
            messageQueue,
            null,
            segmentService,
            50,
            TimeUnit.HOURS.toMillis(1),
            0,
            MAX_BATCH_SIZE * 4,
            log,
            threadFactory,
            networkExecutor,
            Collections.singletonList(callback),
            isShutDown,
            writeKey,
            new Gson());

    Map<String, String> properties = new HashMap<String, String>();

    properties.put("property3", generateDataOfSize(MSG_MAX_CREATE_SIZE));

    for (int i = 0; i < 46; i++) {
      TrackMessage bigMessage =
          TrackMessage.builder("Big Event").userId("bar").properties(properties).build();
      client.enqueue(bigMessage);
      verify(messageQueue).put(bigMessage);
    }

    wait(messageQueue);
    /**
     * modified from expected 4 to expected 3 times, since we removed the inner loop. The inner loop
     * was forcing to message list created from the queue to keep making batches even if its a 1
     * message batch until the message list is empty, that was forcing the code to make one last
     * batch of 1 msg in size bumping the number of times a batch would be submitted from 3 to 4
     */
    verify(networkExecutor, times(3)).submit(any(Runnable.class));
  }

  /**
   * Had to slightly change test case since we are now modifying the logic to NOT allow messages
   * above 32 KB in size So needed to change size of generated msg to MSG_MAX_CREATE_SIZE to keep
   * purpose of test case intact which is to test the scenario for several messages eventually
   * filling up the queue and flushing. Batches submitted will change from 1 to 2 because the queue
   * will be backpressured at 16 (at this point queue is over the 500KB batch limit so its flushed
   * and when batch is created 16 will be above 500kbs limit so it creates one batch for 15 msg and
   * another one for the remaining single message so 500kb limit per batch is not violated
   *
   * @throws InterruptedException
   */
  @Test
  public void flushWhenMultipleMessagesReachesMaxSize() throws InterruptedException {
    AnalyticsClient client = newClient();
    Map<String, String> properties = new HashMap<String, String>();
    properties.put("property3", generateDataOfSize(MSG_MAX_CREATE_SIZE));

    for (int i = 0; i < 16; i++) {
      TrackMessage bigMessage =
          TrackMessage.builder("Big Event").userId("bar").properties(properties).build();
      client.enqueue(bigMessage);
    }
    wait(messageQueue);
    client.shutdown();
    while (!isShutDown.get()) {}
    verify(networkExecutor, times(2)).submit(any(Runnable.class));
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
    return Batch.create(
        Collections.<String, Object>emptyMap(), Collections.singletonList(message), writeKey);
  }

  @Test
  public void batchRetriesForNetworkErrors() {
    AnalyticsClient client = newClient();
    TrackMessage trackMessage = TrackMessage.builder("foo").userId("bar").build();
    Batch batch = batchFor(trackMessage);

    Response<UploadResponse> successResponse = Response.success(200, response);
    Response<UploadResponse> failureResponse = Response.error(429, ResponseBody.create(null, ""));

    // Throw a network error 3 times.
    when(segmentService.upload(null, batch))
        .thenReturn(Calls.response(failureResponse))
        .thenReturn(Calls.response(failureResponse))
        .thenReturn(Calls.response(failureResponse))
        .thenReturn(Calls.response(successResponse));

    BatchUploadTask batchUploadTask = new BatchUploadTask(client, BACKO, batch, DEFAULT_RETRIES);
    batchUploadTask.run();

    // Verify that we tried to upload 4 times, 3 failed and 1 succeeded.
    verify(segmentService, times(4)).upload(null, batch);
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
    when(segmentService.upload(null, batch))
        .thenReturn(Calls.response(failResponse))
        .thenReturn(Calls.response(failResponse))
        .thenReturn(Calls.response(failResponse))
        .thenReturn(Calls.response(successResponse));

    BatchUploadTask batchUploadTask = new BatchUploadTask(client, BACKO, batch, DEFAULT_RETRIES);
    batchUploadTask.run();

    // Verify that we tried to upload 4 times, 3 failed and 1 succeeded.
    verify(segmentService, times(4)).upload(null, batch);
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
    when(segmentService.upload(null, batch))
        .thenReturn(Calls.response(failResponse))
        .thenReturn(Calls.response(failResponse))
        .thenReturn(Calls.response(failResponse))
        .thenReturn(Calls.response(successResponse));

    BatchUploadTask batchUploadTask = new BatchUploadTask(client, BACKO, batch, DEFAULT_RETRIES);
    batchUploadTask.run();

    // Verify that we tried to upload 4 times, 3 failed and 1 succeeded.
    verify(segmentService, times(4)).upload(null, batch);
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
    when(segmentService.upload(null, batch)).thenReturn(Calls.response(failResponse));

    BatchUploadTask batchUploadTask = new BatchUploadTask(client, BACKO, batch, DEFAULT_RETRIES);
    batchUploadTask.run();

    // Verify we only tried to upload once.
    verify(segmentService).upload(null, batch);
    verify(callback).failure(eq(trackMessage), any(IOException.class));
  }

  @Test
  public void batchDoesNotRetryForNonNetworkErrors() {
    AnalyticsClient client = newClient();
    TrackMessage trackMessage = TrackMessage.builder("foo").userId("bar").build();
    Batch batch = batchFor(trackMessage);

    Call<UploadResponse> networkFailure = Calls.failure(new RuntimeException());
    when(segmentService.upload(null, batch)).thenReturn(networkFailure);

    BatchUploadTask batchUploadTask = new BatchUploadTask(client, BACKO, batch, DEFAULT_RETRIES);
    batchUploadTask.run();

    // Verify we only tried to upload once.
    verify(segmentService).upload(null, batch);
    verify(callback).failure(eq(trackMessage), any(RuntimeException.class));
  }

  @Test
  public void givesUpAfterMaxRetries() {
    AnalyticsClient client = newClient();
    TrackMessage trackMessage = TrackMessage.builder("foo").userId("bar").build();
    Batch batch = batchFor(trackMessage);

    when(segmentService.upload(null, batch))
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
    verify(segmentService, times(11)).upload(null, batch);
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

    when(segmentService.upload(null, batch))
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
    verify(segmentService, times(4)).upload(null, batch);
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
    verifyNoInteractions(networkExecutor, callback, segmentService);
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

    when(segmentService.upload(null, batch))
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
    verify(segmentService, times(1)).upload(null, batch);
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

  /**
   * **********************************************************************************************
   * Test cases for Size check
   * *********************************************************************************************
   */

  /** Individual Size check happy path regular chars */
  @Test
  public void checkForIndividualMessageSizeLessThanLimit() {
    AnalyticsClient client = newClient();
    int msgSize = 1024 * 31; // 31KB
    int sizeLimit = MAX_MSG_SIZE; // 32KB = 32768
    Map<String, String> properties = new HashMap<String, String>();

    properties.put("property1", generateDataOfSize(msgSize));

    TrackMessage bigMessage =
        TrackMessage.builder("Event").userId("jorgen25").properties(properties).build();
    client.enqueue(bigMessage);

    int msgActualSize = client.messageSizeInBytes(bigMessage);
    assertThat(msgActualSize).isLessThanOrEqualTo(sizeLimit);
  }

  /** Individual Size check sad path regular chars (over the limit) */
  @Test
  public void checkForIndividualMessageSizeOverLimit() throws IllegalArgumentException {
    AnalyticsClient client = newClient();
    int msgSize = MAX_MSG_SIZE + 1; // BARELY over the limit
    int sizeLimit = MAX_MSG_SIZE; // 32KB = 32768
    Map<String, String> properties = new HashMap<String, String>();

    properties.put("property1", generateDataOfSize(msgSize));

    TrackMessage bigMessage =
        TrackMessage.builder("Event").userId("jorgen25").properties(properties).build();
    try {
      client.enqueue(bigMessage);
    } catch (IllegalArgumentException e) {
      assertThat(e).isExactlyInstanceOf(e.getClass());
    }

    int msgActualSize = client.messageSizeInBytes(bigMessage);
    assertThat(msgActualSize).isGreaterThan(sizeLimit);
  }

  /** Individual Size check happy path special chars */
  @Test
  public void checkForIndividualMessageSizeSpecialCharsLessThanLimit() {
    AnalyticsClient client = newClient();
    int msgSize = MAX_MSG_SIZE; // 32KB
    int sizeLimit = MAX_MSG_SIZE; // 32KB = 32768

    Map<String, String> properties = new HashMap<String, String>();
    properties.put("property1", generateDataOfSizeSpecialChars(msgSize, true));

    TrackMessage bigMessage =
        TrackMessage.builder("Event").userId("jorgen25").properties(properties).build();
    client.enqueue(bigMessage);

    int msgActualSize = client.messageSizeInBytes(bigMessage);
    assertThat(msgActualSize).isLessThanOrEqualTo(sizeLimit);
  }

  /** Individual Size check sad path special chars (over the limit) */
  @Test
  public void checkForIndividualMessageSizeSpecialCharsAboveLimit() {
    AnalyticsClient client = newClient();
    int msgSize = MAX_MSG_SIZE; // 32KB
    int sizeLimit = MAX_MSG_SIZE; // 32KB = 32768
    Map<String, String> properties = new HashMap<String, String>();

    properties.put("property1", generateDataOfSizeSpecialChars(msgSize, false));

    TrackMessage bigMessage =
        TrackMessage.builder("Event").userId("jorgen25").properties(properties).build();

    try {
      client.enqueue(bigMessage);
    } catch (IllegalArgumentException e) {
      assertThat(e).isExactlyInstanceOf(e.getClass());
    }

    int msgActualSize = client.messageSizeInBytes(bigMessage);
    assertThat(msgActualSize).isGreaterThan(sizeLimit);
  }

  /**
   * *****************************************************************************************************************
   * Test cases for enqueue modified logic
   * ***************************************************************************************************************
   */
  @Test
  public void enqueueVerifyPoisonIsNotCheckedForSize() throws InterruptedException {
    AnalyticsClient clientSpy = spy(newClient());

    clientSpy.enqueue(POISON);
    verify(messageQueue).put(POISON);
    verify(clientSpy, never()).messageSizeInBytes(POISON);
  }

  @Test
  public void enqueueVerifyStopIsNotCheckedForSize() throws InterruptedException {
    AnalyticsClient clientSpy = spy(newClient());

    clientSpy.enqueue(STOP);
    verify(messageQueue).put(STOP);
    verify(clientSpy, never()).messageSizeInBytes(STOP);
  }

  @Test
  public void enqueueVerifyRegularMessageIsEnqueuedAndCheckedForSize(MessageBuilderTest builder)
      throws InterruptedException {
    AnalyticsClient clientSpy = spy(newClient());

    Message message = builder.get().userId("jorgen25").build();
    clientSpy.enqueue(message);
    verify(messageQueue).put(message);
    verify(clientSpy, times(1)).messageSizeInBytes(message);
  }

  /**
   * This test case was to prove the limit in batch is not being respected so will probably delete
   * it later NOTE: Used to be a test case created to prove huge messages above the limit are still
   * being submitted in batch modified it to prove they are not anymore after changing logic in
   * analyticsClient
   *
   * @param builder
   * @throws InterruptedException
   */
  @Test
  public void enqueueSingleMessageAboveLimitWhenNotShutdown(MessageBuilderTest builder)
      throws InterruptedException, IllegalArgumentException {
    AnalyticsClient client = newClient();

    // Message is above batch limit
    final String massData = generateDataOfSizeSpecialChars(MAX_MSG_SIZE, false);
    Map<String, String> integrationOpts = new HashMap<>();
    integrationOpts.put("massData", massData);
    Message message =
        builder.get().userId("foo").integrationOptions("someKey", integrationOpts).build();

    try {
      client.enqueue(message);
    } catch (IllegalArgumentException e) {
      assertThat(e).isExactlyInstanceOf(e.getClass());
    }

    wait(messageQueue);

    // Message is above MSG/BATCH size limit so it should not be put in queue
    verify(messageQueue, never()).put(message);
    // And since it was never in the queue, it was never submitted in batch
    verify(networkExecutor, never()).submit(any(AnalyticsClient.BatchUploadTask.class));
  }

  @Test
  public void enqueueVerifyRegularMessagesSpecialCharactersBelowLimit(MessageBuilderTest builder)
      throws InterruptedException, IllegalArgumentException {
    AnalyticsClient client = newClient();
    int msgSize = 1024 * 18; // 18KB

    for (int i = 0; i < 2; i++) {
      final String data = generateDataOfSizeSpecialChars(msgSize, true);
      Map<String, String> integrationOpts = new HashMap<>();
      integrationOpts.put("data", data);
      Message message =
          builder.get().userId("jorgen25").integrationOptions("someKey", integrationOpts).build();
      client.enqueue(message);
      verify(messageQueue).put(message);
    }
    client.enqueue(POISON);
    verify(messageQueue).put(POISON);

    wait(messageQueue);
    client.shutdown();
    while (!isShutDown.get()) {}

    verify(networkExecutor, times(1)).submit(any(AnalyticsClient.BatchUploadTask.class));
  }

  /**
   * ******************************************************************************************************************
   * Test cases for Batch creation logic
   * ****************************************************************************************************************
   */

  /**
   * Several messages are enqueued and then submitted in a batch
   *
   * @throws InterruptedException
   */
  @Test
  public void submitBatchBelowThreshold() throws InterruptedException, IllegalArgumentException {
    AnalyticsClient client =
        new AnalyticsClient(
            messageQueue,
            null,
            segmentService,
            50,
            TimeUnit.HOURS.toMillis(1),
            0,
            MAX_BATCH_SIZE * 4,
            log,
            threadFactory,
            networkExecutor,
            Collections.singletonList(callback),
            isShutDown,
            writeKey,
            new Gson());

    Map<String, String> properties = new HashMap<String, String>();
    properties.put("property3", generateDataOfSizeSpecialChars(((int) (MAX_MSG_SIZE * 0.9)), true));

    for (int i = 0; i < 15; i++) {
      TrackMessage bigMessage =
          TrackMessage.builder("Big Event").userId("jorgen25").properties(properties).build();
      client.enqueue(bigMessage);
      verify(messageQueue).put(bigMessage);
    }
    client.enqueue(POISON);
    wait(messageQueue);

    client.shutdown();
    while (!isShutDown.get()) {}
    verify(networkExecutor, times(1)).submit(any(Runnable.class));
  }

  /**
   * Enqueued several messages above threshold of 500Kbs so queue gets backpressured at some point
   * and several batches have to be created to not violate threshold
   *
   * @throws InterruptedException
   */
  @Test
  public void submitBatchAboveThreshold() throws InterruptedException, IllegalArgumentException {
    AnalyticsClient client =
        new AnalyticsClient(
            messageQueue,
            null,
            segmentService,
            50,
            TimeUnit.HOURS.toMillis(1),
            0,
            MAX_BATCH_SIZE * 4,
            log,
            threadFactory,
            networkExecutor,
            Collections.singletonList(callback),
            isShutDown,
            writeKey,
            new Gson());

    Map<String, String> properties = new HashMap<String, String>();
    properties.put("property3", generateDataOfSizeSpecialChars(MAX_MSG_SIZE, true));

    for (int i = 0; i < 100; i++) {
      TrackMessage message =
          TrackMessage.builder("Big Event").userId("jorgen25").properties(properties).build();
      client.enqueue(message);
      verify(messageQueue).put(message);
    }
    wait(messageQueue);
    client.shutdown();
    while (!isShutDown.get()) {}

    verify(networkExecutor, times(8)).submit(any(Runnable.class));
  }

  @Test
  public void submitManySmallMessagesBatchAboveThreshold() throws InterruptedException {
    AnalyticsClient client =
        new AnalyticsClient(
            messageQueue,
            null,
            segmentService,
            50,
            TimeUnit.HOURS.toMillis(1),
            0,
            MAX_BATCH_SIZE * 4,
            log,
            threadFactory,
            networkExecutor,
            Collections.singletonList(callback),
            isShutDown,
            writeKey,
            new Gson());

    Map<String, String> properties = new HashMap<String, String>();
    properties.put("property3", generateDataOfSizeSpecialChars(1024 * 8, true));

    for (int i = 0; i < 600; i++) {
      TrackMessage message =
          TrackMessage.builder("Event").userId("jorgen25").properties(properties).build();
      client.enqueue(message);
      verify(messageQueue).put(message);
    }
    wait(messageQueue);
    client.shutdown();
    while (!isShutDown.get()) {}

    verify(networkExecutor, times(21)).submit(any(Runnable.class));
  }
}
