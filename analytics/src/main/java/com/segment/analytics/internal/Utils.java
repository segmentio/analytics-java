package com.segment.analytics.internal;

import com.segment.analytics.Payload;

public final class Utils {
  private Utils() {
    throw new AssertionError("No instances allowed.");
  }

  public static void validate(Payload payload) {
    if (payload.anonymousId() == null && payload.userId() == null) {
      throw new IllegalArgumentException("Either anonymousId or userId must be provided.");
    }
  }
}
