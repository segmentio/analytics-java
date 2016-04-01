package com.segment.analytics;

import com.segment.analytics.messages.AliasMessage;
import com.segment.analytics.messages.GroupMessage;
import com.segment.analytics.messages.IdentifyMessage;
import com.segment.analytics.messages.Message;
import com.segment.analytics.messages.MessageBuilder;
import com.segment.analytics.messages.PageMessage;
import com.segment.analytics.messages.ScreenMessage;
import com.segment.analytics.messages.TrackMessage;

public final class TestUtils {
  private TestUtils() {
    throw new AssertionError("No instances.");
  }

  @SuppressWarnings("UnusedDeclaration") public enum MessageBuilderTest {
    ALIAS {
      @Override public AliasMessage.Builder get() {
        return AliasMessage.builder("foo");
      }
    }, GROUP {
      @Override public GroupMessage.Builder get() {
        return GroupMessage.builder("foo");
      }
    },
    IDENTIFY {
      @Override public IdentifyMessage.Builder get() {
        return IdentifyMessage.builder();
      }
    }, SCREEN {
      @Override public ScreenMessage.Builder get() {
        return ScreenMessage.builder("foo");
      }
    }, PAGE {
      @Override public PageMessage.Builder get() {
        return PageMessage.builder("foo");
      }
    }, TRACK {
      @Override public TrackMessage.Builder get() {
        return TrackMessage.builder("foo");
      }
    };

    public abstract <T extends Message, V extends MessageBuilder> MessageBuilder<T, V> get();
  }
}
