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

  public AnalyticsClient(BlockingQueue<Message> messageQueue, SegmentService service, int size,
      Log log, ThreadFactory looperThreadFactory, ExecutorService flushExecutor) {
    this.messageQueue = messageQueue;
    this.service = service;
    this.size = size;
    this.log = log;
    this.flushExecutor = flushExecutor;

    looperThread = looperThreadFactory.newThread(new Looper());
    looperThread.start();
  }

  public void enqueue(Message message) {
    messageQueue.add(message);
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
    private Backo backo;

    public UploadBatchTask(SegmentService service, Batch batch, Log log) {
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
        } catch (RetrofitError error) {
          switch (error.getKind()) {
            case HTTP:
              log.e(error, String.format("Server rejected batch: %s.", batch));
              return; // Don't retry
            default:
              log.e(error, String.format("Could not upload batch: %s.", batch));
          }
        }

        attempts++;
        if (attempts > 5) {
          log.e(null, String.format("Giving up on batch: %s.", batch));
          return;
        }

        if (backo == null) {
          // todo: pool backo instances
          backo = new Backo.Builder().base(TimeUnit.SECONDS, 30).jitter(1).build();
        }

        try {
          backo.backOff();
        } catch (InterruptedException e) {
          log.e(e, String.format("Thread interrupted while backing off for batch: %s.", batch));
          return;
        }
      }
    }
  }

  class Looper implements Runnable {
    @Override public void run() {
      List<Message> messages = new ArrayList<>();
      try {
        while (true) {
          Message message = messageQueue.take();
          messages.add(message);

          if (messages.size() >= size) {
            flushExecutor.submit(new UploadBatchTask(service, Batch.create(messages), log));
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
