package com.segment.analytics.messages;

import com.google.auto.value.AutoValue;
import com.segment.analytics.internal.gson.AutoGson;
import java.util.Date;
import java.util.UUID;

import static com.segment.analytics.internal.Utils.isNullOrEmpty;

@AutoValue @AutoGson public abstract class AliasMessage implements Message {

  public static Builder builder(String previousId) {
    return new Builder(previousId);
  }

  public abstract String previousId();

  public static class Builder extends PayloadBuilder<AliasMessage, Builder> {
    private String previousId;

    private Builder(String previousId) {
      if (isNullOrEmpty(previousId)) {
        throw new NullPointerException("previousId cannot be null or empty.");
      }
      this.previousId = previousId;
    }

    @Override protected AliasMessage realBuild() {
      return new AutoValue_AliasMessage(Type.ALIAS, UUID.randomUUID(), new Date(), context,
          anonymousId, userId, previousId);
    }

    @Override Builder self() {
      return this;
    }
  }
}
