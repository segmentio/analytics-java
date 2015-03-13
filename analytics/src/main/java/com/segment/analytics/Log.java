package com.segment.analytics;

import java.util.logging.Level;
import java.util.logging.Logger;

public interface Log {
  /** A {@link Log} implementation which does not log anything. */
  Log NONE = new Log() {
    @Override public void v(String msg) {

    }

    @Override public void d(String msg) {

    }

    @Override public void e(Throwable e, String msg) {

    }
  };

  /** A {@link Log} implementation which logs to a {@link Logger} instance. */
  Log DEFAULT = new Log() {
    final Logger logger = Logger.getLogger("Analytics");

    @Override public void v(String msg) {
      logger.log(Level.FINE, msg);
    }

    @Override public void d(String msg) {
      logger.log(Level.INFO, msg);
    }

    @Override public void e(Throwable throwable, String msg) {
      logger.log(Level.SEVERE, msg, throwable);
    }
  };

  void v(String msg);

  void d(String msg);

  void e(Throwable e, String msg);
}
