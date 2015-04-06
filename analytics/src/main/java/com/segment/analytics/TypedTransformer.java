package com.segment.analytics;

import com.segment.analytics.messages.AliasMessage;
import com.segment.analytics.messages.GroupMessage;
import com.segment.analytics.messages.IdentifyMessage;
import com.segment.analytics.messages.Message;
import com.segment.analytics.messages.MessageBuilder;
import com.segment.analytics.messages.ScreenMessage;
import com.segment.analytics.messages.TrackMessage;

/**
 * A {@link MessageTransformer} that lets you implement more strongly typed methods and add
 * transformations specific to the event type.
 */
public abstract class TypedTransformer implements MessageTransformer {

  @Override public final boolean transform(MessageBuilder builder) {
    // todo: non final so messages can be filtered without duplicating logic?
    Message.Type type = builder.type();
    switch (type) {
      case ALIAS:
        return transformAlias((AliasMessage.Builder) builder);
      case GROUP:
        return transformGroup((GroupMessage.Builder) builder);
      case IDENTIFY:
        return transformIdentify((IdentifyMessage.Builder) builder);
      case SCREEN:
        return transformScreen((ScreenMessage.Builder) builder);
      case TRACK:
        return transformTrack((TrackMessage.Builder) builder);
      default:
        throw new IllegalArgumentException("Unknown payload type: " + type);
    }
  }

  /** Called for every {@link AliasMessage}. */
  abstract boolean transformAlias(AliasMessage.Builder builder);

  /** Called for every {@link GroupMessage}. */
  abstract boolean transformGroup(GroupMessage.Builder builder);

  /** Called for every {@link IdentifyMessage}. */
  abstract boolean transformIdentify(IdentifyMessage.Builder builder);

  /** Called for every {@link ScreenMessage}. */
  abstract boolean transformScreen(ScreenMessage.Builder builder);

  /** Called for every {@link TrackMessage}. */
  abstract boolean transformTrack(TrackMessage.Builder builder);
}
