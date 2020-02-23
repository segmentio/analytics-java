package com.segment.analytics;

import static java.lang.Thread.MIN_PRIORITY;

import com.jakewharton.retrofit.Ok3Client;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import okhttp3.OkHttpClient;
import retrofit.client.Client;

class Platform {
  static final String THREAD_NAME = "Analytics";

  private static final Platform PLATFORM = findPlatform();

  static Platform get() {
    return PLATFORM;
  }

  private static Platform findPlatform() {
    return new Platform();
  }

  Client defaultClient() {
    OkHttpClient client =
        new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build();
    return new Ok3Client(client);
  }

  ExecutorService defaultNetworkExecutor() {
    //    return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new SynchronousQueue()),
    //      defaultThreadFactory());
    return Executors.newSingleThreadExecutor(defaultThreadFactory());
  }

  ThreadFactory defaultThreadFactory() {
    return new ThreadFactory() {
      @Override
      public Thread newThread(final Runnable r) {
        return new Thread(
            new Runnable() {
              @Override
              public void run() {
                Thread.currentThread().setPriority(MIN_PRIORITY);
                r.run();
              }
            },
            THREAD_NAME);
      }
    };
  }

  public int defaultMaxQueueSize() {
    return 2147483647;
  }

  public long defaultFlushIntervalInMillis() {
    return 10 * 1000; // 10s
  }

  public int defaultFlushQueueSize() {
    return 250;
  }
}
