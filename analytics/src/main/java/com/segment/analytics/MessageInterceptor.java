package com.segment.analytics;

import com.segment.analytics.messages.Message;

/** Intercept every message before it is processed in order to add additional data. */
public interface MessageInterceptor {
  /**
   * Called for every message. This will be called on the same thread the request was made.
   * Returning {@code null} will skip processing this message any further.
   */
  public Message intercept(Message message);
}
