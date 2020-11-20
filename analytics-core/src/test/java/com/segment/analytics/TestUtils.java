package com.segment.analytics;

import com.segment.analytics.messages.*;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public final class TestUtils {
  private TestUtils() {
    throw new AssertionError("No instances.");
  }

  @SuppressWarnings("UnusedDeclaration")
  public enum MessageBuilderFactory {
    ALIAS {
      @Override
      public AliasMessage.Builder get() {
        return AliasMessage.builder("foo");
      }
    },
    GROUP {
      @Override
      public GroupMessage.Builder get() {
        return GroupMessage.builder("foo");
      }
    },
    IDENTIFY {
      @Override
      public IdentifyMessage.Builder get() {
        return IdentifyMessage.builder();
      }
    },
    SCREEN {
      @Override
      public ScreenMessage.Builder get() {
        return ScreenMessage.builder("foo");
      }
    },
    PAGE {
      @Override
      public PageMessage.Builder get() {
        return PageMessage.builder("foo");
      }
    },
    TRACK {
      @Override
      public TrackMessage.Builder get() {
        return TrackMessage.builder("foo");
      }
    };

    public abstract <T extends Message, V extends MessageBuilder> MessageBuilder<T, V> get();
  }

  public static Date newDate(
      int year, int month, int day, int hour, int minute, int second, int millis, int offset) {
    Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    calendar.set(year, month - 1, day, hour, minute, second);
    calendar.set(Calendar.MILLISECOND, millis);
    return new Date(calendar.getTimeInMillis() - TimeUnit.MINUTES.toMillis(offset));
  }
}
