package com.segment.analytics;

import com.google.auto.value.AutoValue;
import com.segment.analytics.internal.Utils;
import com.segment.analytics.internal.gson.AutoGson;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@AutoValue @AutoGson //
public abstract class AliasPayload implements Payload {

  public abstract String previousId();

  public static Builder builder(String previousId) {
    return new AutoValue_AliasPayload.Builder() //
        .type(Type.ALIAS) //
        .timestamp(new Date()) //
        .messageId(UUID.randomUUID()) //
        .previousId(previousId);
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

    // Alias Payload
    public abstract Builder previousId(String previousId);

    public abstract AliasPayload build();
  }
}
