package com.segment.analytics;

import com.segment.analytics.TestUtils.MessageBuilder;
import com.segment.analytics.internal.AnalyticsClient;
import com.segment.analytics.messages.Message;
import com.squareup.burst.BurstJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(BurstJUnit4.class) public class AnalyticsTest {

  @Mock AnalyticsClient client;
  Analytics analytics;

  @Before public void setUp() {
    initMocks(this);
    analytics = new Analytics(client);
  }

  @Test public void enqueueIsDispatched(MessageBuilder builder) {
    Message message = builder.get().userId("prateek").build();

    analytics.enqueue(message);

    verify(client).enqueue(message);
  }

  @Test public void shutdownIsDispatched() {
    analytics.shutdown();

    verify(client).shutdown();
  }
}
