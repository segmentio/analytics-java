package com.segment.analytics.messages;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

public class TrackMessageTest {

  @Test
  public void invalidPropertiesThrows() {
    try {
      TrackMessage.builder("foo").properties(null);
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("Null properties");
    }
  }

  @Test
  public void invalidNameThrows() {
    try {
      TrackMessage.builder(null);
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessage("event cannot be null or empty.");
    }
  }

  @Test
  public void toBuilder() {
    TrackMessage original =
        TrackMessage.builder("event")
            .properties(ImmutableMap.of("foo", "bar"))
            .userId("userId")
            .build();
    TrackMessage copy = original.toBuilder().build();

    assertThat(copy.event()).isEqualTo("event");
    assertThat(copy.properties()).isEqualTo(ImmutableMap.of("foo", "bar"));
  }

  @Test
  public void userIdOrAnonymousIdIsRequired() {
    final String exceptionMessage = "Either anonymousId or userId must be provided.";

    try {
      TrackMessage.builder("event").build();
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessage(exceptionMessage);
    }

    try {
      TrackMessage.builder("event").userId(null).anonymousId((String) null).build();
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessage(exceptionMessage);
    }

    try {
      TrackMessage.builder("event").userId("").anonymousId("").build();
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessage(exceptionMessage);
    }

    TrackMessage message = TrackMessage.builder("event").userId("theUserId").build();
    assertThat(message.userId()).isEqualTo("theUserId");
    assertThat(message.event()).isEqualTo("event");

    message = TrackMessage.builder("event").anonymousId("theAnonymousId").build();
    assertThat(message.anonymousId()).isEqualTo("theAnonymousId");
    assertThat(message.event()).isEqualTo("event");
  }
}
