package com.segment.analytics.internal;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Backo implements the "full jitter" backoff policy as described in this article <a
 * href="http://www.awsarchitectureblog.com/2015/03/backoff.html">Exponential Backoff And
 * Jitter </a>.
 *
 * @see <a href="http://www.awsarchitectureblog.com/2015/03/backoff.html">Exponential Backoff And
 * Jitter
 * </a>
 */
// TODO: Use our published library
public class Backo {
  private static final long DEFAULT_BASE = 100; // 100ms
  private static final int DEFAULT_FACTOR = 2;
  private static final double DEFAULT_JITTER = 0;
  private static final long DEFAULT_CAP = Long.MAX_VALUE;

  private final AtomicInteger attempts;
  private final long base;
  private final int factor;
  private final double jitter;
  private final long cap;

  Backo(long base, int factor, double jitter, long cap) {
    attempts = new AtomicInteger();
    this.base = base;
    this.factor = factor;
    this.jitter = jitter;
    this.cap = cap;
  }

  /** Return a builder to construct instances of {@link Backo}. */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Sleeps for the duration returned by {@link #duration}.
   *
   * @throws InterruptedException
   */
  public void backOff() throws InterruptedException {
    Thread.sleep(duration());
  }

  long duration() {
    long duration = base * (long) Math.pow(factor, attempts.getAndIncrement());
    if (jitter != 0) {
      double random = Math.random();
      int deviation = (int) Math.floor(random * jitter * duration);
      if ((((int) Math.floor(random * 10)) & 1) == 0) {
        duration = duration - deviation;
      } else {
        duration = duration + deviation;
      }
    }
    if (duration < base) {
      duration = Long.MAX_VALUE;
    }
    return Math.min(duration, cap);
  }

  void reset() {
    attempts.set(0);
  }

  public static class Builder {
    private long base = DEFAULT_BASE;
    private int factor = DEFAULT_FACTOR;
    private double jitter = DEFAULT_JITTER;
    private long cap = DEFAULT_CAP;

    Builder() {
    }

    /** Set the initial backoff interval. Defaults to {@code 100ms}. */
    public Builder base(TimeUnit timeUnit, long duration) {
      this.base = timeUnit.toMillis(duration);
      return this;
    }

    /** Set the backoff factor. Defaults to {@code 2}. */
    public Builder factor(int factor) {
      // Disallow factor of 1?
      this.factor = factor;
      return this;
    }

    /** Set the backoff jitter. Defaults to {@code 0}. */
    public Builder jitter(int jitter) {
      this.jitter = jitter;
      return this;
    }

    /** Set the maximum backoff. Defaults to {@link Long#MAX_VALUE}. */
    public Builder cap(TimeUnit timeUnit, long duration) {
      this.cap = timeUnit.toMillis(duration);
      return this;
    }

    /** Build a {@link Backo} instance. */
    public Backo build() {
      if (cap < base) {
        throw new IllegalStateException("Initial backoff cannot be more than maximum.");
      }
      return new Backo(base, factor, jitter, cap);
    }
  }
}
