package com.segment.analytics;

import com.segment.analytics.messages.AliasMessage;
import com.segment.analytics.messages.GroupMessage;
import com.segment.analytics.messages.IdentifyMessage;
import com.segment.analytics.messages.Message;
import com.segment.analytics.messages.MessageBuilder;
import com.segment.analytics.messages.ScreenMessage;
import com.segment.analytics.messages.TrackMessage;

/**
 * A {@link MessageInterceptor} that lets you implement more strongly typed methods and add
 * transformations specific to the event type.
 */
public abstract class TypedInterceptor implements MessageInterceptor {

  @Override public final boolean intercept(MessageBuilder builder) {
    // todo: non final so messages can be filtered without duplicating logic?
    Message.Type type = builder.type();
    switch (type) {
      case ALIAS:
        return alias((AliasMessage.Builder) builder);
      case GROUP:
        return group((GroupMessage.Builder) builder);
      case IDENTIFY:
        return identify((IdentifyMessage.Builder) builder);
      case SCREEN:
        return screen((ScreenMessage.Builder) builder);
      case TRACK:
        return track((TrackMessage.Builder) builder);
      default:
        throw new IllegalArgumentException("Unknown payload type: " + type);
    }
  }

  /** Called for every {@link AliasMessage}. */
  abstract boolean alias(AliasMessage.Builder builder);

  /** Called for every {@link GroupMessage}. */
  abstract boolean group(GroupMessage.Builder builder);

  /** Called for every {@link IdentifyMessage}. */
  abstract boolean identify(IdentifyMessage.Builder builder);

  /** Called for every {@link ScreenMessage}. */
  abstract boolean screen(ScreenMessage.Builder builder);

  /** Called for every {@link TrackMessage}. */
  abstract boolean track(TrackMessage.Builder builder);
}
