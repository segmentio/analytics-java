package com.segment.analytics.messages;

import com.google.auto.value.AutoValue;
import com.segment.analytics.gson.AutoGson;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

/**
 * The identify call ties a customer and their actions to a recognizable ID and traits like their
 * email, name, etc.
 * <p>
 * Use {@link #builder} to construct your own instances.
 *
 * @see <a href="https://segment.com/docs/spec/identify/">Identify</a>
 */
@AutoValue @AutoGson //
public abstract class IdentifyMessage implements Message {

  /** Start building an {@link IdentifyMessage} instance. */
  public static Builder builder() {
    return new Builder();
  }

  @Nullable public abstract Map<String, ?> traits();

  public Builder toBuilder() {
    return new Builder(this);
  }

  /** Fluent API for creating {@link IdentifyMessage} instances. */
  public static class Builder extends MessageBuilder<IdentifyMessage, Builder> {
    private Map<String, ?> traits;

    private Builder(IdentifyMessage identify) {
      super(identify);
      traits = identify.traits();
    }

    private Builder() {
      super(Type.identify);
    }

    /**
     * Set a map of information of the user, like email or name.
     *
     * @see <a href="https://segment.com/docs/spec/identify/#traits">Traits</a>
     */
    public Builder traits(Map<String, ?> traits) {
      if (traits == null) {
        throw new NullPointerException("Null traits");
      }
      this.traits = ImmutableMap.copyOf(traits);
      return this;
    }

    @Override protected IdentifyMessage realBuild(Type type, UUID messageId, Date timestamp,
        Map<String, ?> context, UUID anonymousId, String userId,
        Map<String, Object> integrations) {
      if (userId == null && traits == null) {
        throw new IllegalStateException("Either userId or traits must be provided.");
      }

      return new AutoValue_IdentifyMessage(type, messageId, timestamp, context, anonymousId, userId,
          integrations, traits);
    }

    @Override Builder self() {
      return this;
    }
  }
}
