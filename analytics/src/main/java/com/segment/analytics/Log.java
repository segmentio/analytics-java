package com.segment.analytics;

/** Abstraction for logging messages. */
public abstract class Log {
  public enum Level {
    VERBOSE, DEBUG, ERROR
  }

  public abstract void print(Level level, String format, Object... args);
}
