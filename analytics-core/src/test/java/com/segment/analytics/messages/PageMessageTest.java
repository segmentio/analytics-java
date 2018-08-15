package com.segment.analytics.messages;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

public class PageMessageTest {

  @Test
  public void invalidPropertiesThrows() {
    try {
      PageMessage.builder("foo").properties(null);
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("Null properties");
    }
  }

  @Test
  public void invalidNameThrows() {
    try {
      PageMessage.builder(null);
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessage("page name cannot be null or empty.");
    }
  }

  @Test
  public void toBuilder() {
    PageMessage original =
        PageMessage.builder("name")
            .properties(ImmutableMap.of("foo", "bar"))
            .userId("userId")
            .build();
    PageMessage copy = original.toBuilder().build();

    assertThat(copy.name()).isEqualTo("name");
    assertThat(copy.properties()).isEqualTo(ImmutableMap.of("foo", "bar"));
  }
}
