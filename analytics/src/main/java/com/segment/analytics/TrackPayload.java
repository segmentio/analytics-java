package com.segment.analytics;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.segment.analytics.internal.gson.AutoGson;
import java.util.Map;
import javax.annotation.Nullable;

@AutoValue @AutoGson //
public abstract class TrackPayload implements Payload {
  public abstract String event();

  @Nullable public abstract Map<String, Object> properties();

  public static Builder builder(String event) {
    return new Builder(event);
  }

  public static class Builder extends PayloadBuilder<TrackPayload> {
    String event;
    Map<String, Object> properties;

    Builder(String event) {
      super(Type.SCREEN);

      if (event == null) {
        throw new NullPointerException("Null event");
      }
      this.event = event;
    }

    public Builder properties(Map<String, Object> properties) {
      if (properties == null) {
        throw new NullPointerException("Null properties");
      }
      this.properties = ImmutableMap.copyOf(properties);
      return this;
    }

    @Override TrackPayload realBuild() {
      return new AutoValue_TrackPayload(type, messageId, timestamp, context, anonymousId, userId,
          event, properties);
    }
  }
}
