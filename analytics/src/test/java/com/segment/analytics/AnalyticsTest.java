package com.segment.analytics;

import com.segment.analytics.TestUtils.MessageBuilder;
import com.segment.analytics.internal.AnalyticsClient;
import com.segment.analytics.messages.Message;
import com.squareup.burst.BurstJUnit4;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(BurstJUnit4.class) public class AnalyticsTest {

  @Mock AnalyticsClient client;
  @Mock MessageInterceptor interceptor;
  @Mock Log log;
  Analytics analytics;

  @Before public void setUp() {
    initMocks(this);
    when(interceptor.intercept(any(Message.class))).thenAnswer(new Answer<Message>() {
      @Override public Message answer(InvocationOnMock invocation) throws Throwable {
        return (Message) invocation.getArguments()[0];
      }
    });
    analytics = new Analytics(client, Collections.singletonList(interceptor), log);
  }

  @Test public void enqueueIsDispatched(MessageBuilder builder) {
    Message message = builder.get().userId("prateek").build();

    analytics.enqueue(message);

    verify(interceptor).intercept(message);
    verify(client).enqueue(message);
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
