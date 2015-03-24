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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
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
  private final AnalyticsClient client;
  private final List<MessageInterceptor> messageInterceptors;

  Analytics(AnalyticsClient client, List<MessageInterceptor> interceptors) {
    this.client = client;
    if (interceptors == null) {
      messageInterceptors = Collections.emptyList();
    } else {
      messageInterceptors = Collections.unmodifiableList(interceptors);
    }
  }

  /** Enqueue the given message to be uploaded to Segment's servers. */
  public void enqueue(Message message) {
    for (int i = 0, size = messageInterceptors.size(); i < size; i++) {
      message = messageInterceptors.get(i).intercept(message);
      if (message == null) {
        // todo: log
        return;
      }
    }
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
    private List<MessageInterceptor> messageInterceptors;

    /**
     * Start building a new {@link Analytics} instance.
     *
     * @param writeKey Your project write key available on the Segment dashboard.
     */
    public Builder(String writeKey) {
      if (isNullOrEmpty(writeKey)) {
        throw new NullPointerException("category cannot be null or empty.");
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

    public Builder addMessageInterceptor(MessageInterceptor interceptor) {
      if (interceptor == null) {
        throw new IllegalArgumentException("Null interceptor");
      }
      if (messageInterceptors == null) {
        messageInterceptors = new ArrayList<>();
      }
      if (messageInterceptors.contains(interceptor)) {
        throw new IllegalStateException("MessageInterceptor is already registered.");
      }
      messageInterceptors.add(interceptor);
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
          new AnalyticsClient(new LinkedBlockingDeque<Message>(), segmentService, 25, log,
              Executors.defaultThreadFactory(), Executors.newSingleThreadExecutor());

      return new Analytics(analyticsClient, messageInterceptors);
    }
  }
}