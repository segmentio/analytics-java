package com.segment.analytics.internal;

import com.segment.analytics.Log;
import com.segment.analytics.internal.http.SegmentService;
import com.segment.backo.Backo;
import retrofit.RetrofitError;

class BatchUploadTask implements Runnable {
  private final SegmentService service;
  final Batch batch;
  private final Log log;
  private final Backo backo;

  BatchUploadTask(SegmentService service, Batch batch, Log log, Backo backo) {
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
            log.print(Log.Level.VERBOSE, "Could not upload batch: %s.\n%s", batch,
                error.toString());
            break;
          default:
            log.print(Log.Level.DEBUG, "Could not upload batch: %s.\n%s", batch, error.toString());
            return; // Don't retry
        }
      }

      try {
        backo.sleep(attempts);
        attempts++;
      } catch (InterruptedException e) {
        log.print(Log.Level.ERROR, "Thread interrupted while backing off for batch: %s.", batch);
        return;
      }
    }
  }
}

