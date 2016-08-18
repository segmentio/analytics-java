package sample;

import com.segment.analytics.Analytics;
import com.segment.analytics.Callback;
import com.segment.analytics.Log;
import com.segment.analytics.messages.Message;
import com.segment.analytics.messages.TrackMessage;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
  /** A {@link com.segment.analytics.Log} implementation that logs to {@link System#out}. */
  static final Log STDOUT = new Log() {
    @Override public void print(Level level, String format, Object... args) {
      System.out.println(new Date().toString() + "\t" + level + ":\t" + String.format(format, args));
    }

    @Override public void print(Level level, Throwable error, String format, Object... args) {
      System.out.println(new Date().toString() + "\t" +  level + ":\t" + String.format(format, args));
      System.out.println(error);
    }
  };

  /** A {@link com.segment.analytics.Callback} implementation that prints to {@link System#out}. */
  static final Callback CALLBACK = new Callback() {
    @Override public void success(Message message) {
      System.out.println("Uploaded " + message);
    }

    @Override public void failure(Message message, Throwable throwable) {
      System.out.println("Could not upload " + message);
      System.out.println(throwable);
    }
  };

  public static void main(String... args) throws Exception {
    final BlockingFlush blockingFlush = BlockingFlush.create();

    // https://segment.com/segment-engineering/sources/test-java/debugger
    final Analytics analytics = Analytics.builder("xemyw6oe3n") //
        .log(STDOUT) //
        .callback(CALLBACK) //
        .plugin(blockingFlush.plugin())
        .build();

    final String userId = System.getProperty("user.name");
    final UUID anonymousId = UUID.randomUUID();

    final AtomicInteger count = new AtomicInteger();
    for (int i = 0; i < 10; i++) {
      for (int j = 0; j < 10; j++) {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("count", count.incrementAndGet());
        analytics.enqueue(TrackMessage.builder("Java Test") //
            .properties(properties) //
            .anonymousId(anonymousId) //
            .userId(userId));
      }
    }

    analytics.flush();
    blockingFlush.block();
    analytics.shutdown();
  }
}