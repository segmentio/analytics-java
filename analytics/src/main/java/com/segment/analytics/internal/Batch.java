package com.segment.analytics.internal;

import com.google.auto.value.AutoValue;
import com.segment.analytics.Payload;
import com.segment.analytics.internal.gson.AutoGson;
import java.util.Date;
import java.util.List;
import java.util.Map;

@AutoValue @AutoGson public abstract class Batch {
  public abstract List<Payload> batch();

  public abstract Date sentAt();

  public abstract Map<String, Object> context();

  public static Batch create(List<Payload> batch, Map<String, Object> context) {
    return new AutoValue_Batch(batch, new Date(), context);
  }
}