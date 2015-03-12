package com.segment.analytics;

import com.google.auto.value.AutoValue;
import com.segment.analytics.internal.Utils;
import com.segment.analytics.internal.gson.AutoGson;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

@AutoValue @AutoGson //
public abstract class ScreenPayload implements Payload {
  @Nullable public abstract String name();

  @Nullable public abstract String category();

  @Nullable public abstract Map<String, Object> properties();

  public static Builder builder() {
    return new AutoValue_ScreenPayload.Builder() //
        .type(Type.SCREEN) //
        .timestamp(new Date()) //
        .messageId(UUID.randomUUID());
  }

  @AutoValue.Validate void validate() {
    Utils.validate(this);

    if (name() == null && category() == null) {
      throw new IllegalArgumentException("either name or category must be provided");
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

    // Screen Payload
    public abstract Builder name(String name);

    public abstract Builder category(String category);

    public abstract Builder properties(Map<String, Object> properties);

    public abstract ScreenPayload build();
  }
}
