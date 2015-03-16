package sample;

import com.segment.analytics.Analytics;
import com.segment.analytics.Log;
import com.segment.analytics.messages.TrackMessage;
import java.util.concurrent.atomic.AtomicInteger;

public class Sample {
  public static void main(String... args) throws Exception {
    System.out.println("boo");
    final Analytics analytics =
        new Analytics.Builder("uFIKMspL0GD0klDBZFlE3mklPVtUgPpd").log(Log.STDOUT).build();

    final AtomicInteger count = new AtomicInteger();

    for (int i = 0; i < 10; i++) {
      new Thread() {
        @Override public void run() {
          super.run();
          for (int i = 0; i < 10; i++) {

            analytics.enqueue(TrackMessage.builder("Java Test #" + count.getAndIncrement())
                .userId("prateek")
                .build());
          }
        }
      }.start();
    }
  }
}