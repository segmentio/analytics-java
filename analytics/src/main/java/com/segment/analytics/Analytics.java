package com.segment.analytics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.segment.analytics.internal.Client;
import com.segment.analytics.internal.gson.AutoValueAdapterFactory;
import com.segment.analytics.internal.gson.PayloadTypeTypeAdapter;
import com.segment.analytics.internal.http.SegmentService;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.OkHttpClient;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

public class Analytics {
  private final Client client;

  Analytics(Client client) {
    this.client = client;
  }

  public void track(TrackPayload payload) {
    client.enqueue(payload);
  }

  public void identify(IdentifyPayload payload) {
    client.enqueue(payload);
  }

  public void alias(AliasPayload payload) {
    client.enqueue(payload);
  }

  public void group(GroupPayload payload) {
    client.enqueue(payload);
  }

  public void screen(ScreenPayload payload) {
    client.enqueue(payload);
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

      Client client = new Client(segmentService);

      return new Analytics(client);
    }
  }
}
