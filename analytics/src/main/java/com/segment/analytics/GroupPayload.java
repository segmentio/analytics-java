package com.segment.analytics;

import com.google.auto.value.AutoValue;
import com.segment.analytics.internal.Utils;
import com.segment.analytics.internal.gson.AutoGson;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

@AutoValue @AutoGson //
public abstract class GroupPayload implements Payload {

  public abstract String groupId();

  @Nullable public abstract Map<String, Object> traits();

  public static Builder builder(String groupId) {
    return new AutoValue_GroupPayload.Builder() //
        .type(Type.GROUP) //
        .timestamp(new Date()) //
        .messageId(UUID.randomUUID()).groupId(groupId);
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

    // Group Payload
    public abstract Builder groupId(String groupId);

    public abstract Builder traits(Map<String, Object> traits);

    public abstract GroupPayload build();
  }
}
