package com.segment.analytics.messages;

import static com.segment.analytics.TestUtils.newDate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableMap;
import com.segment.analytics.TestUtils;
import com.squareup.burst.BurstJUnit4;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BurstJUnit4.class)
public class MessageBuilderTest {

  @Test
  public void nullStringMessageIdThrowsException(TestUtils.MessageBuilderFactory builder) {
    try {
      builder.get().messageId((String) null);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessage("messageId cannot be null or empty.");
    }
  }

  @Test
  public void type(TestUtils.MessageBuilderFactory builder) {
    assertThat(builder.get().type()).isNotNull();
  }

  @Test
  public void emptyStringMessageIdThrowsException(TestUtils.MessageBuilderFactory builder) {
    try {
      builder.get().messageId("");
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessage("messageId cannot be null or empty.");
    }
  }

  @Test
  public void nullUUIDMessageIdThrowsException(TestUtils.MessageBuilderFactory builder) {
    try {
      builder.get().messageId((UUID) null);
      fail();
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("Null messageId");
    }
  }

  @Test
  public void defaultMessageIdIsGenerated(TestUtils.MessageBuilderFactory builder) {
    Message message = builder.get().userId("foo").build();
    assertThat(message.messageId()).isNotNull();
  }

  @Test
  public void uuidMessageId(TestUtils.MessageBuilderFactory builder) {
    UUID uuid = UUID.randomUUID();
    Message message = builder.get().userId("foo").messageId(uuid).build();
    assertThat(message.messageId()).isEqualTo(uuid.toString());
  }

  @Test
  public void stringMessageId(TestUtils.MessageBuilderFactory builder) {
    Message message = builder.get().userId("foo").messageId("messageId").build();
    assertThat(message.messageId()).isEqualTo("messageId");
  }

  @Test
  public void defaultAnonymousIdIsNotGenerated(TestUtils.MessageBuilderFactory builder) {
    Message message = builder.get().userId("foo").build();
    assertThat(message.anonymousId()).isNull();
  }

  @Test
  public void uuidAnonymousId(TestUtils.MessageBuilderFactory builder) {
    UUID uuid = UUID.randomUUID();
    Message message = builder.get().anonymousId(uuid).userId("foo").build();
    assertThat(message.anonymousId()).isEqualTo(uuid.toString());
  }

  @Test
  public void stringAnonymousId(TestUtils.MessageBuilderFactory builder) {
    Message message = builder.get().anonymousId("anonymousId").userId("foo").build();
    assertThat(message.anonymousId()).isEqualTo("anonymousId");
  }

  @Test
  public void nullUUIDAnonymousIdThrowsException(TestUtils.MessageBuilderFactory builder) {
    try {
      builder.get().anonymousId((UUID) null);
      fail();
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("Null anonymousId");
    }
  }

  @Test
  public void missingUserIdAndAnonymousIdThrowsException(TestUtils.MessageBuilderFactory builder) {
    try {
      builder.get().build();
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessage("Either anonymousId or userId must be provided.");
    }
  }

  @Test
  public void nullTimestampThrowsError(TestUtils.MessageBuilderFactory builder) {
    try {
      builder.get().timestamp(null);
      fail();
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("Null timestamp");
    }
  }

  @Test
  public void timestamp(TestUtils.MessageBuilderFactory builder) {
    Date date = newDate(1985, 4, 12, 23, 20, 50, 520, 0);
    Message message = builder.get().userId("userId").timestamp(date).build();
    assertThat(message.timestamp()).isEqualTo(date);
  }

  @Test
  public void providingUserIdBuildsSuccessfully(TestUtils.MessageBuilderFactory builder) {
    Message message = builder.get().userId("foo").build();
    assertThat(message.userId()).isEqualToIgnoringCase("foo");
  }

  @Test
  public void defaultIntegrationsIsGenerated(TestUtils.MessageBuilderFactory builder) {
    Message message = builder.get().userId("foo").build();

    assertThat(message.integrations()).isEmpty();
  }

  @Test
  public void invalidIntegrationKeyThrowsWhenSettingOptions(
      TestUtils.MessageBuilderFactory builder) {
    try {
      builder.get().integrationOptions(null, null);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessage("Key cannot be null or empty.");
    }
  }

  @Test
  public void invalidIntegrationKeyThrowsWhenEnabling(TestUtils.MessageBuilderFactory builder) {
    try {
      builder.get().enableIntegration(null, false);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessage("Key cannot be null or empty.");
    }
  }

  @Test
  public void enableIntegration(TestUtils.MessageBuilderFactory builder) {
    Message message = builder.get().userId("foo").enableIntegration("foo", false).build();

    assertThat(message.integrations()).hasSize(1).containsEntry("foo", false);
  }

  @Test
  public void integrationOptions(TestUtils.MessageBuilderFactory builder) {
    Message message =
        builder
            .get()
            .userId("foo")
            .integrationOptions("bar", ImmutableMap.of("qaz", "qux"))
            .build();

    assertThat(message.integrations())
        .hasSize(1)
        .containsEntry("bar", ImmutableMap.of("qaz", "qux"));
  }

  @Test
  public void integrations(TestUtils.MessageBuilderFactory builder) {
    Message message =
        builder
            .get()
            .userId("foo")
            .enableIntegration("foo", false)
            .integrationOptions("bar", ImmutableMap.of("qaz", "qux"))
            .build();

    assertThat(message.integrations())
        .hasSize(2)
        .containsEntry("foo", false)
        .containsEntry("bar", ImmutableMap.of("qaz", "qux"));
  }

  @Test
  public void invalidContextThrows(TestUtils.MessageBuilderFactory builder) {
    try {
      builder.get().context(null);
      fail();
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("Null context");
    }
  }

  @Test
  public void context(TestUtils.MessageBuilderFactory builder) {
    Map<String, String> context = ImmutableMap.of("foo", "bar");
    Message message = builder.get().userId("foo").context(context).build();
    assertThat(message.context()).isEqualTo(context);
  }
}
