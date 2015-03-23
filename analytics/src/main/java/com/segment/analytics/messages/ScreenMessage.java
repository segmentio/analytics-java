package com.segment.analytics.messages;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.segment.analytics.internal.gson.AutoGson;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

import static com.segment.analytics.internal.Utils.isNullOrEmpty;

@AutoValue @AutoGson public abstract class ScreenMessage implements Message {
  public static Builder builder() {
    return new Builder();
  }

  @Nullable public abstract String name();

  @Nullable public abstract String category();

  @Nullable public abstract Map<String, Object> properties();

  public Builder toBuilder() {
    return new Builder(this);
  }

  public static class Builder extends MessageBuilder<ScreenMessage, Builder> {
    private String name;
    private String category;
    private Map<String, Object> properties;

    private Builder(ScreenMessage screen) {
      super(screen);
      name = screen.name();
      category = screen.category();
      properties = screen.properties();
    }

    private Builder() {
    }

    public Builder name(String name) {
      if (name == null) {
        throw new NullPointerException("Null name");
      }
      this.name = name;
      return this;
    }

    public Builder category(String category) {
      if (isNullOrEmpty(category)) {
        throw new NullPointerException("category cannot be null or empty.");
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

    @Override Builder self() {
      return this;
    }

    @Override protected ScreenMessage realBuild() {
      if (name == null && category == null) {
        throw new IllegalStateException("Either name or category must be provided.");
      }

      return new AutoValue_ScreenMessage(Type.SCREEN, UUID.randomUUID(), new Date(), context,
          anonymousId, userId, name, category, properties);
    }
  }
}
