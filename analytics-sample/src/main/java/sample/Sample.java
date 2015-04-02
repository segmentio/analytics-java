package sample;

import com.google.common.collect.ImmutableMap;
import com.segment.analytics.Analytics;
import com.segment.analytics.Log;
import com.segment.analytics.messages.TrackMessage;
import java.util.concurrent.atomic.AtomicInteger;

public class Sample {
  public static void main(String... args) throws Exception {
    final Analytics analytics =
        Analytics.builder("uFIKMspL0GD0klDBZFlE3mklPVtUgPpd").log(new Log() {
          @Override public void v(String msg) {
            System.out.println(msg);
          }

          @Override public void d(String msg) {
            System.out.println(msg);
          }

          @Override public void e(Throwable e, String msg) {
            System.out.println(msg);
            System.out.println(e.getMessage());
          }
        }).build();

    final AtomicInteger count = new AtomicInteger();
    for (int i = 0; i < 10; i++) {
      new Thread() {
        @Override public void run() {
          super.run();
          for (int i = 0; i < 10; i++) {
            analytics.enqueue(TrackMessage.builder("Java Test")
                .properties(ImmutableMap.<String, Object>of("count", count.incrementAndGet()))
                .userId("prateek")
                .build());
          }
          analytics.flush();
        }
      }.start();
    }
  }
}