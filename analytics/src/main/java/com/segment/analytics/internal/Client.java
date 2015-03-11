package com.segment.analytics.internal;

import com.segment.analytics.Payload;
import com.segment.analytics.internal.http.SegmentService;
import com.segment.analytics.internal.http.UploadResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import retrofit.RetrofitError;

public class Client {
  private final SegmentService service;
  private static final Map<String, Object> CONTEXT;
  private final BlockingQueue<Payload> payloadQueue;

  static {
    Map<String, Object> context = new ConcurrentHashMap<>();
    Map<String, Object> library = new ConcurrentHashMap<>();
    library.put("name", "analytics-java");
    library.put("version", Version.version());
    context.put("library", library);

    CONTEXT = Collections.unmodifiableMap(context);
  }

  public Client(SegmentService service) {
    this.service = service;

    payloadQueue = new LinkedBlockingDeque<>();

    new Worker().start();
  }

  public void enqueue(Payload payload) {
    try {
      payloadQueue.put(payload);
    } catch (InterruptedException e) {
      // todo: handle
    }
  }

  class Worker extends Thread {

    @Override public void run() {
      super.run();

      List<Payload> payloadList = new ArrayList<>();
      try {
        while (true) {
          Payload payload = payloadQueue.take();
          payloadList.add(payload);

          if (payloadList.size() >= 20) {

            System.out.println("Uploading 5 payloads:" + payloadList);

            Batch batch = Batch.create(payloadList, CONTEXT);

            try {
              UploadResponse response = service.upload(batch);
              System.out.println(response);
            } catch (RetrofitError error) {
              error.getKind();
            }

            payloadList.clear();
          }
        }
      } catch (InterruptedException e) {
        // todo: handle
      }
    }
  }
}
