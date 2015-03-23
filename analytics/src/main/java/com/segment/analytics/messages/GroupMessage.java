package com.segment.analytics.messages;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.segment.analytics.internal.gson.AutoGson;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

import static com.segment.analytics.internal.Utils.isNullOrEmpty;

@AutoValue @AutoGson public abstract class GroupMessage implements Message {

  public static Builder builder(String groupId) {
    return new Builder(groupId);
  }

  public abstract String groupId();

  @Nullable public abstract Map<String, Object> traits();

  public Builder toBuilder() {
    return new Builder(this);
  }

  public static class Builder extends MessageBuilder<GroupMessage, Builder> {
    private String groupId;
    private Map<String, Object> traits;

    private Builder(GroupMessage group) {
      super(group);
      groupId = group.groupId();
      traits = group.traits();
    }

    private Builder(String groupId) {
      if (isNullOrEmpty(groupId)) {
        throw new NullPointerException("groupId cannot be null or empty.");
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
