package com.segment.analytics;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

import com.segment.analytics.messages.AliasMessage;
import com.segment.analytics.messages.GroupMessage;
import com.segment.analytics.messages.IdentifyMessage;
import com.segment.analytics.messages.PageMessage;
import com.segment.analytics.messages.ScreenMessage;
import com.segment.analytics.messages.TrackMessage;
import org.junit.Test;

public class TypedTransformerTest {
  @Test
  public void messagesFanOutCorrectly() {
    MessageTransformer.Typed transformer = mock(MessageTransformer.Typed.class);

    AliasMessage.Builder alias = AliasMessage.builder("foo").userId("bar");
    transformer.transform(alias);
    assertFalse(transformer.alias(alias));

    GroupMessage.Builder group = GroupMessage.builder("foo").userId("bar");
    transformer.transform(group);
    assertFalse(transformer.group(group));

    IdentifyMessage.Builder identify = IdentifyMessage.builder().userId("bar");
    transformer.transform(identify);
    assertFalse(transformer.identify(identify));

    ScreenMessage.Builder screen = ScreenMessage.builder("foo").userId("bar");
    transformer.transform(screen);
    assertFalse(transformer.screen(screen));

    PageMessage.Builder page = PageMessage.builder("foo").userId("bar");
    transformer.transform(page);
    assertFalse(transformer.page(page));

    TrackMessage.Builder track = TrackMessage.builder("foo").userId("bar");
    transformer.transform(track);
    assertFalse(transformer.track(track));
  }
}
