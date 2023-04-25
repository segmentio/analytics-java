package com.segment.analytics.autoconfigure;

import com.segment.analytics.Analytics;

/**
 * Callback interface that can be used to customize a {@link com.segment.analytics.Analytics.Builder
 * Analytics.Builder}.
 *
 * @author Koen Punt
 */
@FunctionalInterface
public interface SegmentAnalyticsCustomizer {

  /**
   * Callback to customize a {@link com.segment.analytics.Analytics.Builder Analytics.Builder}
   * instance.
   *
   * @param analyticsBuilder the analytics builder to customize
   */
  void customize(Analytics.Builder analyticsBuilder);
}
