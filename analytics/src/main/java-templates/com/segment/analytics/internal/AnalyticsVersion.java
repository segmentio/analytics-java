package com.segment.analytics.internal;

public final class AnalyticsVersion {
  private AnalyticsVersion() {
    throw new AssertionError("No instances allowed.");
  }

  static String get() {
    return "analytics/${project.version}";
  }
}
