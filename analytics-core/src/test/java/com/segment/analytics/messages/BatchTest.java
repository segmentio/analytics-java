package com.segment.analytics.messages;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.segment.analytics.TestUtils;
import com.squareup.burst.BurstJUnit4;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BurstJUnit4.class)
public class BatchTest {

  private static final String writeKey = "writeKey";

  @Test
  public void create(TestUtils.MessageBuilderFactory factory) {
    Message message = factory.get().userId("userId").build();
    Map<String, String> context = ImmutableMap.of("foo", "bar");
    List<Message> messages = ImmutableList.of(message);

    Batch batch = Batch.create(context, messages, writeKey);

    assertThat(batch.batch()).isEqualTo(messages);
    assertThat(batch.context()).isEqualTo(context);
  }

  @Test
  public void createWithSentAt(TestUtils.MessageBuilderFactory factory) throws ParseException {
    Message message =
        factory
            .get()
            .userId("userId")
            .sentAt(new SimpleDateFormat("yyyy-MM-dd").parse("2022-04-01"))
            .build();
    Map<String, String> context = ImmutableMap.of("foo", "bar");
    List<Message> messages = ImmutableList.of(message);

    Batch batch = Batch.create(context, messages, writeKey);

    assertThat(batch.sentAt()).isEqualTo(messages.get(0).sentAt());
  }

  @Test
  public void createWithSentAtNull(TestUtils.MessageBuilderFactory factory) throws ParseException {
    Message message = factory.get().userId("userId").sentAt(null).build();
    Map<String, String> context = ImmutableMap.of("foo", "bar");
    List<Message> messages = ImmutableList.of(message);

    Batch batch = Batch.create(context, messages, writeKey);

    assertThat(batch.sentAt()).isEqualToIgnoringHours(new Date());
  }

  @Test
  public void sequence(TestUtils.MessageBuilderFactory factory) {
    Message message = factory.get().userId("userId").build();
    Map<String, String> context = ImmutableMap.of("foo", "bar");
    List<Message> messages = ImmutableList.of(message);

    Batch first = Batch.create(context, messages, writeKey);
    Batch second = Batch.create(context, messages, writeKey);

    assertThat(first.sequence() + 1).isEqualTo(second.sequence());
  }
}
