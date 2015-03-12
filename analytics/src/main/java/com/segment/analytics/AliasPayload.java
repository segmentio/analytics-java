package com.segment.analytics;

import com.google.auto.value.AutoValue;
import com.segment.analytics.internal.gson.AutoGson;

@AutoValue @AutoGson //
public abstract class AliasPayload implements Payload {

  public static Builder builder(String previousId) {
    return new Builder(previousId);
  }

  public abstract String previousId();

  public static class Builder extends PayloadBuilder<AliasPayload, Builder> {
    String previousId;

    Builder(String previousId) {
      super(Type.ALIAS);

      if (previousId == null) {
        // todo validate length?
        throw new NullPointerException("Null previousId");
      }
      this.previousId = previousId;
    }

    @Override AliasPayload realBuild() {
      return new AutoValue_AliasPayload(type, messageId, timestamp, context, anonymousId, userId,
          previousId);
    }

    @Override Builder self() {
      return this;
    }
  }
}
