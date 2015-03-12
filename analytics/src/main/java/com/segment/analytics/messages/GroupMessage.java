package com.segment.analytics.messages;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.segment.analytics.internal.gson.AutoGson;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

@AutoValue @AutoGson public abstract class GroupMessage implements Message {

  public static Builder builder(String groupId) {
    return new Builder(groupId);
  }

  public abstract String groupId();

  @Nullable public abstract Map<String, Object> traits();

  public static class Builder extends PayloadBuilder<GroupMessage, Builder> {
    private String groupId;
    private Map<String, Object> traits;

    private Builder(String groupId) {
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

    @Override protected GroupMessage realBuild() {
      return new AutoValue_GroupMessage(Type.GROUP, UUID.randomUUID(), new Date(), context,
          anonymousId, userId, groupId, traits);
    }

    @Override Builder self() {
      return this;
    }
  }
}
