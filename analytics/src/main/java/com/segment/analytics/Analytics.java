package com.segment.analytics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.segment.analytics.internal.AnalyticsVersion;
import com.segment.analytics.internal.Batch;
import com.segment.analytics.internal.gson.AutoValueAdapterFactory;
import com.segment.analytics.internal.gson.PayloadTypeTypeAdapter;
import com.segment.analytics.internal.http.SegmentService;
import com.segment.analytics.internal.http.UploadResponse;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.OkHttpClient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

public class Analytics {
  private final SegmentService service;
  private static final Map<String, Object> CONTEXT;
  private final BlockingQueue<Payload> payloadQueue;

  static {
    Map<String, Object> context = new ConcurrentHashMap<>();
    Map<String, Object> library = new ConcurrentHashMap<>();
    library.put("name", "analytics-java");
    library.put("version", AnalyticsVersion.get());
    context.put("library", library);
    CONTEXT = Collections.unmodifiableMap(context);
  }

  public Analytics(SegmentService service) {
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

  public static class Builder {
    private final String writeKey;

    public Builder(String writeKey) {
      this.writeKey = writeKey;
    }

    public Analytics build() {
      Gson gson = new GsonBuilder() //
          .registerTypeAdapterFactory(new AutoValueAdapterFactory())
          .registerTypeAdapter(Payload.Type.class, new PayloadTypeTypeAdapter())
          .create();

      OkHttpClient okHttpClient = new OkHttpClient();

      RestAdapter restAdapter = new RestAdapter.Builder().setConverter(new GsonConverter(gson))
          .setEndpoint("https://api.segment.io")
          .setClient(new OkClient(okHttpClient))
          .setRequestInterceptor(new RequestInterceptor() {
            @Override public void intercept(RequestFacade request) {
              request.addHeader("Authorization", Credentials.basic(writeKey, ""));
            }
          })
          .setLogLevel(RestAdapter.LogLevel.FULL)
          .build();

      SegmentService segmentService = restAdapter.create(SegmentService.class);

      return new Analytics(segmentService);
    }
  }
}
