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

  /**
   * The AnalyticsClient polls the message queue. Once it polls enough times, it offloads the
   * messages into a batch on a different thread, which uploads it.
   */
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

  static class BackOff {
    void backOff() throws InterruptedException {
      // todo: a real strategy
      Thread.sleep(1000);
    }
  }

  static class UploadBatchTask implements Runnable {
    private final SegmentService service;
    private final Batch batch;
    private final Log log;
    private final BackOff backOff = new BackOff();

    public UploadBatchTask(SegmentService service, Batch batch, Log log) {
      this.service = service;
      this.batch = batch;
      this.log = log;
    }

    @Override public void run() {
      while (true) {
        try {
          backOff.backOff();
          if (upload(batch)) {
            return;
          }
        } catch (InterruptedException ignored) {
        }
      }
    }

    /** Returns {@code true} to indicate the batch was successfully uploaded. */
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
