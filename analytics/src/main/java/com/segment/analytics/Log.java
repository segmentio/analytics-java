package com.segment.analytics;

/** Abstraction for logging messages. */
public interface Log {
  void v(String msg);

  void d(String msg);

  void e(Throwable e, String msg);
}
