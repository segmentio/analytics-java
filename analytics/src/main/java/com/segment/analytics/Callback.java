package com.segment.analytics;

import com.segment.analytics.messages.Message;

/**
 * Callback invoked when the client library is done processing a message.
 * <p>
 * Methods may be called on background threads, implementations must implement their own
 * synchronization if needed. Implementations should also take care to make the methods
 * non-blocking.
 */
public interface Callback {
  /**
   * Invoked when the message is successfully uploaded to Segment.
   * <p>
   * Note: The Segment HTTP API itself is asynchronous, so this doesn't indicate whether the
   * message was sent to all integrations or not — just that the message was sent to the Segment
   * API and will be sent to integrations at a later time.
   */
  void success(Message message);

  /**
   * Invoked when the library gives up on sending a message.
   * <p>
   * This could be due to exhausting retries, or other unexpected errors. Use the {@code throwable}
   * provided to take further action.
   */
  void failure(Message message, Throwable throwable);
}
