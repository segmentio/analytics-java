package com.segment.analytics;

import com.squareup.burst.BurstJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@RunWith(BurstJUnit4.class) public class PayloadTest {

  @SuppressWarnings("UnusedDeclaration") public enum Factories {
    TRACK {
      @Override public Payload create() {
        return TrackPayload.builder("foo").build();
      }
    },
    IDENTIFY {
      @Override public Payload create() {
        return IdentifyPayload.builder().build();
      }
    }, SCREEN {
      @Override public Payload create() {
        return ScreenPayload.builder().build();
      }
    }, GROUP {
      @Override public Payload create() {
        return GroupPayload.builder("bar").build();
      }
    }, ALIAS {
      @Override public Payload create() {
        return AliasPayload.builder("qaz").build();
      }
    };

    public abstract Payload create();

  }

  @Test public void missingUserIdAndAnonymousIdThrowsException(Factories factories) {
    try {
      factories.create();
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessage("Either anonymousId or userId must be provided.");
    }
  }
}
