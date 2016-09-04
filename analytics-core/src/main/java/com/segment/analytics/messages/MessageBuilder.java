package com.segment.analytics.messages;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Fluent API to construct instances of a {@link Message}.
 * <p></p>
 * Note: Although it is not enforced by the compiler, either the {@link Message#anonymousId} or
 * {@link Message#userId} must be provided before calling {@link #build()}. Failure to do so will
 * result in a {@link IllegalStateException} at runtime.
 */
public abstract class MessageBuilder<T extends Message, V extends MessageBuilder> {
  private String messageId;
  private final Message.Type type;
  private Map<String, ?> context;
  private UUID anonymousId;
  private String userId;
  private Map<String, Object> integrationsBuilder;
  private Date timestamp;

  // Hidden from Public API.
  MessageBuilder(Message.Type type) {
    this.type = type;
    // We would use Auto's Builders, but they don't provide a nice way of hiding internal details,
    // like mapping Maps to ImmutableMaps
  }

  MessageBuilder(Message message) {
    messageId = message.messageId();
    type = message.type();
    context = message.context();
    anonymousId = message.anonymousId();
    userId = message.userId();
  }

  /** Returns {@code true} if the given string is null or empty. */
  static boolean isNullOrEmpty(String string) {
    return string == null || string.trim().length() == 0;
  }

  /**
   * The Message ID is a unique identifier for this message. If not specified, a unique identifier will
   * be generated.
   */
  public V messageId(String messageId) {
    if (messageId == null) {
      throw new NullPointerException("Null messageId");
    }
    if (messageId.isEmpty()) {
      throw new IllegalArgumentException("Empty messageId");
    }
    if (messageId.length() > Message.MAX_MESSAGE_ID_LENGTH) {
      throw new IllegalArgumentException("messageId longer than " + Message.MAX_MESSAGE_ID_LENGTH + " characters");
    }
    this.messageId = messageId;
    return self();
  }

  /**
   * Set a map of information about the state of the device. You can add any custom data to the
   * context dictionary that you'd like to have access to in the raw logs.
   * <p>
   * Some keys in the context dictionary have semantic meaning and will be collected for you
   * automatically, depending on the library you send data from. Some keys, such as location and
   * speed need to be manually entered.
   *
   * @see <a href="https://segment.com/docs/spec/common/#context">Context</a>
   */
  public V context(Map<String, ?> context) {
    if (context == null) {
      throw new NullPointerException("Null context");
    }
    this.context = ImmutableMap.copyOf(context);
    return self();
  }

  /**
   * The Anonymous ID is a pseudo-unique substitute for a User ID, for cases when you don’t have an
   * absolutely unique identifier.
   *
   * @see <a href="https://segment.com/docs/spec/identify/#identities">Identities</a>
   * @see <a href="https://segment.com/docs/spec/identify/#anonymous-id">Anonymous ID</a>
   */
  public V anonymousId(UUID anonymousId) {
    if (anonymousId == null) {
      throw new NullPointerException("Null anonymousId");
    }
    this.anonymousId = anonymousId;
    return self();
  }

  /**
   * The Anonymous ID is a pseudo-unique substitute for a User ID, for cases when you don’t have an
   * absolutely unique identifier.
   *
   * @see <a href="https://segment.com/docs/spec/identify/#identities">Identities</a>
   * @see <a href="https://segment.com/docs/spec/identify/#user-id">User ID</a>
   */
  public V userId(String userId) {
    if (isNullOrEmpty(userId)) {
      throw new IllegalArgumentException("userId cannot be null or empty.");
    }
    this.userId = userId;
    return self();
  }

  /**
   * Set whether this message is sent to the specified integration or not. 'All' is a special key
   * that applies when no key for a specific integration is found.
   *
   * @see <a href="https://segment.com/docs/spec/common/#integrations">Integrations</a>
   */
  public V enableIntegration(String key, boolean enable) {
    if (isNullOrEmpty(key)) {
      throw new IllegalArgumentException("Key cannot be null or empty.");
    }
    if (integrationsBuilder == null) {
      integrationsBuilder = new LinkedHashMap<>();
    }
    integrationsBuilder.put(key, enable);
    return self();
  }

  /**
   * Pass in some options that will only be used by the target integration. This will implicitly
   * mark the integration as enabled.
   *
   * @see <a href="https://segment.com/docs/spec/common/#integrations">Integrations</a>
   */
  public V integrationOptions(String key, Map<String, ? super Object> options) {
    if (isNullOrEmpty(key)) {
      throw new IllegalArgumentException("Key name cannot be null or empty.");
    }
    if (integrationsBuilder == null) {
      integrationsBuilder = new LinkedHashMap<>();
    }
    integrationsBuilder.put(key, ImmutableMap.copyOf(options));
    return self();
  }

  /**
   * Set a timestamp for the event. By default, the current timestamp is used, but you may override
   * it for historical import.
   *
   * @see <a href="https://segment.com/docs/spec/common/#-timestamp-">Timestamp</a>
   */
  public V timestamp(Date timestamp) {
    if (timestamp == null) {
      throw new NullPointerException("Null timestamp");
    }
    this.timestamp = timestamp;
    return self();
  }

  protected abstract T realBuild(Message.Type type, String messageId, Date timestamp,
      Map<String, ?> context, UUID anonymousId, String userId, Map<String, Object> integrations);

  abstract V self();

  /**
   * Create a {@link Message} instance.
   *
   * @throws IllegalStateException if both anonymousId and userId are not provided.
   */
  public T build() {
    if (anonymousId == null && userId == null) {
      throw new IllegalStateException("Either anonymousId or userId must be provided.");
    }
    Map<String, Object> integrations = integrationsBuilder == null ? //
        Collections.<String, Object>emptyMap() : ImmutableMap.copyOf(integrationsBuilder);
    return realBuild(type, messageId == null ? UUID.randomUUID().toString() : messageId,
        timestamp == null ? new Date() : timestamp, context,
        anonymousId, userId, integrations);
  }

  /** Returns the {@link Message.Type} of the message this builder is constructing. */
  public Message.Type type() {
    return type;
  }
}
