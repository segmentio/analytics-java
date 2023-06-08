package com.segment.analytics;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

import com.segment.analytics.messages.AliasMessage;
import com.segment.analytics.messages.GroupMessage;
import com.segment.analytics.messages.IdentifyMessage;
import com.segment.analytics.messages.PageMessage;
import com.segment.analytics.messages.ScreenMessage;
import com.segment.analytics.messages.TrackMessage;
import org.junit.Test;

public class TypedInterceptorTest {
  @Test
  public void messagesFanOutCorrectly() {
    MessageInterceptor.Typed interceptor = mock(MessageInterceptor.Typed.class);

    AliasMessage alias = AliasMessage.builder("foo").userId("bar").build();
    interceptor.intercept(alias);
    assertNull(interceptor.alias(alias));

    GroupMessage group = GroupMessage.builder("foo").userId("bar").build();
    interceptor.intercept(group);
    assertNull(interceptor.group(group));

    IdentifyMessage identify = IdentifyMessage.builder().userId("bar").build();
    interceptor.intercept(identify);
    assertNull(interceptor.identify(identify));

    ScreenMessage screen = ScreenMessage.builder("foo").userId("bar").build();
    interceptor.intercept(screen);
    assertNull(interceptor.screen(screen));

    PageMessage page = PageMessage.builder("foo").userId("bar").build();
    interceptor.intercept(page);
    assertNull(interceptor.page(page));

    TrackMessage track = TrackMessage.builder("foo").userId("bar").build();
    interceptor.intercept(track);
    assertNull(interceptor.track(track));
  }
}
