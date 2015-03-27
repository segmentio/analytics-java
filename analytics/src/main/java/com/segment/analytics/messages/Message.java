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
  public Type type();

  public UUID messageId();

  public Date timestamp();

  @Nullable public Map<String, Object> context();

  @Nullable public UUID anonymousId();

  @Nullable public String userId();

  @Nullable public Map<String, Boolean> integrations();

  public enum Type {
    IDENTIFY, GROUP, TRACK, SCREEN, ALIAS
  }
}
