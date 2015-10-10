package sample;

import com.segment.analytics.Analytics;
import com.segment.analytics.Log;
import com.segment.analytics.messages.TrackMessage;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Sample {
  /** A {@link com.segment.analytics.Log} implementation that logs to {@link System#out}. */
  static final Log STDOUT = new Log() {
    @Override public void print(Level level, String format, Object... args) {
      System.out.println(level + "\t:" + String.format(format, args));
    }

    @Override public void print(Level level, Throwable error, String format, Object... args) {
      System.out.println(level + "\t:" + String.format(format, args));
      System.out.println(error);
    }
  };

  public static void main(String... args) throws Exception {
    final Analytics analytics = Analytics.builder("uFIKMspL0GD0klDBZFlE3mklPVtUgPpd") //
        .log(STDOUT) //
        .build();

    final AtomicInteger count = new AtomicInteger();
    for (int i = 0; i < 10; i++) {
      new Thread() {
        @Override public void run() {
          super.run();
          for (int i = 0; i < 10; i++) {
            Map<String, Object> properties = new LinkedHashMap<>();
            properties.put("count", count.incrementAndGet());
            analytics.enqueue(TrackMessage.builder("Java Test") //
                .properties(properties) //
                .userId("prateek"));
          }
          analytics.flush();
        }
      }.start();
    }

    // Give some time for requests to complete.
    Thread.sleep(TimeUnit.SECONDS.toMillis(30));

    analytics.shutdown();
  }
}