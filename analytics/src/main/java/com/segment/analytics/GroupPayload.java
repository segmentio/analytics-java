package com.segment.analytics;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.segment.analytics.internal.gson.AutoGson;
import java.util.Map;
import javax.annotation.Nullable;

@AutoValue @AutoGson //
public abstract class GroupPayload implements Payload {

  public abstract String groupId();

  @Nullable public abstract Map<String, Object> traits();

  public static Builder builder(String groupId) {
    return new Builder(groupId);
  }

  public static class Builder extends PayloadBuilder<GroupPayload> {
    String groupId;
    Map<String, Object> traits;

    Builder(String groupId) {
      super(Type.GROUP);

      if (groupId == null) {
        // todo validate length?
        throw new NullPointerException("Null groupId");
      }
      this.groupId = groupId;
    }

    public Builder traits(Map<String, Object> traits) {
      if (traits == null) {
        throw new NullPointerException("Null traits");
      }
      this.traits = ImmutableMap.copyOf(traits);
      return this;
    }

    @Override GroupPayload realBuild() {
      return new AutoValue_GroupPayload(type, messageId, timestamp, context, anonymousId, userId,
          groupId, traits);
    }
  }
}
