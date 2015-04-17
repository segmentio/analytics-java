package com.segment.analytics.internal;

import com.segment.analytics.messages.Message;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

public class FlushMessage implements Message {
  public static final FlushMessage POISON = new FlushMessage();

  private FlushMessage() {
  }

  @Override public Type type() {
    throw new UnsupportedOperationException();
  }

  @Override public UUID messageId() {
    throw new UnsupportedOperationException();
  }

  @Override public Date timestamp() {
    throw new UnsupportedOperationException();
  }

  @Nullable @Override public Map<String, Object> context() {
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
}
