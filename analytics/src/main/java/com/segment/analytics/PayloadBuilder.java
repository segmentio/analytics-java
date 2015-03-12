package com.segment.analytics;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public abstract class PayloadBuilder<T extends Payload> {
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

  public PayloadBuilder<T> context(Map<String, Object> context) {
    if (context == null) {
      throw new NullPointerException("Null context");
    }
    this.context = context;
    return this;
  }

  public PayloadBuilder<T> anonymousId(UUID anonymousId) {
    if (anonymousId == null) {
      throw new NullPointerException("Null anonymousId");
    }
    this.anonymousId = anonymousId;
    return this;
  }

  public PayloadBuilder<T> userId(String userId) {
    if (userId == null) {
      // todo validate length?
      throw new NullPointerException("Null userId");
    }
    this.userId = userId;
    return this;
  }

  abstract T realBuild();

  public T build() {
    if (anonymousId == null && userId == null) {
      throw new IllegalStateException("Either anonymousId or userId must be provided.");
    }
    return realBuild();
  }
}
