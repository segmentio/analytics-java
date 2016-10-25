package com.segment.analytics.messages;

import com.google.common.collect.ImmutableMap;
import com.segment.analytics.TestUtils;
import com.squareup.burst.BurstJUnit4;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@RunWith(BurstJUnit4.class)
public class MessageBuilderTest {

  @Test
  public void nullMessageIdThrowsException(TestUtils.MessageBuilderTest builder) {
    try {
      builder.get().messageId(null);
      fail();
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("Null messageId");
    }
  }

  @Test
  public void defaultMessageIdIsGenerated(TestUtils.MessageBuilderTest builder) {
    Message message = builder.get().userId("foo").build();
    assertThat(message.messageId()).isNotNull();
  }

  @Test
  public void messageIdCanBeProvided(TestUtils.MessageBuilderTest builder) {
    UUID uuid = UUID.randomUUID();
    Message message = builder.get().userId("foo").messageId(uuid).build();
    assertThat(message.messageId()).isNotNull();
  }

  @Test
  public void defaultAnonymousIdIsNotGenerated(TestUtils.MessageBuilderTest builder) {
    Message message = builder.get().userId("foo").build();
    assertThat(message.anonymousId()).isNull();
  }

  @Test
  public void anonymousIdCanBeProvided(TestUtils.MessageBuilderTest builder) {
    UUID uuid = UUID.randomUUID();
    // Must also provide a userId because identify requires `userId` or `traits`.
    Message message = builder.get().anonymousId(uuid).userId("foo").build();
    assertThat(message.anonymousId()).isEqualTo(uuid);
  }

  @Test
  public void missingUserIdAndAnonymousIdThrowsException(TestUtils.MessageBuilderTest builder) {
    try {
      builder.get().build();
      fail();
    } catch (IllegalStateException e) {
      assertThat(e).hasMessage("Either anonymousId or userId must be provided.");
    }
  }

  @Test
  public void nullTimestampThrowsError(TestUtils.MessageBuilderTest builder) {
    try {
      builder.get().timestamp(null);
      fail();
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("Null timestamp");
    }
  }

  @Test public void providingUserIdBuildsSuccessfully(TestUtils.MessageBuilderTest builder) {
    Message message = builder.get().userId("foo").build();
    assertThat(message.userId()).isEqualToIgnoringCase("foo");
  }

  @Test public void aliasBuilder() {
    try {
      AliasMessage.builder(null);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessage("previousId cannot be null or empty.");
    }
  }

  @Test public void groupBuilder() {
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
      ScreenMessage.builder("foo").properties(null).build();
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("Null properties");
    }

    try {
      ScreenMessage.builder(null).userId("foo").build();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessage("screen name cannot be null or empty.");
    }
  }

  @Test public void pageBuilder() {
    try {
      PageMessage.builder("foo").properties(null).build();
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("Null properties");
    }

    try {
      PageMessage.builder(null).userId("foo").build();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessage("page name cannot be null or empty.");
    }
  }

  @Test public void trackBuilder() {
    try {
      TrackMessage.builder(null);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessage("event cannot be null or empty.");
    }

    try {
      TrackMessage.builder("foo").properties(null);
      fail();
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("Null properties");
    }
  }

  @Test public void defaultIntegrationsIsGenerated(TestUtils.MessageBuilderTest builder) {
    Message message = builder.get()
        .userId("foo")
        .build();

    assertThat(message.integrations()).isEmpty();
  }

  @Test public void integrations(TestUtils.MessageBuilderTest builder) {
    Message message = builder.get()
        .userId("foo")
        .enableIntegration("foo", false)
        .integrationOptions("bar", ImmutableMap.of("qaz", "qux"))
        .build();

    assertThat(message.integrations()).hasSize(2)
        .containsEntry("foo", false)
        .containsEntry("bar", ImmutableMap.of("qaz", "qux"));
  }
}
