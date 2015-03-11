package sample;

import com.segment.analytics.Analytics;
import com.segment.analytics.TrackPayload;

public class Sample {
  public static void main(String... args) throws Exception {
    System.out.println("boo");
    final Analytics analytics = new Analytics.Builder("uFIKMspL0GD0klDBZFlE3mklPVtUgPpd").build();

    for (int i = 0; i < 10; i++) {
      new Thread() {
        @Override public void run() {
          super.run();
          for (int i = 0; i < 10; i++) {
            analytics.track(
                TrackPayload.builder("Trying out the Java Lib").userId("prateek").build());
          }
        }
      }.start();
    }
  }
}