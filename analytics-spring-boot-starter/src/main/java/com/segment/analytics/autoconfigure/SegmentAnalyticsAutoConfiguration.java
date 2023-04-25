package com.segment.analytics.autoconfigure;

import com.segment.analytics.Analytics;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot autoconfiguration class for Segment Analytics.
 *
 * @author Christopher Smith
 */
@Configuration
@EnableConfigurationProperties(SegmentProperties.class)
@ConditionalOnProperty("segment.analytics.writeKey")
public class SegmentAnalyticsAutoConfiguration {

  @Autowired private SegmentProperties properties;

  @Bean
  public Analytics segmentAnalytics(ObjectProvider<SegmentAnalyticsCustomizer> customizerProvider) {
    Analytics.Builder builder = Analytics.builder(properties.getWriteKey());
    customizerProvider.orderedStream().forEach((customizer) -> customizer.customize(builder));
    return builder.build();
  }
}
