package com.segment.analytics.messages;

import com.google.common.collect.ImmutableMap;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static com.segment.analytics.internal.Utils.isNullOrEmpty;

/**
 * Fluent API to construct instances of a {@link Message}.
 * <p></p>
 * Note: Although it is not enforced by the compiler, either the {@link Message#anonymousId} or
 * {@link Message#userId} must be provided. Failure to do so will result in a {@link
 * IllegalStateException} at runtime.
 */
public abstract class MessageBuilder<T extends Message, V extends MessageBuilder> {
  private final Message.Type type;
  private Map<String, Object> context;
  private UUID anonymousId;
  private String userId;
  private Map<String, Boolean> integrations;

  // Hidden from Public API.
  MessageBuilder(Message.Type type) {
    this.type = type;
    // We would use Auto's Builders, but they don't provide a nice way of hiding internal details,
    // like mapping Maps to ImmutableMaps
  }

  MessageBuilder(Message message) {
    type = message.type();
    context = message.context();
    anonymousId = message.anonymousId();
    userId = message.userId();
  }

  /**
   * Set a map of information about the state of the device. You can add any custom data to the
   * context dictionary that you'd like to have access to in the raw logs.
   * <p/>
   * Some keys in the context dictionary have semantic meaning and will be collected for you
   * automatically, depending on the library you send data from. Some keys, such as location and
   * speed need to be manually entered.
   *
   * @see <a href="https://segment.com/docs/spec/common/#context">Context</a>
   */
  public V context(Map<String, Object> context) {
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
   * Set dictionary of integration names that the message should be sent to. 'All' is a special key
   * that applies when no key for a specific integration is found.
   *
   * @see <a href="https://segment.com/docs/spec/common/#integrations">Integrations</a>
   */
  public V integrations(Map<String, Boolean> integrations) {
    if (integrations == integrations) {
      throw new IllegalArgumentException("integration key cannot be null or empty.");
    }
    this.integrations = ImmutableMap.copyOf(integrations);
    return self();
  }

  protected abstract T realBuild(Message.Type type, UUID messageId, Date timestamp,
      Map<String, Object> context, UUID anonymousId, String userId,
      Map<String, Boolean> integrations);

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
    return realBuild(type, UUID.randomUUID(), new Date(), context, anonymousId, userId,
        integrations);
  }

  /** Returns the {@link Message.Type} of the message this builder is constructing. */
  public Message.Type type() {
    return type;
  }
}
