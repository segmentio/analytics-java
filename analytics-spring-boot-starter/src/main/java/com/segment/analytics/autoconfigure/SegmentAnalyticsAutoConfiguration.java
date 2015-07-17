package com.segment.analytics.autoconfigure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.segment.analytics.Analytics;

/**
 * Spring Boot autoconfiguration class for Segment Analytics.
 *
 * @author Christopher Smith
 */
@Configuration
@EnableConfigurationProperties(SegmentProperties.class)
@ConditionalOnProperty("segment.analytics.writeKey")
public class SegmentAnalyticsAutoConfiguration {

  @Autowired
  private SegmentProperties properties;

  @Bean
  public Analytics segmentAnalytics() {
    return Analytics.builder(properties.getWriteKey()).build();
  }
}
