package com.segment.analytics.internal;

import static com.segment.analytics.Log.Level.DEBUG;
import static com.segment.analytics.Log.Level.ERROR;
import static com.segment.analytics.Log.Level.VERBOSE;

import com.google.gson.Gson;
import com.segment.analytics.Callback;
import com.segment.analytics.Log;
import com.segment.analytics.http.SegmentService;
import com.segment.analytics.http.UploadResponse;
import com.segment.analytics.messages.Batch;
import com.segment.analytics.messages.Message;
import com.segment.backo.Backo;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.Response;

public class AnalyticsClient {
  private static final Map<String, ?> CONTEXT;
  private static final int BATCH_MAX_SIZE = 1024 * 500;
  private static final int MSG_MAX_SIZE = 1024 * 32;
  private static final Charset ENCODING = StandardCharsets.UTF_8;
  private Gson gsonInstance;
  private static final String instanceId = UUID.randomUUID().toString();

  static {
    Map<String, String> library = new LinkedHashMap<>();
    library.put("name", "analytics-java");
    library.put("version", AnalyticsVersion.get());
    Map<String, Object> context = new LinkedHashMap<>();
    context.put("library", Collections.unmodifiableMap(library));
    context.put("instanceId", instanceId);
    CONTEXT = Collections.unmodifiableMap(context);
  }

  private final BlockingQueue<Message> messageQueue;
  private final HttpUrl uploadUrl;
  private final SegmentService service;
  private final int size;
  private final int maximumRetries;
  private final int maximumQueueByteSize;
  private int currentQueueSizeInBytes;
  private final Log log;
  private final List<Callback> callbacks;
  private final ExecutorService networkExecutor;
  private final ExecutorService looperExecutor;
  private final ScheduledExecutorService flushScheduler;
  private final AtomicBoolean isShutDown;
  private final String writeKey;

  public static AnalyticsClient create(
      HttpUrl uploadUrl,
      SegmentService segmentService,
      int queueCapacity,
      int flushQueueSize,
      long flushIntervalInMillis,
      int maximumRetries,
      int maximumQueueSizeInBytes,
      Log log,
      ThreadFactory threadFactory,
      ExecutorService networkExecutor,
      List<Callback> callbacks,
      String writeKey,
      Gson gsonInstance) {
    return new AnalyticsClient(
        new LinkedBlockingQueue<Message>(queueCapacity),
        uploadUrl,
        segmentService,
        flushQueueSize,
        flushIntervalInMillis,
        maximumRetries,
        maximumQueueSizeInBytes,
        log,
        threadFactory,
        networkExecutor,
        callbacks,
        new AtomicBoolean(false),
        writeKey,
        gsonInstance);
  }

  public AnalyticsClient(
      BlockingQueue<Message> messageQueue,
      HttpUrl uploadUrl,
      SegmentService service,
      int maxQueueSize,
      long flushIntervalInMillis,
      int maximumRetries,
      int maximumQueueSizeInBytes,
      Log log,
      ThreadFactory threadFactory,
      ExecutorService networkExecutor,
      List<Callback> callbacks,
      AtomicBoolean isShutDown,
      String writeKey,
      Gson gsonInstance) {
    this.messageQueue = messageQueue;
    this.uploadUrl = uploadUrl;
    this.service = service;
    this.size = maxQueueSize;
    this.maximumRetries = maximumRetries;
    this.maximumQueueByteSize = maximumQueueSizeInBytes;
    this.log = log;
    this.callbacks = callbacks;
    this.looperExecutor = Executors.newSingleThreadExecutor(threadFactory);
    this.networkExecutor = networkExecutor;
    this.isShutDown = isShutDown;
    this.writeKey = writeKey;
    this.gsonInstance = gsonInstance;

    this.currentQueueSizeInBytes = 0;

    if (!isShutDown.get()) looperExecutor.submit(new Looper());

    flushScheduler = Executors.newScheduledThreadPool(1, threadFactory);
    flushScheduler.scheduleAtFixedRate(
        new Runnable() {
          @Override
          public void run() {
            flush();
          }
        },
        flushIntervalInMillis,
        flushIntervalInMillis,
        TimeUnit.MILLISECONDS);
  }

  public int messageSizeInBytes(Message message) {
    String stringifiedMessage = gsonInstance.toJson(message);

    return stringifiedMessage.getBytes(ENCODING).length;
  }

  private Boolean isBackPressuredAfterSize(int incomingSize) {
    int POISON_BYTE_SIZE = messageSizeInBytes(FlushMessage.POISON);
    int sizeAfterAdd = this.currentQueueSizeInBytes + incomingSize + POISON_BYTE_SIZE;
    // Leave a 10% buffer since the unsynchronized enqueue could add multiple at a time
    return sizeAfterAdd >= Math.min(this.maximumQueueByteSize, BATCH_MAX_SIZE) * 0.9;
  }

  public boolean offer(Message message) {
    return messageQueue.offer(message);
  }

  public void enqueue(Message message) {
    if (message != StopMessage.STOP && isShutDown.get()) {
      log.print(ERROR, "Attempt to enqueue a message when shutdown has been called %s.", message);
      return;
    }

    try {
      // @jorgen25 message here could be regular msg, POISON or STOP. Only do regular logic if its
      // valid message
      if (message != StopMessage.STOP && message != FlushMessage.POISON) {
        int messageByteSize = messageSizeInBytes(message);

        // @jorgen25 check if message is below 32kb limit for individual messages, no need to check
        // for extra characters
        if (messageByteSize <= MSG_MAX_SIZE) {
          if (isBackPressuredAfterSize(messageByteSize)) {
            this.currentQueueSizeInBytes = messageByteSize;
            messageQueue.put(FlushMessage.POISON);
            messageQueue.put(message);

            log.print(VERBOSE, "Maximum storage size has been hit Flushing...");
          } else {
            messageQueue.put(message);
            this.currentQueueSizeInBytes += messageByteSize;
          }
        } else {
          log.print(
              ERROR, "Message was above individual limit. MessageId: %s", message.messageId());
          throw new IllegalArgumentException(
              "Message was above individual limit. MessageId: " + message.messageId());
        }
      } else {
        messageQueue.put(message);
      }
    } catch (InterruptedException e) {
      log.print(ERROR, e, "Interrupted while adding message %s.", message);
      Thread.currentThread().interrupt();
    }
  }

  public void flush() {
    if (!isShutDown.get()) {
      enqueue(FlushMessage.POISON);
    }
  }

  public void shutdown() {
    if (isShutDown.compareAndSet(false, true)) {
      final long start = System.currentTimeMillis();

      // first let's tell the system to stop
      enqueue(StopMessage.STOP);

      // we can shutdown the flush scheduler without worrying
      flushScheduler.shutdownNow();

      shutdownAndWait(looperExecutor, "looper");
      shutdownAndWait(networkExecutor, "network");

      log.print(
          VERBOSE, "Analytics client shut down in %s ms", (System.currentTimeMillis() - start));
    }
  }

  public void shutdownAndWait(ExecutorService executor, String name) {
    try {
      executor.shutdown();
      final boolean executorTerminated = executor.awaitTermination(1, TimeUnit.SECONDS);

      log.print(
          VERBOSE,
          "%s executor %s.",
          name,
          executorTerminated ? "terminated normally" : "timed out");
    } catch (InterruptedException e) {
      log.print(ERROR, e, "Interrupted while stopping %s executor.", name);
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Looper runs on a background thread and takes messages from the queue. Once it collects enough
   * messages, it triggers a flush.
   */
  class Looper implements Runnable {
    private boolean stop;

    public Looper() {
      this.stop = false;
    }

    @Override
    public void run() {
      LinkedList<Message> messages = new LinkedList<>();
      AtomicInteger currentBatchSize = new AtomicInteger();
      boolean batchSizeLimitReached = false;
      int contextSize = gsonInstance.toJson(CONTEXT).getBytes(ENCODING).length;
      try {
        while (!stop) {
          Message message = messageQueue.take();

          if (message == StopMessage.STOP) {
            log.print(VERBOSE, "Stopping the Looper");
            stop = true;
          } else if (message == FlushMessage.POISON) {
            if (!messages.isEmpty()) {
              log.print(VERBOSE, "Flushing messages.");
            }
          } else {
            // we do  +1 because we are accounting for this new message we just took from the queue
            // which is not in list yet
            // need to check if this message is going to make us go over the limit considering
            // default batch size as well
            int defaultBatchSize =
                BatchUtility.getBatchDefaultSize(contextSize, messages.size() + 1);
            int msgSize = messageSizeInBytes(message);
            if (currentBatchSize.get() + msgSize + defaultBatchSize <= BATCH_MAX_SIZE) {
              messages.add(message);
              currentBatchSize.addAndGet(msgSize);
            } else {
              // put message that did not make the cut this time back on the queue, we already took
              // this message if we dont put it back its lost
              // we take care of that after submitting the batch
              batchSizeLimitReached = true;
            }
          }

          Boolean isBlockingSignal = message == FlushMessage.POISON || message == StopMessage.STOP;
          Boolean isOverflow = messages.size() >= size;

          if (!messages.isEmpty() && (isOverflow || isBlockingSignal || batchSizeLimitReached)) {
            Batch batch = Batch.create(CONTEXT, new ArrayList<>(messages), writeKey);
            log.print(
                VERBOSE,
                "Batching %s message(s) into batch %s.",
                batch.batch().size(),
                batch.sequence());
            networkExecutor.submit(
                BatchUploadTask.create(AnalyticsClient.this, batch, maximumRetries));

            currentBatchSize.set(0);
            messages.clear();
            if (batchSizeLimitReached) {
              // If this is true that means the last message that would make us go over the limit
              // was not added,
              // add it to the now cleared messages list so its not lost
              messages.add(message);
            }
            batchSizeLimitReached = false;
          }
        }
      } catch (InterruptedException e) {
        log.print(DEBUG, "Looper interrupted while polling for messages.");
        Thread.currentThread().interrupt();
      }
      log.print(VERBOSE, "Looper stopped");
    }
  }

  static class BatchUploadTask implements Runnable {
    private static final Backo BACKO =
        Backo.builder() //
            .base(TimeUnit.SECONDS, 15) //
            .cap(TimeUnit.HOURS, 1) //
            .jitter(1) //
            .build();

    private final AnalyticsClient client;
    private final Backo backo;
    final Batch batch;
    private final int maxRetries;

    static BatchUploadTask create(AnalyticsClient client, Batch batch, int maxRetries) {
      return new BatchUploadTask(client, BACKO, batch, maxRetries);
    }

    BatchUploadTask(AnalyticsClient client, Backo backo, Batch batch, int maxRetries) {
      this.client = client;
      this.batch = batch;
      this.backo = backo;
      this.maxRetries = maxRetries;
    }

    private void notifyCallbacksWithException(Batch batch, Exception exception) {
      for (Message message : batch.batch()) {
        for (Callback callback : client.callbacks) {
          callback.failure(message, exception);
        }
      }
    }

    /** Returns {@code true} to indicate a batch should be retried. {@code false} otherwise. */
    boolean upload() {
      client.log.print(VERBOSE, "Uploading batch %s.", batch.sequence());

      try {
        Call<UploadResponse> call = client.service.upload(client.uploadUrl, batch);
        Response<UploadResponse> response = call.execute();

        if (response.isSuccessful()) {
          client.log.print(VERBOSE, "Uploaded batch %s.", batch.sequence());

          for (Message message : batch.batch()) {
            for (Callback callback : client.callbacks) {
              callback.success(message);
            }
          }

          return false;
        }

        int status = response.code();
        if (is5xx(status)) {
          client.log.print(
              DEBUG, "Could not upload batch %s due to server error. Retrying.", batch.sequence());
          return true;
        } else if (status == 429) {
          client.log.print(
              DEBUG, "Could not upload batch %s due to rate limiting. Retrying.", batch.sequence());
          return true;
        }

        client.log.print(DEBUG, "Could not upload batch %s. Giving up.", batch.sequence());
        notifyCallbacksWithException(batch, new IOException(response.errorBody().string()));

        return false;
      } catch (IOException error) {
        client.log.print(DEBUG, error, "Could not upload batch %s. Retrying.", batch.sequence());

        return true;
      } catch (Exception exception) {
        client.log.print(DEBUG, "Could not upload batch %s. Giving up.", batch.sequence());

        notifyCallbacksWithException(batch, exception);

        return false;
      }
    }

    @Override
    public void run() {
      int attempt = 0;
      for (; attempt <= maxRetries; attempt++) {
        boolean retry = upload();
        if (!retry) return;
        try {
          backo.sleep(attempt);
        } catch (InterruptedException e) {
          client.log.print(
              DEBUG, "Thread interrupted while backing off for batch %s.", batch.sequence());
          return;
        }
      }

      client.log.print(ERROR, "Could not upload batch %s. Retries exhausted.", batch.sequence());
      notifyCallbacksWithException(
          batch, new IOException(Integer.toString(attempt) + " retries exhausted"));
    }

    private static boolean is5xx(int status) {
      return status >= 500 && status < 600;
    }
  }

  public static class BatchUtility {

    /**
     * Method to determine what is the expected default size of the batch regardless of messages
     *
     * <p>Sample batch:
     * {"batch":[{"type":"alias","messageId":"fc9198f9-d827-47fb-96c8-095bd3405d93","timestamp":"Nov
     * 18, 2021, 2:45:07
     * PM","userId":"jorgen25","integrations":{"someKey":{"data":"aaaaa"}},"previousId":"foo"},{"type":"alias",
     * "messageId":"3ce6f88c-36cb-4991-83f8-157e10261a89","timestamp":"Nov 18, 2021, 2:45:07
     * PM","userId":"jorgen25",
     * "integrations":{"someKey":{"data":"aaaaa"}},"previousId":"foo"},{"type":"alias",
     * "messageId":"a328d339-899a-4a14-9835-ec91e303ac4d","timestamp":"Nov 18, 2021, 2:45:07 PM",
     * "userId":"jorgen25","integrations":{"someKey":{"data":"aaaaa"}},"previousId":"foo"},{"type":"alias",
     * "messageId":"57b0ceb4-a1cf-4599-9fba-0a44c7041004","timestamp":"Nov 18, 2021, 2:45:07 PM",
     * "userId":"jorgen25","integrations":{"someKey":{"data":"aaaaa"}},"previousId":"foo"}],
     * "sentAt":"Nov 18, 2021, 2:45:07 PM","context":{"library":{"name":"analytics-java",
     * "version":"3.1.3"}},"sequence":1,"writeKey":"XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"}
     *
     * <p>total size of batch : 932
     *
     * <p>BREAKDOWN: {"batch":[MESSAGE1,MESSAGE2,MESSAGE3,MESSAGE4],"sentAt":"MMM dd, yyyy, HH:mm:ss
     * tt","context":CONTEXT,"sequence":1,"writeKey":"XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"}
     *
     * <p>so we need to account for: 1 -message size: 189 * 4 = 756 2 -context object size = 55 in
     * this sample -> 756 + 55 = 811 3 -Metadata (This has the sent data/sequence characters) +
     * extra chars (these are chars like "batch":[] or "context": etc and will be pretty much the
     * same length in every batch -> size is 73 --> 811 + 73 = 884 (well 72 actually, char 73 is the
     * sequence digit which we account for in point 5) 4 -Commas between each message, the total
     * number of commas is number_of_msgs - 1 = 3 -> 884 + 3 = 887 (sample is 886 because the hour
     * in sentData this time happens to be 2:45 but it could be 12:45 5 -Sequence Number increments
     * with every batch created
     *
     * <p>so formulae to determine the expected default size of the batch is
     *
     * @return: defaultSize = messages size + context size + metadata size + comma number + sequence
     *     digits + writekey + buffer
     * @return
     */
    private static int getBatchDefaultSize(int contextSize, int currentMessageNumber) {
      // sample data: {"batch":[],"sentAt":"MMM dd, yyyy, HH:mm:ss tt","context":,"sequence":1,
      //   "writeKey":"XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"} - 119
      // Don't need to squeeze everything possible into a batch, adding a buffer
      int metadataExtraCharsSize = 119 + 1024;
      int commaNumber = currentMessageNumber - 1;

      return contextSize
          + metadataExtraCharsSize
          + commaNumber
          + String.valueOf(Integer.MAX_VALUE).length();
    }
  }
}
