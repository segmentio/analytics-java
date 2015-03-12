package com.segment.analytics;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.segment.analytics.internal.AnalyticsVersion;
import com.segment.analytics.internal.Batch;
import com.segment.analytics.internal.gson.AutoValueAdapterFactory;
import com.segment.analytics.internal.gson.PayloadTypeTypeAdapter;
import com.segment.analytics.internal.http.SegmentService;
import com.segment.analytics.internal.http.UploadResponse;
import com.segment.analytics.messages.Message;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.OkHttpClient;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Client;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

public class Analytics {
  private static final Logger LOGGER = Logger.getLogger(Analytics.class.getName());
  private static final Map<String, Object> CONTEXT;

  static {
    ImmutableMap<String, String> library =
        ImmutableMap.of("name", "analytics-java", "version", AnalyticsVersion.get());
    CONTEXT = ImmutableMap.<String, Object>of("library", library);
  }

  private final BlockingQueue<Message> messageQueue;
  private final SegmentService service;
  private final int size;

  Analytics(BlockingQueue<Message> messageQueue, SegmentService service, int size) {
    this.messageQueue = messageQueue;
    this.service = service;
    this.size = size;

    new Worker().start();
  }

  public void enqueue(Message message) {
    try {
      messageQueue.put(message);
    } catch (InterruptedException e) {
    }
  }

  public static class Builder {
    private final String writeKey;
    private Client client;

    public Builder(String writeKey) {
      this.writeKey = writeKey;
    }

    public Builder client(Client client) {
      if (client == null) {
        throw new NullPointerException("Null client");
      }
      this.client = client;
      return this;
    }

    public Analytics build() {
      Gson gson = new GsonBuilder() //
          .registerTypeAdapterFactory(new AutoValueAdapterFactory())
          .registerTypeAdapter(Message.Type.class, new PayloadTypeTypeAdapter())
          .create();

      if (client == null) {
        OkHttpClient okHttpClient = new OkHttpClient();
        client = new OkClient(okHttpClient);
      }

      RestAdapter restAdapter = new RestAdapter.Builder().setConverter(new GsonConverter(gson))
          .setEndpoint("https://api.segment.io")
          .setClient(client)
          .setRequestInterceptor(new RequestInterceptor() {
            @Override public void intercept(RequestFacade request) {
              request.addHeader("Authorization", Credentials.basic(writeKey, ""));
            }
          })
          .setLogLevel(RestAdapter.LogLevel.FULL)
          .setLog(new RestAdapter.Log() {
            @Override public void log(String message) {
              LOGGER.log(Level.FINEST, message);
            }
          })
          .build();

      SegmentService segmentService = restAdapter.create(SegmentService.class);

      return new Analytics(new LinkedBlockingDeque<Message>(), segmentService, 200);
    }
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

    boolean upload(Batch batch) {
      try {
        UploadResponse response = service.upload(batch);
        if (response.success()) {
          LOGGER.log(Level.FINEST, "Uploaded batch.");
        } else {
          LOGGER.log(Level.FINEST, "Could not upload batch.");
        }
        return response.success();
      } catch (RetrofitError error) {
        LOGGER.log(Level.FINEST, "Could not upload batch.", error);
        return false;
      }
    }
  }
}
