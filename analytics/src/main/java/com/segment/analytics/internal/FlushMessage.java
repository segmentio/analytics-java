package com.segment.analytics.internal;

import com.segment.analytics.messages.Message;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

class FlushMessage implements Message {
  static final FlushMessage POISON = new FlushMessage();

  private FlushMessage() {
  }

  @Override public Type type() {
    throw new UnsupportedOperationException();
  }

  @Override public String messageId() {
    throw new UnsupportedOperationException();
  }

  @Override public Date timestamp() {
    throw new UnsupportedOperationException();
  }

  @Nullable @Override public Map<String, ?> context() {
    throw new UnsupportedOperationException();
  }

  @Nullable @Override public UUID anonymousId() {
    throw new UnsupportedOperationException();
  }

  @Nullable @Override public String userId() {
    throw new UnsupportedOperationException();
  }

  @Nullable @Override public Map<String, Object> integrations() {
    throw new UnsupportedOperationException();
  }

  @Override public String toString() {
    return "FlushMessage{}";
  }
}
