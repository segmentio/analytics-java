package com.segment.analytics;

import com.segment.analytics.messages.AliasMessage;
import com.segment.analytics.messages.GroupMessage;
import com.segment.analytics.messages.IdentifyMessage;
import com.segment.analytics.messages.Message;
import com.segment.analytics.messages.ScreenMessage;
import com.segment.analytics.messages.TrackMessage;

/**
 * A {@link MessageInterceptor} that lets you implement more strongly typed methods and add
 * transformations specific to the event type.
 */
public abstract class TypedInterceptor implements MessageInterceptor {

  @Override public final Message intercept(Message message) {
    // todo: non final so messages can be filtered without duplicating logic?
    Message.Type type = message.type();
    switch (type) {
      case ALIAS:
        return alias((AliasMessage) message);
      case GROUP:
        return group((GroupMessage) message);
      case IDENTIFY:
        return identify((IdentifyMessage) message);
      case SCREEN:
        return screen((ScreenMessage) message);
      case TRACK:
        return track((TrackMessage) message);
      default:
        throw new IllegalArgumentException("Unknown payload type: " + type);
    }
  }

  /** Called for every {@link AliasMessage}. */
  abstract AliasMessage alias(AliasMessage alias);

  /** Called for every {@link GroupMessage}. */
  abstract GroupMessage group(GroupMessage group);

  /** Called for every {@link IdentifyMessage}. */
  abstract IdentifyMessage identify(IdentifyMessage identify);

  /** Called for every {@link ScreenMessage}. */
  abstract ScreenMessage screen(ScreenMessage screen);

  /** Called for every {@link TrackMessage}. */
  abstract TrackMessage track(TrackMessage track);
}
