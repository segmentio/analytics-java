package com.segment.analytics.messages;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

final class ImmutableMap {
  private ImmutableMap() {
    throw new AssertionError("No instances.");
  }

  static final boolean HAS_GUAVA = hasGuavaOnClasspath();

  private static boolean hasGuavaOnClasspath() {
    try {
      Class.forName("com.google.common.collect.ImmutableMap");
      return true;
    } catch (ClassNotFoundException ignored) {
    }
    return false;
  }

  static <K, V> Map<K, V> copyOf(Map<K, V> map) {
    if (HAS_GUAVA) {
      return com.google.common.collect.ImmutableMap.copyOf(map);
    }

    return Collections.unmodifiableMap(new LinkedHashMap<>(map));
  }
}
