package com.segment.analytics.internal;

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

public class AnalyticsClient {
  private final BlockingQueue<Message> messageQueue;
  private final SegmentService service;
  private final int size;
  private final Log log;
  private final Thread looperThread;
  private final ExecutorService networkExecutor;
  private final ScheduledExecutorService flushScheduler;
  private final Backo backo;

  public static AnalyticsClient create(SegmentService segmentService, int flushQueueSize,
      long flushIntervalInMillis, Log log, ThreadFactory threadFactory,
      ExecutorService networkExecutor) {
    return new AnalyticsClient(new LinkedBlockingQueue<Message>(), segmentService, flushQueueSize,
        flushIntervalInMillis, log, threadFactory, networkExecutor);
  }

  AnalyticsClient(BlockingQueue<Message> messageQueue, SegmentService service, int maxQueueSize,
      long flushIntervalInMillis, Log log, ThreadFactory threadFactory,
      final ExecutorService networkExecutor) {
    this.messageQueue = messageQueue;
    this.service = service;
    this.size = maxQueueSize;
    this.log = log;
    this.networkExecutor = networkExecutor;
    this.backo = Backo.builder() //
        .base(TimeUnit.SECONDS, 30) //
        .cap(TimeUnit.HOURS, 1) //
        .jitter(1) //
        .build();

    looperThread = threadFactory.newThread(new Looper());
    looperThread.start();

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
    looperThread.interrupt();
    messageQueue.clear();
    networkExecutor.shutdown();
    flushScheduler.shutdown();
  }

  /**
   * Looper runs on a background thread and takes messages from the queue. Once it collects enough
   * messages, it triggers a flush.
   */
  class Looper implements Runnable {
    @Override public void run() {
      List<Message> messages = new ArrayList<>();
      try {
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
            networkExecutor.submit(
                new BatchUploadTask(service, Batch.create(messages), log, backo));
            messages = new ArrayList<>();
          }
        }
      } catch (InterruptedException e) {
        log.print(Log.Level.ERROR, "Thread interrupted while polling for messages.");
        return; // Stop processing messages any further
      }
    }
  }
}
