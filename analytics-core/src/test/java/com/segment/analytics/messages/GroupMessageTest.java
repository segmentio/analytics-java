package com.segment.analytics.messages;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

public class GroupMessageTest {

  @Test
  public void invalidGroupIdThrows() {
    try {
      GroupMessage.builder(null);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessage("groupId cannot be null or empty.");
    }

    try {
      GroupMessage.builder("foo").traits(null);
      fail();
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("Null traits");
    }
  }

  @Test
  public void invalidTraitsThrows() {
    try {
      GroupMessage.builder("foo").traits(null);
      fail();
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("Null traits");
    }
  }

  @Test
  public void toBuilder() {
    GroupMessage original =
        GroupMessage.builder("groupId")
            .userId("userId")
            .traits(ImmutableMap.of("foo", "bar"))
            .build();
    GroupMessage copy = original.toBuilder().build();

    assertThat(copy.groupId()).isEqualTo("groupId");
    assertThat(copy.traits()).isEqualTo(ImmutableMap.of("foo", "bar"));
  }
}
