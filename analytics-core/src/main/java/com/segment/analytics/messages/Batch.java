package com.segment.analytics.messages;

import com.google.auto.value.AutoValue;
import com.segment.analytics.gson.AutoGson;
import java.util.Date;
import java.util.List;
import java.util.Map;

@AutoValue @AutoGson public abstract class Batch {
  public static Batch create(Map<String, Object> context, List<Message> batch) {
    return new AutoValue_Batch(batch, new Date(), context);
  }

  public abstract List<Message> batch();

  public abstract Date sentAt();

  public abstract Map<String, Object> context();
}