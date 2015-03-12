package com.segment.analytics;

import com.google.auto.value.AutoValue;
import com.segment.analytics.internal.Utils;
import com.segment.analytics.internal.gson.AutoGson;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

@AutoValue @AutoGson //
public abstract class TrackPayload implements Payload {
  public abstract String event();

  @Nullable public abstract Map<String, Object> properties();

  public static Builder builder(String event) {
    return new AutoValue_TrackPayload.Builder() //
        .type(Type.TRACK) //
        .timestamp(new Date()) //
        .messageId(UUID.randomUUID()) //
        .event(event);
  }

  @AutoValue.Validate void validate() {
    Utils.validate(this);
  }

  @AutoValue.Builder //
  public abstract static class Builder {
    // Common
    abstract Builder type(Type type); // Required

    abstract Builder messageId(UUID messageId); // Required

    abstract Builder timestamp(Date timestamp); // Required

    public abstract Builder context(Map<String, Object> context);

    public abstract Builder anonymousId(UUID anonymousId);

    public abstract Builder userId(String userId);

    // Track Payload

    abstract Builder event(String event); // Required

    public abstract Builder properties(Map<String, Object> properties);

    public abstract TrackPayload build();
  }
}
