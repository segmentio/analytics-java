package com.segment.analytics.internal;

import com.segment.analytics.Log;
import com.segment.analytics.MessageCallback;
import com.segment.analytics.http.SegmentService;
import com.segment.analytics.messages.Batch;
import com.segment.analytics.messages.Message;
import com.segment.backo.Backo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import retrofit.RetrofitError;

public class AnalyticsClient {
  private static final Map<String, ?> CONTEXT;

  static {
    Map<String, String> library = new LinkedHashMap<>();
    library.put("name", "analytics-java");
    library.put("version", AnalyticsVersion.get());
    Map<String, Object> context = new LinkedHashMap<>();
    context.put("library", Collections.unmodifiableMap(library));
    CONTEXT = Collections.unmodifiableMap(context);
  }

  private final BlockingQueue<Message> messageQueue;
  private final SegmentService service;
  private final int size;
  private final Log log;
  private final ExecutorService networkExecutor;
  private final ExecutorService looperExecutor;
  private final ScheduledExecutorService flushScheduler;
  private final MessageCallback callback;

  public static AnalyticsClient create(SegmentService segmentService, int flushQueueSize,
      long flushIntervalInMillis, Log log, ThreadFactory threadFactory,
      ExecutorService networkExecutor, MessageCallback callback) {
    return new AnalyticsClient(new LinkedBlockingQueue<Message>(), segmentService, flushQueueSize,
        flushIntervalInMillis, log, threadFactory, networkExecutor, callback);
  }

  AnalyticsClient(BlockingQueue<Message> messageQueue, SegmentService service, int maxQueueSize,
      long flushIntervalInMillis, Log log, ThreadFactory threadFactory,
      ExecutorService networkExecutor, MessageCallback callback) {
    this.messageQueue = messageQueue;
    this.service = service;
    this.size = maxQueueSize;
    this.log = log;
    this.looperExecutor = Executors.newSingleThreadExecutor(threadFactory);
    this.networkExecutor = networkExecutor;
    this.callback = callback;

    looperExecutor.submit(new Looper());

    flushScheduler = Executors.newScheduledThreadPool(1, threadFactory);
    flushScheduler.scheduleAtFixedRate(new Runnable() {
      @Override public void run() {
        flush();
      }
    }, flushIntervalInMillis, flushIntervalInMillis, TimeUnit.MILLISECONDS);
  }

  public void enqueue(Message message) {
    messageQueue.add(message);
  }

  public void flush() {
    messageQueue.add(FlushMessage.POISON);
  }

  public void shutdown() {
    messageQueue.clear();
    looperExecutor.shutdownNow();
    flushScheduler.shutdownNow();
    networkExecutor.shutdown(); // Let in-flight requests complete.
  }

  /**
   * Looper runs on a background thread and takes messages from the queue. Once it collects enough
   * messages, it triggers a flush.
   */
  class Looper implements Runnable {
    @Override public void run() {
      List<Message> messages = new ArrayList<>();
      try {
        //noinspection InfiniteLoopStatement
        while (true) {
          Message message = messageQueue.take();

          if (message != FlushMessage.POISON) {
            messages.add(message);
          } else if (messages.size() < 1) {
            log.print(Log.Level.VERBOSE, "No messages to flush.");
            continue;
          }

          if (messages.size() >= size || message == FlushMessage.POISON) {
            log.print(Log.Level.VERBOSE, "Uploading batch with %s message(s).", messages.size());
            Batch batch = Batch.create(CONTEXT, messages);
            networkExecutor.submit(BatchUploadTask.create(service, batch, log, callback));
            messages = new ArrayList<>();
          }
        }
      } catch (InterruptedException e) {
        log.print(Log.Level.DEBUG, "Looper interrupted while polling for messages.");
      }
    }
  }

  static class BatchUploadTask implements Runnable {
    private static final Backo BACKO = Backo.builder() //
        .base(TimeUnit.SECONDS, 15) //
        .cap(TimeUnit.HOURS, 1) //
        .jitter(1) //
        .build();
    private static final int MAX_ATTEMPTS = 50; // Max 50 hours ~ 2 days

    private final SegmentService service;
    private final Backo backo;
    private final Log log;
    private final MessageCallback callback;
    final Batch batch;

    public static Runnable create(SegmentService service, Batch batch, Log log,
        MessageCallback callback) {
      return new BatchUploadTask(service, batch, BACKO, log, callback);
    }

    BatchUploadTask(SegmentService service, Batch batch, Backo backo, Log log,
        MessageCallback callback) {
      this.service = service;
      this.batch = batch;
      this.backo = backo;
      this.log = log;
      this.callback = callback;
    }

    /** Returns {@code true} to indicate a batch should be retried. {@code false} otherwise. */
    boolean upload() {
      try {
        // Ignore return value, UploadResponse#onSuccess will never return false for 200 OK
        service.upload(batch);

        if (callback != null) {
          for (Message message : batch.batch()) {
            callback.onSuccess(message);
          }
        }

        return false;
      } catch (RetrofitError error) {
        switch (error.getKind()) {
          case NETWORK:
            log.print(Log.Level.DEBUG, error, "Could not upload batch: %s. Retrying.", batch);
            return true;
          default:
            log.print(Log.Level.ERROR, error, "Could not upload batch: %s. Giving up.", batch);
            if (callback != null) {
              for (Message message : batch.batch()) {
                callback.onFailure(message, error);
              }
            }
            return false; // Don't retry
        }
      }
    }

    @Override public void run() {
      for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
        boolean retry = upload();
        if (!retry) return;

        try {
          backo.sleep(attempt);
        } catch (InterruptedException e) {
          log.print(Log.Level.DEBUG, "Thread interrupted while backing off for batch: %s.", batch);
          return;
        }
      }

      log.print(Log.Level.ERROR, "Could not upload batch: %s. Giving up after exhausting retries.",
          batch);
      if (callback != null) {
        Throwable t = new RetriesExhaustedException(MAX_ATTEMPTS);
        for (Message message : batch.batch()) {
          callback.onFailure(message, t);
        }
      }
    }
  }

  static class RetriesExhaustedException extends Exception {
    final int retryCount;

    private RetriesExhaustedException(int retryCount) {
      super("Exhausted retries: " + retryCount);
      this.retryCount = retryCount;
    }

    @Override public String toString() {
      return "RetriesExhaustedException{" +
          "retryCount=" + retryCount +
          '}';
    }
  }
}
