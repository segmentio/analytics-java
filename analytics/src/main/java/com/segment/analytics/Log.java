package com.segment.analytics;

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
  /** A {@link Log} implementation which logs to {@link System#out Standard Output}. */
  Log STDOUT = new Log() {
    @Override public void v(String msg) {
      System.out.println(msg);
    }

    @Override public void d(String msg) {
      System.out.println(msg);
    }

    @Override public void e(Throwable throwable, String msg) {
      System.out.println(msg);
      System.out.println(throwable.getMessage());
    }
  };

  void v(String msg);

  void d(String msg);

  void e(Throwable e, String msg);
}
