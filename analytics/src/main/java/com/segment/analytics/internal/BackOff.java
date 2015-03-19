package com.segment.analytics.internal;

import java.util.concurrent.atomic.AtomicInteger;

class BackOff {
  private static final long DEFAULT_INTERVAL = 100; // 100ms
  private static final long DEFAULT_MAX_INTERVAL = 10 * 1000; // 10s
  private static final int DEFAULT_FACTOR = 2;
  private static final double DEFAULT_JITTER = 0;

  private final AtomicInteger attempts;
  private final long duration;
  private final int factor;
  private final double jitter;
  private final long max;

  BackOff(long duration, int factor, double jitter, long max) {
    attempts = new AtomicInteger();
    this.duration = duration;
    this.factor = factor;
    this.jitter = jitter;
    this.max = max;
  }

  static BackOff create() {
    // TODO: pool instances
    return new BackOff(DEFAULT_INTERVAL, DEFAULT_FACTOR, DEFAULT_JITTER, DEFAULT_MAX_INTERVAL);
  }

  void backOff() throws InterruptedException {
    Thread.sleep(duration());
  }

  long duration() {
    long duration = this.duration * (long) Math.pow(factor, attempts.getAndIncrement());
    if (jitter != DEFAULT_JITTER) {
      double random = Math.random();
      int deviation = (int) Math.floor(random * jitter * duration);
      if ((((int) Math.floor(random * 10)) & 1) == 0) {
        duration = duration - deviation;
      } else {
        duration = duration + deviation;
      }
    }
    if (duration < this.duration) {
      duration = Long.MAX_VALUE;
    }
    return Math.min(duration, max);
  }

  void reset() {
    attempts.set(0);
  }
}
