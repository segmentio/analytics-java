package com.segment.analytics;

/** Abstraction for logging messages. */
public interface Log {
  enum Level {
    VERBOSE, DEBUG, ERROR
  }

  void print(Level level, String format, Object... args);

  void print(Level level, Throwable error, String format, Object... args);

  /** A {@link Log} implementation which does nothing. */
  Log NONE = new Log() {
    @Override public void print(Level level, String format, Object... args) {

    }

    @Override public void print(Level level, Throwable error, String format, Object... args) {

    }
  };
}
