package com.segment.analytics;

import com.segment.analytics.messages.Message;

/** Intercept every message after it is built to process it further. */
public interface MessageInterceptor {
  /**
   * Called for every message. This will be called on the same thread the request was made and
   * after all {@link MessageTransformer}'s have been called.
   * Returning {@code null} will skip processing this message any further.
   */
  Message intercept(Message message);
}
