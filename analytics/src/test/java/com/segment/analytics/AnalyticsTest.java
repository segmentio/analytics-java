package com.segment.analytics;

import com.segment.analytics.TestUtils.MessageBuilderTest;
import com.segment.analytics.internal.AnalyticsClient;
import com.segment.analytics.messages.Message;
import com.segment.analytics.messages.MessageBuilder;
import com.squareup.burst.BurstJUnit4;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(BurstJUnit4.class) public class AnalyticsTest {

  @Mock AnalyticsClient client;
  @Mock Log log;
  MessageTransformer messageTransformer;
  Analytics analytics;

  @Before public void setUp() {
    initMocks(this);

    messageTransformer = new MessageTransformer() {
      @Override public boolean transform(MessageBuilder builder) {
        builder.userId("prateek");
        return true;
      }
    };
    analytics = new Analytics(client, Collections.singletonList(messageTransformer),
        Collections.<MessageInterceptor>emptyList(), log);
  }

  @Test public void enqueueIsDispatched(MessageBuilderTest builder) {
    MessageBuilder messageBuilder = builder.get().userId("prateek");

    analytics.enqueue(messageBuilder);

    ArgumentCaptor<Message> messageArgumentCaptor = ArgumentCaptor.forClass(Message.class);
    verify(client).enqueue(messageArgumentCaptor.capture());
    assertThat(messageArgumentCaptor.getValue().userId()).isEqualTo("prateek");
  }

  @Test public void shutdownIsDispatched() {
    analytics.shutdown();

    verify(client).shutdown();
  }

  @Test public void flushIsDispatched() {
    analytics.flush();

    verify(client).flush();
  }
}
