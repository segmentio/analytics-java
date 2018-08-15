package com.segment.analytics.messages;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.segment.analytics.TestUtils;
import com.squareup.burst.BurstJUnit4;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BurstJUnit4.class)
public class BatchTest {

  @Test
  public void create(TestUtils.MessageBuilderFactory factory) {
    Message message = factory.get().userId("userId").build();
    Map<String, String> context = ImmutableMap.of("foo", "bar");
    List<Message> messages = ImmutableList.of(message);

    Batch batch = Batch.create(context, messages);

    assertThat(batch.batch()).isEqualTo(messages);
    assertThat(batch.context()).isEqualTo(context);
  }

  @Test
  public void sequence(TestUtils.MessageBuilderFactory factory) {
    Message message = factory.get().userId("userId").build();
    Map<String, String> context = ImmutableMap.of("foo", "bar");
    List<Message> messages = ImmutableList.of(message);

    Batch first = Batch.create(context, messages);
    Batch second = Batch.create(context, messages);

    assertThat(first.sequence() + 1).isEqualTo(second.sequence());
  }
}
