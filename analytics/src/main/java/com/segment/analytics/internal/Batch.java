package com.segment.analytics.internal;

import com.google.auto.value.AutoValue;
import com.segment.analytics.internal.gson.AutoGson;
import com.segment.analytics.messages.Message;
import java.util.Date;
import java.util.List;
import java.util.Map;

@AutoValue @AutoGson public abstract class Batch {
  public static Batch create(List<Message> batch, Map<String, Object> context, int retryCount) {
    return new AutoValue_Batch(batch, new Date(), context, retryCount);
  }

  public abstract List<Message> batch();

  public abstract Date sentAt();

  public abstract Map<String, Object> context();

  public abstract int retryCount();
}