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

import static com.segment.analytics.internal.Utils.isNullOrEmpty;

/**
 * The entry point into the Segment for Java library.
 * <p/>
 * The idea is simple: one pipeline for all your data. Segment is the single hub to collect,
 * translate and route your data with the flip of a switch.
 * <p/>
 * Analytics for Java will automatically batch events and upload it periodically to Segment's
 * servers for you. You only need to instrument Segment once, then flip a switch to install
 * new tools.
 * <p/>
 * This class is the main entry point into the client API. Use {@link Builder} to construct your
 * own instances.
 *
 * @see <a href="https://Segment/">Segment</a>
 */
public class Analytics {
  final AnalyticsClient client;

  Analytics(AnalyticsClient client) {
    this.client = client;
  }

  /** Enqueue the given message to be uploaded to Segment's servers. */
  public void enqueue(Message message) {
    client.enqueue(message);
  }

  /** Stops this instance from processing further requests. */
  public void shutdown() {
    client.shutdown();
  }

  /** Fluent API for creating {@link Analytics} instances. */
  public static class Builder {
    private final String writeKey;
    private Log log;
    private Client client;

    /**
     * Start building a new {@link Analytics} instance.
     *
     * @param writeKey Your project write key available on the Segment dashboard.
     */
    public Builder(String writeKey) {
      if (isNullOrEmpty(writeKey)) {
        throw new NullPointerException("Empty writeKey");
      }
      this.writeKey = writeKey;
    }

    /** Configure debug logging mechanism. By default, nothing is logged. */
    public Builder log(Log log) {
      if (log == null) {
        throw new NullPointerException("Null log");
      }
      this.log = log;
      return this;
    }

    /** Create a {@link Analytics} client. */
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
          new AnalyticsClient(new LinkedBlockingDeque<Message>(), segmentService, 25, log);

      return new Analytics(analyticsClient);
    }
  }
}
