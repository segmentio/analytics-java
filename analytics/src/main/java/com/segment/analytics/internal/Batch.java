package com.segment.analytics.internal;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.segment.analytics.internal.gson.AutoGson;
import com.segment.analytics.messages.Message;
import java.util.Date;
import java.util.List;
import java.util.Map;

@AutoValue @AutoGson public abstract class Batch {
  private static final Map<String, Object> CONTEXT;

  static {
    ImmutableMap<String, String> library = new ImmutableMap.Builder<String, String>() //
        .put("name", "analytics-java") //
        .put("version", AnalyticsVersion.get()) //
        .build();
    CONTEXT = ImmutableMap.<String, Object>of("library", library);
  }

  public static Batch create(List<Message> batch) {
    return new AutoValue_Batch(batch, new Date(), CONTEXT);
  }

  public abstract List<Message> batch();

  public abstract Date sentAt();

  public abstract Map<String, Object> context();
}