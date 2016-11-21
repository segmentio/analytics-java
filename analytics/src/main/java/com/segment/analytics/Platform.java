package com.segment.analytics;

import okhttp3.OkHttpClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.MIN_PRIORITY;

class Platform {
  static final String THREAD_NAME = "Analytics";

  private static final Platform PLATFORM = findPlatform();

  static Platform get() {
    return PLATFORM;
  }

  private static Platform findPlatform() {
    return new Platform();
  }

  OkHttpClient defaultClient() {
    return new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build();
  }

  ExecutorService defaultNetworkExecutor() {
    return Executors.newSingleThreadExecutor(defaultThreadFactory());
  }

  ThreadFactory defaultThreadFactory() {
    return new ThreadFactory() {
      @Override public Thread newThread(final Runnable r) {
        return new Thread(new Runnable() {
          @Override public void run() {
            Thread.currentThread().setPriority(MIN_PRIORITY);
            r.run();
          }
        }, THREAD_NAME);
      }
    };
  }

  public long defaultFlushIntervalInMillis() {
    return 10 * 1000; // 10s
  }

  public int defaultFlushQueueSize() {
    return 250;
  }
}
