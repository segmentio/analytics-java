package com.segment.analytics;

import com.google.auto.value.AutoValue;
import com.segment.analytics.internal.gson.AutoGson;
import java.util.Date;
import java.util.UUID;

@AutoValue @AutoGson //
public abstract class AliasPayload implements Payload {

  public static Builder builder(String previousId) {
    return new Builder(previousId);
  }

  public abstract String previousId();

  public static class Builder extends PayloadBuilder<AliasPayload, Builder> {
    String previousId;

    private Builder(String previousId) {
      if (previousId == null) {
        // todo validate length?
        throw new NullPointerException("Null previousId");
      }
      this.previousId = previousId;
    }

    @Override AliasPayload realBuild() {
      return new AutoValue_AliasPayload(Type.ALIAS, UUID.randomUUID(), new Date(), context,
          anonymousId, userId, previousId);
    }

    @Override Builder self() {
      return this;
    }
  }
}
