package com.segment.analytics;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.segment.analytics.internal.gson.AutoGson;
import java.util.Map;
import javax.annotation.Nullable;

@AutoValue @AutoGson //
public abstract class IdentifyPayload implements Payload {

  public static Builder builder() {
    return new Builder();
  }

  @Nullable public abstract Map<String, Object> traits();

  public static class Builder extends PayloadBuilder<IdentifyPayload, Builder> {
    Map<String, Object> traits;

    protected Builder() {
      super(Type.IDENTIFY);
    }

    public Builder traits(Map<String, Object> traits) {
      if (traits == null) {
        throw new NullPointerException("Null traits");
      }
      this.traits = ImmutableMap.copyOf(traits);
      return this;
    }

    @Override IdentifyPayload realBuild() {
      if (userId == null && traits == null) {
        throw new IllegalStateException("Either userId or traits must be provided.");
      }

      return new AutoValue_IdentifyPayload(type, messageId, timestamp, context, anonymousId, userId,
          traits);
    }

    @Override Builder self() {
      return this;
    }
  }
}
