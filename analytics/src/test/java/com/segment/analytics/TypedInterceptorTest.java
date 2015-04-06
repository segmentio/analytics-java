package com.segment.analytics;

import com.segment.analytics.messages.AliasMessage;
import com.segment.analytics.messages.GroupMessage;
import com.segment.analytics.messages.IdentifyMessage;
import com.segment.analytics.messages.ScreenMessage;
import com.segment.analytics.messages.TrackMessage;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TypedInterceptorTest {
  @Test public void messagesFanOutCorrectly() {
    final TypedInterceptor mockInterceptor = mock(TypedInterceptor.class);
    TypedInterceptor interceptor = new TypedInterceptor() {
      @Override boolean alias(AliasMessage.Builder alias) {
        return mockInterceptor.alias(alias);
      }

      @Override boolean group(GroupMessage.Builder group) {
        return mockInterceptor.group(group);
      }

      @Override boolean identify(IdentifyMessage.Builder identify) {
        return mockInterceptor.identify(identify);
      }

      @Override boolean screen(ScreenMessage.Builder screen) {
        return mockInterceptor.screen(screen);
      }

      @Override boolean track(TrackMessage.Builder track) {
        return mockInterceptor.track(track);
      }
    };

    AliasMessage.Builder alias = AliasMessage.builder("foo").userId("bar");
    interceptor.intercept(alias);
    verify(mockInterceptor).alias(alias);

    GroupMessage.Builder group = GroupMessage.builder("foo").userId("bar");
    interceptor.intercept(group);
    verify(mockInterceptor).group(group);

    IdentifyMessage.Builder identify = IdentifyMessage.builder().userId("bar");
    interceptor.intercept(identify);
    verify(mockInterceptor).identify(identify);

    ScreenMessage.Builder screen = ScreenMessage.builder("foo").userId("bar");
    interceptor.intercept(screen);
    verify(mockInterceptor).screen(screen);

    TrackMessage.Builder track = TrackMessage.builder("foo").userId("bar");
    interceptor.intercept(track);
    verify(mockInterceptor).track(track);
  }
}
