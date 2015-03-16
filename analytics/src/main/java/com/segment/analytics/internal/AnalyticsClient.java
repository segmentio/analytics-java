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
import java.util.concurrent.ThreadFactory;
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
  private final Thread looperThread;

  public AnalyticsClient(BlockingQueue<Message> messageQueue, SegmentService service, int size,
      Log log, ThreadFactory threadFactory) {
    this.messageQueue = messageQueue;
    this.service = service;
    this.size = size;
    this.log = log;

    looperThread = threadFactory.newThread(new Looper());
    looperThread.start();
  }

  public void enqueue(Message message) {
    messageQueue.add(message);
  }

  public void shutdown() {
    looperThread.interrupt();
    messageQueue.clear();
  }

  class Looper implements Runnable {
    @Override public void run() {
      List<Message> messageList = new ArrayList<>();
      List<Batch> failedBatches = new ArrayList<>();

      try {
        while (true) {
          Message message = messageQueue.take();
          messageList.add(message);

          if (messageList.size() >= size) {
            Batch batch = Batch.create(messageList, CONTEXT, 0);
            boolean shouldRetry = uploadedSuccessfully(batch);
            if (shouldRetry) {
              failedBatches.add(batch);
              return;
            }

            Iterator<Batch> failedBatchesIterator = failedBatches.iterator();
            while (failedBatchesIterator.hasNext()) {
              Batch failedBatch = failedBatchesIterator.next();
              Batch retryBatch = Batch.create(failedBatch.batch(), failedBatch.context(),
                  failedBatch.retryCount() + 1);
              if (uploadedSuccessfully(retryBatch)) {
                failedBatchesIterator.remove();
              } else if (retryBatch.retryCount() > 5) {
                // Give up after 5 retries
                failedBatchesIterator.remove();
              }
            }

            messageList = new ArrayList<>();
          }
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

    /** Returns {@code true} to indicate the batch was successfully uploaded. */
    boolean uploadedSuccessfully(Batch batch) throws InterruptedException {
      if (Thread.currentThread().isInterrupted()) {
        throw new InterruptedException("Thread Interrupted.");
      }
      try {
        UploadResponse response = service.upload(batch);
        return response.success(); // should never return false
      } catch (RetrofitError error) {
        switch (error.getKind()) {
          // todo: kill the thread for unexpected error
          // todo: we should never run into conversion errors, safe to ignore them?
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
}
