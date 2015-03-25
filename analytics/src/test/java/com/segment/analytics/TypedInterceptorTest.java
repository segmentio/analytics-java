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
      @Override AliasMessage alias(AliasMessage alias) {
        return mockInterceptor.alias(alias);
      }

      @Override GroupMessage group(GroupMessage group) {
        return mockInterceptor.group(group);
      }

      @Override IdentifyMessage identify(IdentifyMessage identify) {
        return mockInterceptor.identify(identify);
      }

      @Override ScreenMessage screen(ScreenMessage screen) {
        return mockInterceptor.screen(screen);
      }

      @Override TrackMessage track(TrackMessage track) {
        return mockInterceptor.track(track);
      }
    };

    AliasMessage alias = AliasMessage.builder("foo").userId("bar").build();
    interceptor.intercept(alias);
    verify(mockInterceptor).alias(alias);

    GroupMessage group = GroupMessage.builder("foo").userId("bar").build();
    interceptor.intercept(group);
    verify(mockInterceptor).group(group);

    IdentifyMessage identify = IdentifyMessage.builder().userId("bar").build();
    interceptor.intercept(identify);
    verify(mockInterceptor).identify(identify);

    ScreenMessage screen = ScreenMessage.builder().name("foo").userId("bar").build();
    interceptor.intercept(screen);
    verify(mockInterceptor).screen(screen);

    TrackMessage track = TrackMessage.builder("foo").userId("bar").build();
    interceptor.intercept(track);
    verify(mockInterceptor).track(track);
  }
}
