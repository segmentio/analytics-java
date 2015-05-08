package com.segment.analytics;

import com.squareup.okhttp.OkHttpClient;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import retrofit.client.Client;
import retrofit.client.OkClient;

import static java.lang.Thread.MIN_PRIORITY;

class Platform {
  static final String THREAD_PREFIX = "Analytics-";
  static final String IDLE_THREAD_NAME = THREAD_PREFIX + "Idle";

  private static final Platform PLATFORM = findPlatform();

  static Platform get() {
    return PLATFORM;
  }

  private static Platform findPlatform() {
    return new Platform();
  }

  Client defaultClient() {
    OkHttpClient client = new OkHttpClient();
    client.setConnectTimeout(15, TimeUnit.SECONDS);
    client.setReadTimeout(15, TimeUnit.SECONDS);
    client.setWriteTimeout(15, TimeUnit.SECONDS);
    return new OkClient(client);
  }

  ExecutorService defaultNetworkExecutor() {
    return Executors.newCachedThreadPool(defaultThreadFactory());
  }

  ThreadFactory defaultThreadFactory() {
    return new ThreadFactory() {
      @Override public Thread newThread(final Runnable r) {
        return new Thread(new Runnable() {
          @Override public void run() {
            Thread.currentThread().setPriority(MIN_PRIORITY);
            r.run();
          }
        }, IDLE_THREAD_NAME);
      }
    };
  }
}
