package com.segment.analytics;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.segment.analytics.internal.gson.AutoGson;
import java.util.Map;
import javax.annotation.Nullable;

@AutoValue @AutoGson //
public abstract class ScreenPayload implements Payload {
  @Nullable public abstract String name();

  @Nullable public abstract String category();

  @Nullable public abstract Map<String, Object> properties();

  public static Builder builderForNamedPages(String name) {
    return new Builder().name(name);
  }

  public static Builder builderForCategorizedPages(String category) {
    return new Builder().category(category);
  }

  public static class Builder extends PayloadBuilder<ScreenPayload> {
    String name;
    String category;
    Map<String, Object> properties;

    Builder() {
      super(Type.SCREEN);
    }

    public Builder name(String name) {
      if (name == null) {
        throw new NullPointerException("Null name");
      }
      this.name = name;
      return this;
    }

    public Builder category(String category) {
      if (category == null) {
        throw new NullPointerException("Null category");
      }
      this.category = category;
      return this;
    }

    public Builder properties(Map<String, Object> properties) {
      if (properties == null) {
        throw new NullPointerException("Null properties");
      }
      this.properties = ImmutableMap.copyOf(properties);
      return this;
    }

    @Override ScreenPayload realBuild() {
      return new AutoValue_ScreenPayload(type, messageId, timestamp, context, anonymousId, userId,
          name, category, properties);
    }
  }
}
