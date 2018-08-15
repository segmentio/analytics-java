package com.segment.analytics.messages;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

public class ScreenMessageTest {

  @Test
  public void invalidPropertiesThrows() {
    try {
      ScreenMessage.builder("foo").properties(null);
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("Null properties");
    }
  }

  @Test
  public void invalidNameThrows() {
    try {
      ScreenMessage.builder(null);
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessage("screen name cannot be null or empty.");
    }
  }

  @Test
  public void toBuilder() {
    ScreenMessage original =
        ScreenMessage.builder("name")
            .properties(ImmutableMap.of("foo", "bar"))
            .userId("userId")
            .build();
    ScreenMessage copy = original.toBuilder().build();

    assertThat(copy.name()).isEqualTo("name");
    assertThat(copy.properties()).isEqualTo(ImmutableMap.of("foo", "bar"));
  }
}
