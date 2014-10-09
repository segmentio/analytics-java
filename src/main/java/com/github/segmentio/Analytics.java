package com.github.segmentio;

import com.github.segmentio.models.Options;
import com.github.segmentio.models.Props;
import com.github.segmentio.models.Traits;
import com.github.segmentio.stats.AnalyticsStatistics;

public class Analytics {
  public final static String VERSION = "1.0.0";

  private static AnalyticsClient defaultClient;

  /**
   * Creates a new Segment.io client.
   *
   * The client is an HTTP wrapper over the Segment.io REST API. It will allow
   * you to conveniently consume the API without making any HTTP requests
   * yourself.
   *
   * This client is also designed to be thread-safe and to not block each of
   * your calls to make a HTTP request. It uses batching to efficiently send
   * your requests on a separate resource-constrained thread pool.
   *
   * This method is thread-safe.
   *
   * @param writeKey Your segment.io writeKey. You can get one of these by
   * registering for a project at https://segment.io
   */
  public static synchronized void initialize(String writeKey) {
    if (defaultClient == null) {
      defaultClient = new AnalyticsClient(writeKey, new Config());
    }
  }

  /**
   * Creates a new Segment.io client.
   *
   * The client is an HTTP wrapper over the Segment.io REST API. It will allow
   * you to conveniently consume the API without making any HTTP requests
   * yourself.
   *
   * This client is also designed to be thread-safe and to not block each of
   * your calls to make a HTTP request. It uses batching to efficiently send
   * your requests on a separate resource-constrained thread pool.
   *
   * This method is thread-safe.
   *
   * @param writeKey Your segment.io writeKey. You can get one of these by
   * registering for a project at https://segment.io
   * @param options Options to configure the behavior of the Segment.io client
   */
  public static synchronized void initialize(String writeKey, Config options) {
    if (defaultClient == null) {
      defaultClient = new AnalyticsClient(writeKey, options);
    }
  }

  private static synchronized void checkInitialized() {
    if (defaultClient == null) {
      throw new IllegalStateException("Analytics client is "
          + "not initialized. Please call Analytics.iniitalize(..); "
          + "before calling identify / track / or flush.");
    }
  }

  //
  // API Calls
  //

  //
  // Identify
  //

  /**
   * Identifying a user ties all of their actions to an id, and associates
   * user traits to that id.
   *
   * @param userId the user's id after they are logged in. It's the same id as
   * which you would recognize a signed-in user in your system.
   */
  public void identify(String userId) {
    identify(userId, null, null);
  }

  /**
   * Identifying a user ties all of their actions to an id, and associates
   * user traits to that id.
   *
   * @param userId the user's id after they are logged in. It's the same id as
   * which you would recognize a signed-in user in your system.
   * @param traits a dictionary with keys like subscriptionPlan or age. You only
   * need to record a trait once, no need to send it again.
   */
  public void identify(String userId, Traits traits) {
    identify(userId, traits, null);
  }

  /**
   * Identifying a user ties all of their actions to an id, and associates
   * user traits to that id.
   *
   * @param userId the user's id after they are logged in. It's the same id as
   * which you would recognize a signed-in user in your system.
   * @param traits a dictionary with keys like subscriptionPlan or age. You only
   * need to record a trait once, no need to send it again.
   * @param options options allows you to set a timestamp,
   * an anonymousId, a context, and target integrations.
   */
  public void identify(String userId, Traits traits, Options options) {
    checkInitialized();
    defaultClient.identify(userId, traits, options);
  }

  //
  // Group
  //

  /**
   * The `group` method lets you associate a user with a group. Be it a company,
   * organization, account, project or team! It also lets you record custom traits about the
   * group, like industry or number of employees.
   *
   * @param userId the user's id after they are logged in. It's the same id as
   * which you would recognize a signed-in user in your system.
   * @param groupId the group's id as it would appear in your database.
   * @param traits a dictionary with keys like subscriptionPlan or age. You only
   * need to record a trait once, no need to send it again.
   */
  public void group(String userId, String groupId, Traits traits) {
    group(userId, groupId, traits, null);
  }

  /**
   * The `group` method lets you associate a user with a group. Be it a company,
   * organization, account, project or team! It also lets you record custom traits about the
   * group, like industry or number of employees.
   *
   * @param userId the user's id after they are logged in. It's the same id as
   * which you would recognize a signed-in user in your system.
   * @param groupId the group's id as it would appear in your database.
   * @param traits a dictionary with keys like subscriptionPlan or age. You only
   * need to record a trait once, no need to send it again.
   * @param options options allows you to set a timestamp,
   * an anonymousId, a context, and target integrations.
   */
  public void group(String userId, String groupId, Traits traits, Options options) {
    checkInitialized();
    defaultClient.group(userId, groupId, traits, options);
  }

  //
  // Track
  //

  /**
   * Whenever a user triggers an event, you’ll want to track it.
   *
   * @param userId the user's id after they are logged in. It's the same id as
   * which you would recognize a signed-in user in your system.
   * @param event describes what this user just did. It's a human readable
   * description like "Played a Song", "Printed a Report" or
   * "Updated Status".
   */
  public void track(String userId, String event) {
    track(userId, event, null, null);
  }

  /**
   * Whenever a user triggers an event, you’ll want to track it.
   *
   * @param userId the user's id after they are logged in. It's the same id as
   * which you would recognize a signed-in user in your system.
   * @param event describes what this user just did. It's a human readable
   * description like "Played a Song", "Printed a Report" or
   * "Updated Status".
   * @param properties a dictionary with items that describe the event in more
   * detail. This argument is optional, but highly
   * recommended—you’ll find these properties extremely useful
   * later.
   */
  public void track(String userId, String event, Props properties) {
    track(userId, event, properties, null);
  }

  /**
   * Whenever a user triggers an event, you’ll want to track it.
   *
   * @param userId the user's id after they are logged in. It's the same id as
   * which you would recognize a signed-in user in your system.
   * @param event describes what this user just did. It's a human readable
   * description like "Played a Song", "Printed a Report" or
   * "Updated Status".
   * @param properties a dictionary with items that describe the event in more
   * detail. This argument is optional, but highly
   * recommended—you’ll find these properties extremely useful
   * later.
   * @param options options allows you to set a timestamp,
   * an anonymousId, a context, and target integrations.
   */
  public void track(String userId, String event, Props properties, Options options) {
    checkInitialized();
    defaultClient.track(userId, event, properties, options);
  }

  //
  // Page
  //

  /**
   * The `page` method let your record whenever a user sees a web page on
   * your web site, and attach a `name`, `category` or `properties` to the web page load.
   *
   * @param userId the user's id after they are logged in. It's the same id as
   * which you would recognize a signed-in user in your system.
   * @param name The name of the web page, like "Signup", "Login"
   */
  public void page(String userId, String name) {
    page(userId, name, null, null, null);
  }

  /**
   * The `page` method let your record whenever a user sees a web page on
   * your web site, and attach a `name`, `category` or `properties` to the web page load.
   *
   * @param userId the user's id after they are logged in. It's the same id as
   * which you would recognize a signed-in user in your system.
   * @param name The name of the web page, like "Signup", "Login"
   * @param properties a dictionary with items that describe the event in more
   * detail. This argument is optional, but highly
   * recommended—you’ll find these properties extremely useful
   * later.
   */
  public void page(String userId, String name, Props properties) {
    page(userId, name, null, properties, null);
  }

  /**
   * The `page` method let your record whenever a user sees a web page on
   * your web site, and attach a `name`, `category` or `properties` to the web page load.
   *
   * @param userId the user's id after they are logged in. It's the same id as
   * which you would recognize a signed-in user in your system.
   * @param name The name of the web page, like "Signup", "Login"
   * @param properties a dictionary with items that describe the event in more
   * detail. This argument is optional, but highly
   * recommended—you’ll find these properties extremely useful
   * later.
   * @param options options allows you to set a timestamp,
   * an anonymousId, a context, and target integrations.
   */
  public void page(String userId, String name, Props properties, Options options) {
    page(userId, name, null, properties, options);
  }

  /**
   * The `page` method let your record whenever a user sees a web page on
   * your web site, and attach a `name`, `category` or `properties` to the web page load.
   *
   * @param userId the user's id after they are logged in. It's the same id as
   * which you would recognize a signed-in user in your system.
   * @param name The name of the web page, like "Signup", "Login"
   * @param category The category of the web page, like "Sports" or "Authentication"
   * @param properties a dictionary with items that describe the event in more
   * detail. This argument is optional, but highly
   * recommended—you’ll find these properties extremely useful
   * later.
   * @param options options allows you to set a timestamp,
   * an anonymousId, a context, and target integrations.
   */
  public void page(String userId, String name, String category, Props properties, Options options) {
    checkInitialized();
    defaultClient.page(userId, name, category, properties, options);
  }

  //
  // Screen
  //

  /**
   * The `screen` method let your record whenever a user sees a mobile screen,
   * and attach a `name`, `category` or `properties` to the web page load.
   *
   * @param userId the user's id after they are logged in. It's the same id as
   * which you would recognize a signed-in user in your system.
   * @param name The name of the mobile screen, like "Signup", "Login"
   */
  public void screen(String userId, String name) {
    screen(userId, name, null, null, null);
  }

  /**
   * The `screen` method let your record whenever a user sees a mobile screen,
   * and attach a `name`, `category` or `properties` to the web page load.
   *
   * @param userId the user's id after they are logged in. It's the same id as
   * which you would recognize a signed-in user in your system.
   * @param name The name of the mobile screen, like "Signup", "Login"
   * @param properties a dictionary with items that describe the event in more
   * detail. This argument is optional, but highly
   * recommended—you’ll find these properties extremely useful
   * later.
   */
  public void screen(String userId, String name, Props properties) {
    screen(userId, name, null, properties, null);
  }

  /**
   * The `screen` method let your record whenever a user sees a mobile screen,
   * and attach a `name`, `category` or `properties` to the web page load.
   *
   * @param userId the user's id after they are logged in. It's the same id as
   * which you would recognize a signed-in user in your system.
   * @param name The name of the mobile screen, like "Signup", "Login"
   * @param properties a dictionary with items that describe the event in more
   * detail. This argument is optional, but highly
   * recommended—you’ll find these properties extremely useful
   * later.
   * @param options options allows you to set a timestamp,
   * an anonymousId, a context, and target integrations.
   */
  public void screen(String userId, String name, Props properties, Options options) {
    screen(userId, name, null, properties, options);
  }

  /**
   * The `screen` method let your record whenever a user sees a mobile screen,
   * and attach a `name`, `category` or `properties` to the web page load.
   *
   * @param userId the user's id after they are logged in. It's the same id as
   * which you would recognize a signed-in user in your system.
   * @param name The name of the mobile screen, like "Signup", "Login"
   * @param category The category of the web page, like "Sports" or "Authentication"
   * @param properties a dictionary with items that describe the event in more
   * detail. This argument is optional, but highly
   * recommended—you’ll find these properties extremely useful
   * later.
   * @param options options allows you to set a timestamp,
   * an anonymousId, a context, and target integrations.
   */
  public void screen(String userId, String name, String category, Props properties,
      Options options) {
    checkInitialized();
    defaultClient.screen(userId, name, category, properties, options);
  }

  //
  // Alias
  //

  /**
   * Aliases an anonymous user into an identified user.
   *
   * @param previousId the anonymous user's id before they are logged in.
   * @param userId the identified user's id after they're logged in.
   */
  public void alias(String previousId, String userId) {
    alias(previousId, userId, null);
  }

  /**
   * Aliases an anonymous user into an identified user.
   *
   * @param previousId the anonymous user's id before they are logged in.
   * @param userId the identified user's id after they're logged in.
   * @param options options allows you to set a timestamp,
   * an anonymousId, a context, and target integrations.
   */
  public void alias(String previousId, String userId, Options options) {
    checkInitialized();
    defaultClient.alias(previousId, userId, options);
  }

  //
  // Flush Actions
  //

  /**
   * Blocks until all messages in the queue are flushed.
   */
  public static void flush() {
    checkInitialized();
    defaultClient.flush();
  }

  /**
   * Closes the threads associated with the client
   */
  public static void close() {
    checkInitialized();
    defaultClient.close();
  }

  /**
   * Returns statistics for the analytics client
   */
  public synchronized static AnalyticsStatistics getStatistics() {
    checkInitialized();
    return defaultClient.getStatistics();
  }

  /**
   * Fetches the default analytics client singleton
   */
  public synchronized static AnalyticsClient getDefaultClient() {
    checkInitialized();
    return defaultClient;
  }
}
