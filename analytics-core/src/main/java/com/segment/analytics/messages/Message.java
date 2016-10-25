package com.segment.analytics.messages;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Encapsulates properties common to all messages. Although not enforced by the compiler, either
 * the {@link Message#anonymousId} or {@link Message#userId} must be provided.
 * <p>
 * For any functions that accept a map, such as {@link #integrations()}, {@link #context()}, {@link
 * TrackMessage#properties()}, {@link ScreenMessage#properties()}, {@link IdentifyMessage#traits()},
 * or {@link GroupMessage#traits()}, we'll make an internal copy of the map provided. If you use
 * <a href="https://github.com/google/guava">Guava</a>, we'll use it's {@code ImmutableMap#copyOf}
 * methods instead.
 * <p>
 * There are a few limitations to be aware of when using Guava — mainly that null keys or values
 * are not permitted and duplicate keys are not permitted. For more details, refer to Guava's
 * <a href="http://bit.ly/1N5v3Cq">Javadocs</a>.
 */
public interface Message {
  @Nonnull Type type();

  @Nonnull UUID messageId();

  @Nonnull Date timestamp();

  @Nullable Map<String, ?> context();

  @Nullable UUID anonymousId();

  @Nullable String userId();

  @Nullable Map<String, Object> integrations();

  enum Type {
    identify, group, track, screen, page, alias
  }
}
