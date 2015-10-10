package com.segment.analytics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.segment.analytics.gson.AutoValueAdapterFactory;
import com.segment.analytics.http.SegmentService;
import com.segment.analytics.internal.AnalyticsClient;
import com.segment.analytics.messages.Message;
import com.segment.analytics.messages.MessageBuilder;
import com.squareup.okhttp.Credentials;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.Client;
import retrofit.converter.GsonConverter;

/**
 * The entry point into the Segment for Java library.
 * <p>
 * The idea is simple: one pipeline for all your data. Segment is the single hub to collect,
 * translate and route your data with the flip of a switch.
 * <p>
 * Analytics for Java will automatically batch events and upload it periodically to Segment's
 * servers for you. You only need to instrument Segment once, then flip a switch to install
 * new tools.
 * <p>
 * This class is the main entry point into the client API. Use {@link #builder} to construct your
 * own instances.
 *
 * @see <a href="https://Segment/">Segment</a>
 */
public class Analytics {
  private final AnalyticsClient client;
  private final List<MessageTransformer> messageTransformers;
  private final List<MessageInterceptor> messageInterceptors;
  private final Log log;

  Analytics(AnalyticsClient client, List<MessageTransformer> messageTransformers,
      List<MessageInterceptor> messageInterceptors, Log log) {
    this.client = client;
    this.messageTransformers = messageTransformers;
    this.messageInterceptors = messageInterceptors;
    this.log = log;
  }

  /**
   * Start building an {@link Analytics} instance.
   *
   * @param writeKey Your project write key available on the Segment dashboard.
   */
  public static Builder builder(String writeKey) {
    return new Builder(writeKey);
  }

  /** Enqueue the given message to be uploaded to Segment's servers. */
  public void enqueue(MessageBuilder builder) {
    for (MessageTransformer messageTransformer : messageTransformers) {
      boolean shouldContinue = messageTransformer.transform(builder);
      if (!shouldContinue) {
        log.print(Log.Level.VERBOSE, "Skipping message %s.", builder);
        return;
      }
    }
    Message message = builder.build();
    for (MessageInterceptor messageInterceptor : messageInterceptors) {
      message = messageInterceptor.intercept(message);
      if (message == null) {
        log.print(Log.Level.VERBOSE, "Skipping message %s.", builder);
        return;
      }
    }
    client.enqueue(message);
  }

  /** Flush events in the message queue. */
  public void flush() {
    client.flush();
  }

  /** Stops this instance from processing further requests. */
  public void shutdown() {
    client.shutdown();
  }

  /** Fluent API for creating {@link Analytics} instances. */
  public static class Builder {
    private static final String DEFAULT_ENDPOINT = "https://api.segment.io";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final String writeKey;
    private Client client;
    private Log log;
    private List<MessageTransformer> messageTransformers;
    private List<MessageInterceptor> messageInterceptors;
    private ExecutorService networkExecutor;
    private ThreadFactory threadFactory;
    private int flushQueueSize;
    private long flushIntervalInMillis;
    private MessageCallback callback;

    Builder(String writeKey) {
      if (writeKey == null || writeKey.trim().length() == 0) {
        throw new NullPointerException("writeKey cannot be null or empty.");
      }
      this.writeKey = writeKey;
    }

    /** Set a custom networking client. */
    public Builder client(Client client) {
      if (client == null) {
        throw new NullPointerException("Null client");
      }
      this.client = client;
      return this;
    }

    /** Configure debug logging mechanism. By default, nothing is logged. */
    public Builder log(Log log) {
      if (log == null) {
        throw new NullPointerException("Null log");
      }
      this.log = log;
      return this;
    }

    /** Add a {@link MessageTransformer} for transforming messages. */
    @Beta public Builder messageTransformer(MessageTransformer transformer) {
      if (transformer == null) {
        throw new NullPointerException("Null transformer");
      }
      if (messageTransformers == null) {
        messageTransformers = new ArrayList<>();
      }
      if (messageTransformers.contains(transformer)) {
        throw new IllegalStateException("MessageTransformer is already registered.");
      }
      messageTransformers.add(transformer);
      return this;
    }

    /** Add a {@link MessageInterceptor} for intercepting messages. */
    @Beta public Builder messageInterceptor(MessageInterceptor interceptor) {
      if (interceptor == null) {
        throw new NullPointerException("Null interceptor");
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

    /** Set the queueSize at which flushes should be triggered. */
    @Beta public Builder flushQueueSize(int flushQueueSize) {
      if (flushQueueSize < 1) {
        throw new IllegalArgumentException("flushQueueSize must not be less than 1.");
      }
      this.flushQueueSize = flushQueueSize;
      return this;
    }

    /** Set the interval at which the queue should be flushed. */
    @Beta public Builder flushInterval(long flushInterval, TimeUnit unit) {
      long flushIntervalInMillis = unit.toMillis(flushInterval);
      if (flushIntervalInMillis < 1000) {
        throw new IllegalArgumentException("flushInterval must not be less than 1 second.");
      }
      this.flushIntervalInMillis = flushIntervalInMillis;
      return this;
    }

    /** Set the {@link ExecutorService} on which all HTTP requests will be made. */
    public Builder networkExecutor(ExecutorService networkExecutor) {
      if (networkExecutor == null) {
        throw new NullPointerException("Null networkExecutor");
      }
      this.networkExecutor = networkExecutor;
      return this;
    }

    /** Set the {@link ThreadFactory} used to create threads. */
    @Beta Builder threadFactory(ThreadFactory threadFactory) {
      if (threadFactory == null) {
        throw new NullPointerException("Null threadFactory");
      }
      this.threadFactory = threadFactory;
      return this;
    }

    /** Set the {@link MessageCallback} to be notified when a message is processed. */
    @Beta Builder messageCallback(MessageCallback callback) {
      if (callback == null) {
        throw new NullPointerException("Null callback");
      }
      this.callback = callback;
      return this;
    }

    /** Create a {@link Analytics} client. */
    public Analytics build() {
      Gson gson = new GsonBuilder() //
          .registerTypeAdapterFactory(new AutoValueAdapterFactory()) //
          .create();

      if (client == null) {
        client = Platform.get().defaultClient();
      }
      if (log == null) {
        log = Log.NONE;
      }
      if (flushIntervalInMillis == 0) {
        flushIntervalInMillis = Platform.get().defaultFlushIntervalInMillis();
      }
      if (flushQueueSize == 0) {
        flushQueueSize = Platform.get().defaultFlushQueueSize();
      }
      if (messageTransformers == null) {
        messageTransformers = Collections.emptyList();
      } else {
        messageTransformers = Collections.unmodifiableList(messageTransformers);
      }
      if (messageInterceptors == null) {
        messageInterceptors = Collections.emptyList();
      } else {
        messageInterceptors = Collections.unmodifiableList(messageInterceptors);
      }
      if (networkExecutor == null) {
        networkExecutor = Platform.get().defaultNetworkExecutor();
      }
      if (threadFactory == null) {
        threadFactory = Platform.get().defaultThreadFactory();
      }

      RestAdapter restAdapter = new RestAdapter.Builder().setConverter(new GsonConverter(gson))
          .setEndpoint(DEFAULT_ENDPOINT)
          .setClient(client)
          .setRequestInterceptor(new RequestInterceptor() {
            @Override public void intercept(RequestFacade request) {
              request.addHeader(AUTHORIZATION_HEADER, Credentials.basic(writeKey, ""));
            }
          })
          .setLogLevel(RestAdapter.LogLevel.FULL)
          .setLog(new RestAdapter.Log() {
            @Override public void log(String message) {
              log.print(Log.Level.VERBOSE, message);
            }
          })
          .build();

      SegmentService segmentService = restAdapter.create(SegmentService.class);

      AnalyticsClient analyticsClient =
          AnalyticsClient.create(segmentService, flushQueueSize, flushIntervalInMillis, log,
              threadFactory, networkExecutor, callback);
      return new Analytics(analyticsClient, messageTransformers, messageInterceptors, log);
    }
  }
}
