package com.segment.analytics.internal;

import com.segment.analytics.Log;
import com.segment.analytics.internal.http.SegmentService;
import com.segment.analytics.messages.Message;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import retrofit.RetrofitError;

public class AnalyticsClient {
  private final BlockingQueue<Message> messageQueue;
  private final SegmentService service;
  private final int size;
  private final Log log;
  private final Thread looperThread;
  private final ExecutorService flushExecutor;
  private final Backo backo;

  public AnalyticsClient(BlockingQueue<Message> messageQueue, SegmentService service, int size,
      Log log, ThreadFactory looperThreadFactory, ExecutorService flushExecutor) {
    this.messageQueue = messageQueue;
    this.service = service;
    this.size = size;
    this.log = log;
    this.flushExecutor = flushExecutor;
    this.backo = new Backo.Builder() //
        .base(TimeUnit.SECONDS, 30) //
        .cap(TimeUnit.HOURS, 1) //
        .jitter(1) //
        .build();

    looperThread = looperThreadFactory.newThread(new Looper());
    looperThread.start();
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
    flushExecutor.shutdown();
  }

  static class UploadBatchTask implements Runnable {
    private final SegmentService service;
    private final Batch batch;
    private final Log log;
    private final Backo backo;

    public UploadBatchTask(SegmentService service, Batch batch, Log log, Backo backo) {
      this.service = service;
      this.batch = batch;
      this.log = log;
      this.backo = backo;
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
              log.e(error, String.format("Could not upload batch: %s.", batch));
              break;
            default:
              log.e(error, String.format("Could not upload batch: %s.", batch));
              return; // Don't retry
          }
        }

        try {
          backo.sleep(attempts);
          attempts++;
        } catch (InterruptedException e) {
          log.e(e, String.format("Thread interrupted while backing off for batch: %s.", batch));
          return;
        }
      }
    }
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
            log.v("No messages to flush.");
            continue; // we got a hint to flush, but there aren't any messages in the queue
          }

          if (messages.size() >= size || message == FlushMessage.POISON) {
            log.v(String.format("Uploading batch with %s messages.", messages.size()));
            flushExecutor.submit(new UploadBatchTask(service, Batch.create(messages), log, backo));
            messages = new ArrayList<>();
          }
        }
      } catch (InterruptedException e) {
        log.e(e, "Thread interrupted while polling for messages.");
        return; // Stop processing messages
      }
    }
  }
}
