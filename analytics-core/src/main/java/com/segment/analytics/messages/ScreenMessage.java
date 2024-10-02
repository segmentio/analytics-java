package com.segment.analytics.messages;

import com.google.auto.value.AutoValue;
import com.segment.analytics.gson.AutoGson;
import jakarta.annotation.Nullable;
import java.util.Date;
import java.util.Map;

/**
 * The screen call lets you record whenever a user sees a screen, along with any properties about
 * the screen.
 *
 * <p>Use {@link #builder} to construct your own instances.
 *
 * @see <a href="https://segment.com/docs/spec/screen/">Screen</a>
 */
@AutoValue
@AutoGson //
public abstract class ScreenMessage implements Message {

  /**
   * Start building an {@link ScreenMessage} instance.
   *
   * @param name The name of the screen the user is on.
   * @throws IllegalArgumentException if the screen name is null or empty
   * @see <a href="https://segment.com/docs/spec/screen/#name">Name</a>
   */
  public static Builder builder(String name) {
    return new Builder(name);
  }

  public abstract String name();

  @Nullable
  public abstract Map<String, ?> properties();

  public Builder toBuilder() {
    return new Builder(this);
  }

  /** Fluent API for creating {@link ScreenMessage} instances. */
  public static class Builder extends MessageBuilder<ScreenMessage, Builder> {
    private String name;
    private Map<String, ?> properties;

    private Builder(ScreenMessage screen) {
      super(screen);
      name = screen.name();
      properties = screen.properties();
    }

    private Builder(String name) {
      super(Type.screen);
      if (isNullOrEmpty(name)) {
        throw new IllegalArgumentException("screen name cannot be null or empty.");
      }
      this.name = name;
    }

    /**
     * Set a map of information that describe the screen. These can be anything you want.
     *
     * @see <a href="https://segment.com/docs/spec/screen/#properties">Properties</a>
     */
    public Builder properties(Map<String, ?> properties) {
      if (properties == null) {
        throw new NullPointerException("Null properties");
      }
      this.properties = ImmutableMap.copyOf(properties);
      return this;
    }

    @Override
    Builder self() {
      return this;
    }

    @Override
    protected ScreenMessage realBuild(
        Type type,
        String messageId,
        Date sentAt,
        Date timestamp,
        Map<String, ?> context,
        String anonymousId,
        String userId,
        Map<String, Object> integrations) {
      return new AutoValue_ScreenMessage(
          type,
          messageId,
          sentAt,
          timestamp,
          context,
          anonymousId,
          userId,
          integrations,
          name,
          properties);
    }
  }
}
