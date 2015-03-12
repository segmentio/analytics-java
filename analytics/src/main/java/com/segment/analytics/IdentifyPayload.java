package com.segment.analytics;

import com.google.auto.value.AutoValue;
import com.segment.analytics.internal.Utils;
import com.segment.analytics.internal.gson.AutoGson;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

@AutoValue @AutoGson //
public abstract class IdentifyPayload implements Payload {

  @Nullable public abstract Map<String, Object> traits();

  public static Builder builder() {
    return new AutoValue_IdentifyPayload.Builder() //
        .type(Type.IDENTIFY) //
        .timestamp(new Date()) //
        .messageId(UUID.randomUUID());
  }

  @AutoValue.Validate void validate() {
    Utils.validate(this);

    if (userId() == null && traits() == null) {
      throw new IllegalArgumentException("Either userId or traits must be provided.");
    }
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

    // Identify Payload
    public abstract Builder traits(Map<String, Object> traits);

    public abstract IdentifyPayload build();
  }
}
