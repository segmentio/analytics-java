package com.segment.analytics.messages;

import com.google.auto.value.AutoValue;
import com.segment.analytics.gson.AutoGson;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@AutoValue
@AutoGson
public abstract class Batch {
  private static final AtomicInteger SEQUENCE_GENERATOR = new AtomicInteger();

  public static Batch create(Map<String, ?> context, List<Message> batch, String writeKey) {
    Message sentAtNull =
        batch.stream().filter(message -> message.sentAt() != null).findAny().orElse(null);

    return new AutoValue_Batch(
        batch,
        sentAtNull == null ? new Date() : sentAtNull.sentAt(),
        context,
        SEQUENCE_GENERATOR.incrementAndGet(),
        writeKey);
  }

  public abstract List<Message> batch();

  public abstract Date sentAt();

  public abstract Map<String, ?> context();

  public abstract int sequence();

  public abstract String writeKey();
}
