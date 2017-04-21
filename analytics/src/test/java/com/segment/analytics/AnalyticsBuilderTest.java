package com.segment.analytics;

import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class AnalyticsBuilderTest {
  Analytics.Builder builder;

  @Before public void setUp() {
    builder = Analytics.builder("foo");
  }

  @Test public void nullWriteKey() {
    try {
      builder = Analytics.builder(null);
      fail("Should fail for null writeKey");
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("writeKey cannot be null or empty.");
    }
  }

  @Test public void emptyWriteKey() {
    try {
      builder = Analytics.builder("");
      fail("Should fail for empty writeKey");
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("writeKey cannot be null or empty.");
    }

    try {
      builder = Analytics.builder("  ");
      fail("Should fail for empty writeKey");
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("writeKey cannot be null or empty.");
    }
  }

  @Test public void nullClient() {
    try {
      builder.client(null);
      fail("Should fail for null client");
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("Null client");
    }
  }

  @Test public void nullLog() {
    try {
      builder.log(null);
      fail("Should fail for null log");
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("Null log");
    }
  }

  @Test public void nullTransformer() {
    try {
      builder.messageTransformer(null);
      fail("Should fail for null transformer");
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("Null transformer");
    }
  }

  @Test public void duplicateTransformer() {
    MessageTransformer transformer = mock(MessageTransformer.class);
    try {
      builder.messageTransformer(transformer).messageTransformer(transformer);
      fail("Should fail for duplicate transformer");
    } catch (IllegalStateException e) {
      assertThat(e).hasMessage("MessageTransformer is already registered.");
    }
  }

  @Test public void nullInterceptor() {
    try {
      builder.messageInterceptor(null);
      fail("Should fail for null interceptor");
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("Null interceptor");
    }
  }

  @Test public void duplicateInterceptor() {
    MessageInterceptor interceptor = mock(MessageInterceptor.class);
    try {
      builder.messageInterceptor(interceptor).messageInterceptor(interceptor);
      fail("Should fail for duplicate interceptor");
    } catch (IllegalStateException e) {
      assertThat(e).hasMessage("MessageInterceptor is already registered.");
    }
  }

  @Test public void invalidFlushQueueSize() {
    try {
      builder.flushQueueSize(0);
      fail("Should fail for non positive flushQueueSize");
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessage("flushQueueSize must not be less than 1.");
    }

    try {
      builder.flushQueueSize(-1);
      fail("Should fail for non positive flushQueueSize");
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessage("flushQueueSize must not be less than 1.");
    }
  }

  @Test public void invalidFlushInterval() {
    try {
      builder.flushInterval(-1, TimeUnit.SECONDS);
      fail("Should fail for negative flushInterval");
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessage("flushInterval must not be less than 1 second.");
    }

    try {
      builder.flushInterval(500, TimeUnit.MILLISECONDS);
      fail("Should fail for flushInterval < 1 second");
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessage("flushInterval must not be less than 1 second.");
    }

    // Exercise a bug where we only checked the number passed without converting to milliseconds
    try {
      builder.flushInterval(2000, TimeUnit.NANOSECONDS);
      fail("Should fail for flushInterval < 1 second");
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessage("flushInterval must not be less than 1 second.");
    }
  }

  @Test public void nullNetworkExecutor() {
    try {
      builder.networkExecutor(null);
      fail("Should fail for null network executor");
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("Null networkExecutor");
    }
  }

//  @Test public void nullEndpoint() {
//    try {
//      builder.endpoint(null);
//      fail("Should fail for null endpoint");
//    } catch (NullPointerException e) {
//      assertThat(e).hasMessage("endpoint cannot be null or empty.");
//    }
//  }
//
//  @Test public void emptyEndpoint() {
//    try {
//      builder.endpoint("");
//      fail("Should fail for empty endpoint");
//    } catch (NullPointerException e) {
//      assertThat(e).hasMessage("endpoint cannot be null or empty.");
//    }
//
//    try {
//      builder.endpoint("  ");
//      fail("Should fail for empty endpoint");
//    } catch (NullPointerException e) {
//      assertThat(e).hasMessage("endpoint cannot be null or empty.");
//    }
//  }

  @Test public void nullThreadFactory() {
    try {
      builder.threadFactory(null);
      fail("Should fail for null thread factory");
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("Null threadFactory");
    }
  }

  @Test public void nullCallback() {
    try {
      builder.callback(null);
      fail("Should fail for null callback");
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("Null callback");
    }
  }

  @Test public void nullPlugin() {
    try {
      builder.plugin(null);
      fail("Should fail for null plugin");
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("Null plugin");
    }
  }

  @Test public void pluginCanConfigure() {
    Plugin plugin = Mockito.mock(Plugin.class);
    builder.plugin(plugin);
    verify(plugin).configure(builder);
  }
}
