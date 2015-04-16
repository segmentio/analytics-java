package com.segment.analytics;

import com.segment.analytics.messages.MessageBuilder;

/** Intercept every message before it is built in order to add additional data. */
public interface MessageTransformer {
  /**
   * Called for every builder. This will be called on the same thread the request was made and
   * before any {@link MessageInterceptor}'s are called.
   * Returning {@code false} will skip processing this message any further.
   */
  boolean transform(MessageBuilder builder);
}
