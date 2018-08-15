package com.segment.analytics.messages;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

public class IdentifyMessageTest {

  @Test
  public void invalidTraitsThrows() {
    try {
      IdentifyMessage.builder().traits(null);
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("Null traits");
    }
  }

  @Test
  public void userIdOrTraitsAreRequired() {
    try {
      IdentifyMessage.builder().userId("userId").build();
    } catch (IllegalStateException e) {
      assertThat(e).hasMessage("Either userId or traits must be provided.");
    }
  }

  @Test
  public void toBuilder() {
    IdentifyMessage original =
        IdentifyMessage.builder().traits(ImmutableMap.of("foo", "bar")).userId("userId").build();
    IdentifyMessage copy = original.toBuilder().build();

    assertThat(copy.userId()).isEqualTo("userId");
    assertThat(copy.traits()).isEqualTo(ImmutableMap.of("foo", "bar"));
  }
}
