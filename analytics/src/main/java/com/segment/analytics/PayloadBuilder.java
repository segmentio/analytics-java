package com.segment.analytics;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public abstract class PayloadBuilder<T extends Payload, V extends PayloadBuilder> {
  final Payload.Type type;
  final UUID messageId;
  final Date timestamp;
  Map<String, Object> context;
  UUID anonymousId;
  String userId;

  protected PayloadBuilder(Payload.Type type) {
    this.type = type;
    this.messageId = UUID.randomUUID();
    this.timestamp = new Date();
  }

  public V context(Map<String, Object> context) {
    if (context == null) {
      throw new NullPointerException("Null context");
    }
    this.context = context;
    return self();
  }

  public V anonymousId(UUID anonymousId) {
    if (anonymousId == null) {
      throw new NullPointerException("Null anonymousId");
    }
    this.anonymousId = anonymousId;
    return self();
  }

  public V userId(String userId) {
    if (userId == null) {
      // todo validate length?
      throw new NullPointerException("Null userId");
    }
    this.userId = userId;
    return self();
  }

  abstract T realBuild();

  abstract V self();

  public T build() {
    if (anonymousId == null && userId == null) {
      throw new IllegalStateException("Either anonymousId or userId must be provided.");
    }
    return realBuild();
  }
}
