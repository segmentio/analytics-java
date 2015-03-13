package com.segment.analytics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.segment.analytics.internal.AnalyticsClient;
import com.segment.analytics.internal.gson.AutoValueAdapterFactory;
import com.segment.analytics.internal.gson.PayloadTypeTypeAdapter;
import com.segment.analytics.internal.http.SegmentService;
import com.segment.analytics.messages.Message;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.OkHttpClient;
import java.util.concurrent.LinkedBlockingDeque;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.Client;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

public class Analytics {
  final AnalyticsClient client;

  Analytics(AnalyticsClient client) {
    this.client = client;
  }

  public void enqueue(Message message) {
    client.enqueue(message);
  }

  public static class Builder {
    private final String writeKey;
    private Log log;
    private Client client;

    public Builder(String writeKey) {
      this.writeKey = writeKey;
    }

    public Builder log(Log log) {
      if (log == null) {
        throw new NullPointerException("Null log");
      }
      this.log = log;
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

      if (log == null) {
        log = Log.NONE;
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
              log.v(message);
            }
          })
          .build();

      SegmentService segmentService = restAdapter.create(SegmentService.class);

      AnalyticsClient analyticsClient =
          new AnalyticsClient(new LinkedBlockingDeque<Message>(), segmentService, 200, log);

      return new Analytics(analyticsClient);
    }
  }
}
