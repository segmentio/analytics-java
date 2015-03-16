package com.segment.analytics;

import com.segment.analytics.messages.AliasMessage;
import com.segment.analytics.messages.GroupMessage;
import com.segment.analytics.messages.IdentifyMessage;
import com.segment.analytics.messages.Message;
import com.segment.analytics.messages.ScreenMessage;
import com.segment.analytics.messages.TrackMessage;
import com.squareup.burst.BurstJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@RunWith(BurstJUnit4.class) public class MessageTest {

  @Test public void missingUserIdAndAnonymousIdThrowsException(Factories factories) {
    try {
      factories.create();
      fail();
    } catch (IllegalStateException e) {
      assertThat(e).hasMessage("Either anonymousId or userId must be provided.");
    }
  }

  @Test public void aliasBuilder() {
    try {
      AliasMessage.builder(null);
      fail();
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("previousId cannot be null or empty.");
    }
  }

  @Test public void groupBuilder() {
    try {
      GroupMessage.builder(null);
      fail();
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("groupId cannot be null or empty.");
    }

    try {
      GroupMessage.builder("foo").traits(null);
      fail();
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("Null traits");
    }
  }

  @Test public void identifyBuilder() {
    try {
      IdentifyMessage.builder().traits(null);
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("Null traits");
    }

    try {
      IdentifyMessage.builder().userId("foo").build();
    } catch (IllegalStateException e) {
      assertThat(e).hasMessage("Either userId or traits must be provided.");
    }
  }

  @Test public void screenBuilder() {
    try {
      ScreenMessage.builder().properties(null).build();
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("Null properties");
    }

    try {
      ScreenMessage.builder().userId("foo").build();
    } catch (IllegalStateException e) {
      assertThat(e).hasMessage("Either name or category must be provided.");
    }
  }

  @Test public void trackBuilder() {
    try {
      TrackMessage.builder(null);
      fail();
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("event cannot be null or empty.");
    }

    try {
      TrackMessage.builder("foo").properties(null);
      fail();
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("Null properties");
    }
  }

  @SuppressWarnings("UnusedDeclaration") public enum Factories {
    ALIAS {
      @Override public Message create() {
        return AliasMessage.builder("foo").build();
      }
    }, GROUP {
      @Override public Message create() {
        return GroupMessage.builder("bar").build();
      }
    },
    IDENTIFY {
      @Override public Message create() {
        return IdentifyMessage.builder().build();
      }
    }, SCREEN {
      @Override public Message create() {
        return ScreenMessage.builder().name("baz").build();
      }
    }, TRACK {
      @Override public Message create() {
        return TrackMessage.builder("qux").build();
      }
    };

    public abstract Message create();
  }
}
