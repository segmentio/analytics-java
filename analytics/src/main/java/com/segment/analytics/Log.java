package com.segment.analytics;

import java.io.PrintWriter;
import java.io.StringWriter;

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

  /** A {@link Log} implementation that logs to {@link System#out}. */
  Log STDOUT = new Log() {
    @Override public void print(Level level, String format, Object... args) {
      System.out.println(level + ":\t" + String.format(format, args));
    }

    @Override public void print(Level level, Throwable error, String format, Object... args) {
      StringWriter stringWriter = new StringWriter();
      error.printStackTrace(new PrintWriter(stringWriter));
      String stackTrace = stringWriter.toString();
      System.out.println(level + ":\t" + String.format(format, args) + " Error: " + stackTrace);
    }
  };
}
