package sample;

import com.segment.analytics.Analytics;
import com.segment.analytics.messages.TrackMessage;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import okhttp3.OkHttpClient;

public class Main {
  public static void main(String... args) throws Exception {
    final BlockingFlush blockingFlush = BlockingFlush.create();

    // https://segment.com/segment-engineering/sources/test-java/debugger
    final Analytics analytics =
        Analytics.builder("xemyw6oe3n")
            .plugin(blockingFlush.plugin())
            .plugin(new LoggingPlugin())
            .client(createClient())
            .build();

    final String userId = System.getProperty("user.name");
    final String anonymousId = UUID.randomUUID().toString();

    final AtomicInteger count = new AtomicInteger();
    for (int i = 0; i < 10; i++) {
      for (int j = 0; j < 10; j++) {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("count", count.incrementAndGet());
        analytics.enqueue(
            TrackMessage.builder("Java Test")
                .properties(properties)
                .anonymousId(anonymousId)
                .userId(userId));
      }
    }

    analytics.flush();
    blockingFlush.block();
    analytics.shutdown();
  }

  /**
   * By default, the analytics client uses an HTTP client with sane defaults. However you can
   * customize the client to your needs. For instance, this client is configured to automatically
   * gzip outgoing requests.
   */
  private static OkHttpClient createClient() {
    return new OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(new GzipRequestInterceptor())
        .build();
  }
}
