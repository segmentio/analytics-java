package com.segment.analytics.internal;

import com.segment.analytics.Log;
import com.segment.analytics.internal.http.SegmentService;
import com.segment.analytics.internal.http.UploadResponse;
import com.segment.analytics.messages.Message;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
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
    private final Backo backo = new Backo.Builder().build();

    public UploadBatchTask(SegmentService service, Batch batch, Log log) {
      this.service = service;
      this.batch = batch;
      this.log = log;
    }

    @Override public void run() {
      int attempts = 0;

      while (true) {
        if (upload(batch)) {
          break;
        }

        attempts++;
        if (attempts > 5) {
          log.e(null, String.format("Giving up on batch: %s.", batch));
          break;
        }

        try {
          backo.backOff();
        } catch (InterruptedException e) {
          log.e(e, String.format("Thread interrupted while backing off for batch: %s.", batch));
          break;
        }
      }
    }

    /** Returns {@code false} to indicate the batch should be retried. */
    boolean upload(Batch batch) {
      try {
        UploadResponse response = service.upload(batch);
        return response.success(); // should never return false
      } catch (RetrofitError error) {
        switch (error.getKind()) {
          case HTTP:
            log.e(error, String.format("Server rejected batch: %s.", batch));
            return true; // We connected to the server but it rejected our message. Don't retry
          default:
            log.e(error, String.format("Could not upload batch: %s.", batch));
            return false;
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
        Thread.currentThread().interrupt();
      }
    }
  }
}
