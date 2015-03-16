package com.segment.analytics.internal;

public final class Utils {
  private Utils() {
    throw new AssertionError("No instances");
  }

  /** Returns {@code true} if the given string is null or empty. */
  public static boolean isNullOrEmpty(String string) {
    return string == null || string.trim().length() == 0;
  }
}
