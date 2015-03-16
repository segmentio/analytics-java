package com.segment.analytics.internal;

import com.google.common.collect.ImmutableMap;
import com.segment.analytics.Log;
import com.segment.analytics.internal.http.SegmentService;
import com.segment.analytics.internal.http.UploadResponse;
import com.segment.analytics.messages.Message;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import retrofit.RetrofitError;

public class AnalyticsClient {
  private static final Map<String, Object> CONTEXT;

  static {
    ImmutableMap<String, String> library =
        ImmutableMap.of("name", "analytics-java", "version", AnalyticsVersion.get());
    CONTEXT = ImmutableMap.<String, Object>of("library", library);
  }

  private final BlockingQueue<Message> messageQueue;
  private final SegmentService service;
  private final int size;
  private final Log log;
  private final Worker worker;

  public AnalyticsClient(BlockingQueue<Message> messageQueue, SegmentService service, int size,
      Log log) {
    this.messageQueue = messageQueue;
    this.service = service;
    this.size = size;
    this.log = log;

    worker = new Worker();
    worker.start();
  }

  public void enqueue(Message message) {
    messageQueue.add(message);
  }

  public void shutdown() {
    worker.interrupt();
    messageQueue.clear();
  }

  class Worker extends Thread {
    @Override public void run() {
      super.run();

      List<Message> messageList = new ArrayList<>();
      List<Batch> failedBatches = new ArrayList<>();

      try {
        while (true) {
          Message message = messageQueue.take();
          messageList.add(message);

          if (messageList.size() >= size) {
            Batch batch = Batch.create(messageList, CONTEXT, 0);
            if (!upload(batch)) {
              failedBatches.add(batch);
            } else {
              Iterator<Batch> failedBatchesIterator = failedBatches.iterator();
              while (failedBatchesIterator.hasNext()) {
                Batch failed = failedBatchesIterator.next();
                Batch retry =
                    Batch.create(failed.batch(), failed.context(), failed.retryCount() + 1);
                if (upload(retry)) {
                  failedBatchesIterator.remove();
                }
              }
            }

            messageList = new ArrayList<>();
          }
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

    boolean upload(Batch batch) throws InterruptedException {
      if (Thread.currentThread().isInterrupted()) {
        throw new InterruptedException("Thread Interrupted.");
      }
      try {
        UploadResponse response = service.upload(batch);
        if (response.success()) {
          log.d("Uploaded batch.");
        } else {
          log.e(null, String.format("Server rejected batch: %s.", batch));
        }
        // We connected to the server but it rejected our message. Don't retry
        return true;
      } catch (RetrofitError error) {
        switch (error.getKind()) {
          case HTTP:
            log.e(error, String.format("Server rejected batch: %s.", batch));
            // We connected to the server but it rejected our message. Don't retry
            return true;
          default:
            log.e(error, String.format("Could not upload batch: %s.", batch));
            return false;
        }
      }
    }
  }
}
