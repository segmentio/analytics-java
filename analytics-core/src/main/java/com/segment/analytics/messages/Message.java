package com.segment.analytics.messages;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

/**
 * Encapsulates properties common to all messages. Although not enforced by the compiler, either
 * the {@link Message#anonymousId} or {@link Message#userId} must be provided.
 */
public interface Message {
  Type type();

  UUID messageId();

  Date timestamp();

  @Nullable Map<String, ?> context();

  @Nullable UUID anonymousId();

  @Nullable String userId();

  @Nullable Map<String, Object> integrations();

  enum Type {
    IDENTIFY, GROUP, TRACK, SCREEN, ALIAS
  }
}
