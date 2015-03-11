package com.segment.analytics.internal;

final class Version {
  static String version() {
    return "analytics/${project.version}";
  }

  private Version() {
    throw new AssertionError("No instances allowed.");
  }
}
