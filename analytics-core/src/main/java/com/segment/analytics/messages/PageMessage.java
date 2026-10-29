package com.segment.analytics.messages;

import com.google.auto.value.AutoValue;
import com.segment.analytics.gson.AutoGson;
import jakarta.annotation.Nullable;
import java.util.Date;
import java.util.Map;

/**
 * The page call lets you record whenever a user sees a page of your website, along with any
 * properties about the page.
 *
 * <p>Use {@link #builder} to construct your own instances.
 *
 * @see <a href="https://segment.com/docs/spec/page/">Page</a>
 */
@AutoValue
@AutoGson //
public abstract class PageMessage implements Message {

  /**
   * Start building an {@link PageMessage} instance.
   *
   * @param name The name of the page the user is on.
   * @throws IllegalArgumentException if the page name is null or empty
   * @see <a href="https://segment.com/docs/spec/page/#name">Page</a>
   */
  public static Builder builder(String name) {
    return new Builder(name);
  }

  public abstract String name();

  @Nullable
  public abstract Map<String, ?> properties();

  @Nullable
  public abstract String category();

  public Builder toBuilder() {
    return new Builder(this);
  }

  /** Fluent API for creating {@link PageMessage} instances. */
  public static class Builder extends MessageBuilder<PageMessage, Builder> {
    private String name;
    private Map<String, ?> properties;
    private String category;

    private Builder(PageMessage page) {
      super(page);
      name = page.name();
      properties = page.properties();
      category = page.category();
    }

    private Builder(String name) {
      super(Type.page);
      if (isNullOrEmpty(name)) {
        throw new IllegalArgumentException("page name cannot be null or empty.");
      }
      this.name = name;
    }

    /**
     * Set a map of information that describe the page. These can be anything you want.
     *
     * @see <a href="https://segment.com/docs/spec/page/#properties">Properties</a>
     */
    public Builder properties(Map<String, ?> properties) {
      if (properties == null) {
        throw new NullPointerException("Null properties");
      }
      this.properties = ImmutableMap.copyOf(properties);
      return this;
    }

    public Builder category(String category) {
      if (category == null) {
        throw new NullPointerException("Null category");
      }
      this.category = category;
      return this;
    }

    @Override
    Builder self() {
      return this;
    }

    @Override
    protected PageMessage realBuild(
        Type type,
        String messageId,
        Date sentAt,
        Date timestamp,
        Map<String, ?> context,
        String anonymousId,
        String userId,
        Map<String, Object> integrations) {
      return new AutoValue_PageMessage(
          type,
          messageId,
          sentAt,
          timestamp,
          context,
          anonymousId,
          userId,
          integrations,
          name,
          properties,
          category);
    }
  }
}
