package com.segment.analytics.messages;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.segment.analytics.internal.gson.AutoGson;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

import static com.segment.analytics.internal.Utils.isNullOrEmpty;

@AutoValue @AutoGson public abstract class TrackMessage implements Message {
  public static Builder builder(String event) {
    return new Builder(event);
  }

  public abstract String event();

  @Nullable public abstract Map<String, Object> properties();

  public static class Builder extends PayloadBuilder<TrackMessage, Builder> {
    private String event;
    private Map<String, Object> properties;

    private Builder(String event) {
      if (isNullOrEmpty(event)) {
        throw new NullPointerException("event cannot be null or empty.");
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

    @Override Builder self() {
      return this;
    }

    @Override protected TrackMessage realBuild() {
      return new AutoValue_TrackMessage(Type.TRACK, UUID.randomUUID(), new Date(), context,
          anonymousId, userId, event, properties);
    }
  }
}
