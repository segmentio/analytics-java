package com.segment.analytics;

import com.segment.analytics.messages.AliasMessage;
import com.segment.analytics.messages.GroupMessage;
import com.segment.analytics.messages.IdentifyMessage;
import com.segment.analytics.messages.Message;
import com.segment.analytics.messages.MessageBuilder;
import com.segment.analytics.messages.ScreenMessage;
import com.segment.analytics.messages.TrackMessage;

/** Intercept every message before it is built in order to add additional data. */
public interface MessageTransformer {
  /**
   * Called for every builder. This will be called on the same thread the request was made and
   * before any {@link MessageInterceptor}'s are called.
   * <p></p>
   * Returning {@code false} will skip processing this message any further.
   */
  boolean transform(MessageBuilder builder);

  /**
   * A {@link MessageTransformer} that lets you implement more strongly typed methods and add
   * transformations specific to the event type.
   */
  abstract class Typed implements MessageTransformer {

    @Override public final boolean transform(MessageBuilder builder) {
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
}
