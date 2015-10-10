package com.segment.analytics;

import com.segment.analytics.messages.Message;

public interface MessageCallback {
  /** Invoked when a message is successfully uploaded to Segment. */
  void onSuccess(Message message);

  /** Invoked when the client gives up on uploading the message. */
  void onFailure(Message message, Throwable t);
}
