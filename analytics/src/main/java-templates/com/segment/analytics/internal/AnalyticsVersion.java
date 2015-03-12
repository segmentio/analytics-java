package com.segment.analytics.internal;

public final class AnalyticsVersion {
  public static String get() {
    return "analytics/${project.version}";
  }

  private AnalyticsVersion() {
    throw new AssertionError("No instances allowed.");
  }
}
