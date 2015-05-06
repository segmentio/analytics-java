package com.segment.analytics.internal;

import com.google.common.annotations.VisibleForTesting;
import com.segment.analytics.Log;
import com.segment.analytics.internal.http.SegmentService;
import com.segment.analytics.messages.Message;
import com.segment.backo.Backo;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import retrofit.RetrofitError;

public class AnalyticsClient {
  private final BlockingQueue<Message> messageQueue;
  private final SegmentService service;
  private final int size;
  private final Log log;
  private final ExecutorService networkExecutor;
  private final ExecutorService looperExecutor;
  private final ScheduledExecutorService flushScheduler;

  public static AnalyticsClient create(SegmentService segmentService, int flushQueueSize,
      long flushIntervalInMillis, Log log, ThreadFactory threadFactory,
      ExecutorService networkExecutor) {
    return new AnalyticsClient(new LinkedBlockingQueue<Message>(), segmentService, flushQueueSize,
        flushIntervalInMillis, log, threadFactory, networkExecutor);
  }

  AnalyticsClient(BlockingQueue<Message> messageQueue, SegmentService service, int maxQueueSize,
      long flushIntervalInMillis, Log log, ThreadFactory threadFactory,
      ExecutorService networkExecutor) {
    this.messageQueue = messageQueue;
    this.service = service;
    this.size = maxQueueSize;
    this.log = log;
    this.looperExecutor = Executors.newSingleThreadExecutor(threadFactory);
    this.networkExecutor = networkExecutor;

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
    looperExecutor.shutdown();
    flushScheduler.shutdown();
    networkExecutor.shutdown();
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
            networkExecutor.submit(new BatchUploadTask(service, Batch.create(messages), log));
            messages = new ArrayList<>();
          }
        }
      } catch (InterruptedException e) {
        log.print(Log.Level.ERROR, "Thread interrupted while polling for messages.");
      }
    }
  }

  static class BatchUploadTask implements Runnable {
    @VisibleForTesting final Batch batch;
    private final SegmentService service;
    private final Log log;
    private static final Backo BACKO = Backo.builder() //
        .base(TimeUnit.SECONDS, 30) //
        .cap(TimeUnit.HOURS, 1) //
        .jitter(1) //
        .build();

    BatchUploadTask(SegmentService service, Batch batch, Log log) {
      this.service = service;
      this.batch = batch;
      this.log = log;
    }

    @Override public void run() {
      int attempts = 0;

      while (true) {
        try {
          // Ignore return value, UploadResponse#success will never return false for 200 OK
          service.upload(batch);
          return;
        } catch (RetrofitError error) {
          switch (error.getKind()) {
            case NETWORK:
              log.print(Log.Level.DEBUG, error, "Could not upload batch: %s. Retrying.", batch);
              break;
            default:
              log.print(Log.Level.ERROR, error, "Could not upload batch: %s. Giving up.", batch);
              return; // Don't retry
          }
        }

        try {
          BACKO.sleep(attempts);
          attempts++;
        } catch (InterruptedException e) {
          log.print(Log.Level.ERROR, "Thread interrupted while backing off for batch: %s.", batch);
          return;
        }
      }
    }
  }
}
