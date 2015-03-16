package com.segment.analytics;

import com.segment.analytics.internal.AnalyticsClient;
import com.segment.analytics.messages.AliasMessage;
import com.segment.analytics.messages.GroupMessage;
import com.segment.analytics.messages.IdentifyMessage;
import com.segment.analytics.messages.Message;
import com.segment.analytics.messages.PayloadBuilder;
import com.segment.analytics.messages.ScreenMessage;
import com.segment.analytics.messages.TrackMessage;
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

  // TODO: Re-use from MessageTest?
  @SuppressWarnings("UnusedDeclaration") public enum MessageBuilder {

    ALIAS {
      @Override public AliasMessage.Builder get() {
        return AliasMessage.builder("foo");
      }
    }, GROUP {
      @Override public GroupMessage.Builder get() {
        return GroupMessage.builder("foo");
      }
    },
    IDENTIFY {
      @Override public IdentifyMessage.Builder get() {
        return IdentifyMessage.builder();
      }
    }, SCREEN {
      @Override public ScreenMessage.Builder get() {
        return ScreenMessage.builder().name("foo");
      }
    }, TRACK {
      @Override public TrackMessage.Builder get() {
        return TrackMessage.builder("foo");
      }
    };

    public abstract <T extends Message, V extends PayloadBuilder> PayloadBuilder<T, V> get();
  }
}
