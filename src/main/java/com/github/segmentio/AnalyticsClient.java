package com.github.segmentio;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.github.segmentio.flush.Flusher;
import com.github.segmentio.flush.IBatchFactory;
import com.github.segmentio.models.Alias;
import com.github.segmentio.models.BasePayload;
import com.github.segmentio.models.Batch;
import com.github.segmentio.models.Options;
import com.github.segmentio.models.Props;
import com.github.segmentio.models.Group;
import com.github.segmentio.models.Identify;
import com.github.segmentio.models.Page;
import com.github.segmentio.models.Screen;
import com.github.segmentio.models.Track;
import com.github.segmentio.models.Traits;
import com.github.segmentio.request.IRequester;
import com.github.segmentio.request.RetryingRequester;
import com.github.segmentio.stats.AnalyticsStatistics;

/**
 * The Segment.io Client - Instantiate this to use the Segment.io API.
 * 
 * The client is an HTTP wrapper over the Segment.io REST API. It will allow you
 * to conveniently consume the API without making any HTTP requests yourself.
 * 
 * This client is also designed to be thread-safe and to not block each of your
 * calls to make a HTTP request. It uses batching to efficiently send your
 * requests on a separate resource-constrained thread pool.
 * 
 */
public class AnalyticsClient {

	private String writeKey;
	private Config config;
	
	private Flusher flusher;
	private IRequester requester;
	private AnalyticsStatistics statistics;

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
	 * 
	 * @param writeKey
	 *            Your segment.io writeKey. You can get one of these by
	 *            registering for a project at https://segment.io
	 * 
	 */
	public AnalyticsClient(String writeKey) {

		this(writeKey, new Config());
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
	 * 
	 * @param writeKey
	 *            Your segment.io writeKey. You can get one of these by
	 *            registering for a project at https://segment.io
	 * 
	 * @param config
	 *            Configure the behavior of this Segment.io client
	 * 
	 * 
	 */
	public AnalyticsClient(String writeKey, Config config) {

		String errorPrefix = "analytics-java client must be initialized with a valid ";

		if (StringUtils.isEmpty(writeKey))
			throw new IllegalArgumentException(errorPrefix + "writeKey.");

		if (config == null)
			throw new IllegalArgumentException(errorPrefix + "config.");

		this.writeKey = writeKey;
		this.config = config;
		this.statistics = new AnalyticsStatistics();
	    this.requester = new RetryingRequester(this);
		
		this.flusher = new Flusher(this, factory, requester);
		this.flusher.start();
	}
	
	private IBatchFactory factory = new IBatchFactory() {
		
		public Batch create(List<BasePayload> batch) {
			return new Batch(writeKey, batch);
		}
	};

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
	 * @param userId
	 *            the user's id after they are logged in. It's the same id as
	 *            which you would recognize a signed-in user in your system.
	 * 
	 */
	public void identify(String userId) {
		identify(userId, null, null);
	}

	/**
	 * Identifying a user ties all of their actions to an id, and associates
	 * user traits to that id.
	 * 
	 * @param userId
	 *            the user's id after they are logged in. It's the same id as
	 *            which you would recognize a signed-in user in your system.
	 * 
	 * @param traits
	 *            a dictionary with keys like subscriptionPlan or age. You only
	 *            need to record a trait once, no need to send it again.
	 * 
	 */
	public void identify(String userId, Traits traits) {
		identify(userId, traits, null);
	}

	/**
	 * Identifying a user ties all of their actions to an id, and associates
	 * user traits to that id.
	 * 
	 * @param userId
	 *            the user's id after they are logged in. It's the same id as
	 *            which you would recognize a signed-in user in your system.
	 * 
	 * @param traits
	 *            a dictionary with keys like subscriptionPlan or age. You only
	 *            need to record a trait once, no need to send it again.
	 * 
	 * @param options
	 *            options allows you to set a timestamp, 
     *            an anonymousId, a context, and target integrations.
	 */
	public void identify(String userId, Traits traits, Options options) {
		flusher.enqueue(new Identify(userId, traits, options));
		statistics.updateIdentifies(1);
	}

	//
	// Group
	//

	/**
	 * The `group` method lets you associate a user with a group. Be it a company, 
	 * organization, account, project or team! It also lets you record custom traits about the 
     * group, like industry or number of employees.
	 * 
	 * @param userId
	 *            the user's id after they are logged in. It's the same id as
	 *            which you would recognize a signed-in user in your system.
	 * 
	 * @param groupId
	 *            the group's id as it would appear in your database.
	 * 
	 * @param traits
	 *            a dictionary with keys like subscriptionPlan or age. You only
	 *            need to record a trait once, no need to send it again.
	 */
	public void group(String userId, String groupId, Traits traits) {
		group(userId, groupId, traits, null);
	}
	
	/**
	 * The `group` method lets you associate a user with a group. Be it a company, 
	 * organization, account, project or team! It also lets you record custom traits about the 
     * group, like industry or number of employees.
	 * 
	 * @param userId
	 *            the user's id after they are logged in. It's the same id as
	 *            which you would recognize a signed-in user in your system.
	 * 
	 * @param groupId
	 *            the group's id as it would appear in your database.
	 * 
	 * @param traits
	 *            a dictionary with keys like subscriptionPlan or age. You only
	 *            need to record a trait once, no need to send it again.
	 * 
	 * @param options
	 *            options allows you to set a timestamp, 
     *            an anonymousId, a context, and target integrations.
	 */
	public void group(String userId, String groupId, Traits traits, Options options) {
		flusher.enqueue(new Group(userId, groupId, traits, options));
		statistics.updateGroup(1);
	}
	
	//
	// Track
	//

	/**
	 * Whenever a user triggers an event, you’ll want to track it.
	 * 
	 * @param userId
	 *            the user's id after they are logged in. It's the same id as
	 *            which you would recognize a signed-in user in your system.
	 * 
	 * @param event
	 *            describes what this user just did. It's a human readable
	 *            description like "Played a Song", "Printed a Report" or
	 *            "Updated Status".
	 * 
	 */
	public void track(String userId, String event) {
		track(userId, event, null, null);
	}

	/**
	 * Whenever a user triggers an event, you’ll want to track it.
	 * 
	 * @param userId
	 *            the user's id after they are logged in. It's the same id as
	 *            which you would recognize a signed-in user in your system.
	 * 
	 * @param event
	 *            describes what this user just did. It's a human readable
	 *            description like "Played a Song", "Printed a Report" or
	 *            "Updated Status".
	 * 
	 * @param properties
	 *            a dictionary with items that describe the event in more
	 *            detail. This argument is optional, but highly
	 *            recommended—you’ll find these properties extremely useful
	 *            later.
	 */
	public void track(String userId, String event, Props properties) {
		track(userId, event, properties, null);
	}

	/**
	 * Whenever a user triggers an event, you’ll want to track it.
	 * 
	 * @param userId
	 *            the user's id after they are logged in. It's the same id as
	 *            which you would recognize a signed-in user in your system.
	 * 
	 * @param event
	 *            describes what this user just did. It's a human readable
	 *            description like "Played a Song", "Printed a Report" or
	 *            "Updated Status".
	 * 
	 * @param properties
	 *            a dictionary with items that describe the event in more
	 *            detail. This argument is optional, but highly
	 *            recommended—you’ll find these properties extremely useful
	 *            later.
	 * 
     * @param options
	 *            options allows you to set a timestamp, 
     *            an anonymousId, a context, and target integrations.
	 */
	public void track(String userId, String event, Props properties, Options options) {
		flusher.enqueue(new Track(userId, event, properties, options));
		statistics.updateTracks(1);
	}
	
	//
	// Page
	//

	/**
	 * The `page` method let your record whenever a user sees a web page on 
	 * your web site, and attach a `name`, `category` or `properties` to the web page load. 
	 *
	 * @param userId
	 *            the user's id after they are logged in. It's the same id as
	 *            which you would recognize a signed-in user in your system.
	 * 
	 * @param name
	 *            The name of the web page, like "Signup", "Login"
	 */
	public void page(String userId, String name) {
		page(userId, name, null, null, null);
	}

	/**
	 * The `page` method let your record whenever a user sees a web page on 
	 * your web site, and attach a `name`, `category` or `properties` to the web page load. 
	 *
	 * @param userId
	 *            the user's id after they are logged in. It's the same id as
	 *            which you would recognize a signed-in user in your system.
	 * 
	 * @param name
	 *            The name of the web page, like "Signup", "Login"
	 * 
	 * @param properties
	 *            a dictionary with items that describe the event in more
	 *            detail. This argument is optional, but highly
	 *            recommended—you’ll find these properties extremely useful
	 *            later.
	 */
	public void page(String userId, String name, Props properties) {
		page(userId, name, null, properties, null);
	}
	
	/**
	 * The `page` method let your record whenever a user sees a web page on 
	 * your web site, and attach a `name`, `category` or `properties` to the web page load. 
	 *
	 * @param userId
	 *            the user's id after they are logged in. It's the same id as
	 *            which you would recognize a signed-in user in your system.
	 * 
	 * @param name
	 *            The name of the web page, like "Signup", "Login"
	 * 
	 * @param properties
	 *            a dictionary with items that describe the event in more
	 *            detail. This argument is optional, but highly
	 *            recommended—you’ll find these properties extremely useful
	 *            later.
	 * 
     * @param options
	 *            options allows you to set a timestamp, 
     *            an anonymousId, a context, and target integrations.
	 */
	public void page(String userId, String name, Props properties, Options options) {
		page(userId, name, null, properties, options);
	}
	
	/**
	 * The `page` method let your record whenever a user sees a web page on 
	 * your web site, and attach a `name`, `category` or `properties` to the web page load. 
	 
	 * @param userId
	 *            the user's id after they are logged in. It's the same id as
	 *            which you would recognize a signed-in user in your system.
	 * 
	 * @param name
	 *            The name of the web page, like "Signup", "Login"
	 * 
	 * @param category
	 *            The category of the web page, like "Sports" or "Authentication"
	 * 
	 * @param properties
	 *            a dictionary with items that describe the event in more
	 *            detail. This argument is optional, but highly
	 *            recommended—you’ll find these properties extremely useful
	 *            later.
	 * 
     * @param options
	 *            options allows you to set a timestamp, 
     *            an anonymousId, a context, and target integrations.
	 */
	public void page(String userId, String name, String category, 
			Props properties, Options options) {
		flusher.enqueue(new Page(userId, name, category, properties, options));
		statistics.updatePage(1);
	}
	
	//
	// Screen
	//

	/**
	 * The `screen` method let your record whenever a user sees a mobile screen, 
	 * and attach a `name`, `category` or `properties` to the web page load. 
	 *
	 * @param userId
	 *            the user's id after they are logged in. It's the same id as
	 *            which you would recognize a signed-in user in your system.
	 * 
	 * @param name
	 *            The name of the mobile screen, like "Signup", "Login"
	 */
	public void screen(String userId, String name) {
		screen(userId, name, null, null, null);
	}

	/**
	 * The `screen` method let your record whenever a user sees a mobile screen, 
	 * and attach a `name`, `category` or `properties` to the web page load. 
	 *
	 * @param userId
	 *            the user's id after they are logged in. It's the same id as
	 *            which you would recognize a signed-in user in your system.
	 * 
	 * @param name
	 *            The name of the mobile screen, like "Signup", "Login"
	 * 
	 * @param properties
	 *            a dictionary with items that describe the event in more
	 *            detail. This argument is optional, but highly
	 *            recommended—you’ll find these properties extremely useful
	 *            later.
	 */
	public void screen(String userId, String name, Props properties) {
		screen(userId, name, null, properties, null);
	}
	
	/**
	 * The `screen` method let your record whenever a user sees a mobile screen, 
	 * and attach a `name`, `category` or `properties` to the web page load. 
	 *
	 * @param userId
	 *            the user's id after they are logged in. It's the same id as
	 *            which you would recognize a signed-in user in your system.
	 * 
	 * @param name
	 *            The name of the mobile screen, like "Signup", "Login"
	 * 
	 * @param properties
	 *            a dictionary with items that describe the event in more
	 *            detail. This argument is optional, but highly
	 *            recommended—you’ll find these properties extremely useful
	 *            later.
	 * 
     * @param options
	 *            options allows you to set a timestamp, 
     *            an anonymousId, a context, and target integrations.
	 */
	public void screen(String userId, String name, Props properties, Options options) {
		screen(userId, name, null, properties, options);
	}
	
	/**
	 * The `screen` method let your record whenever a user sees a mobile screen, 
	 * and attach a `name`, `category` or `properties` to the web page load. 
	 *
	 * @param userId
	 *            the user's id after they are logged in. It's the same id as
	 *            which you would recognize a signed-in user in your system.
	 * 
	 * @param name
	 *            The name of the mobile screen, like "Signup", "Login"
	 * 
	 * @param category
	 *            The category of the web page, like "Sports" or "Authentication"
	 * 
	 * @param properties
	 *            a dictionary with items that describe the event in more
	 *            detail. This argument is optional, but highly
	 *            recommended—you’ll find these properties extremely useful
	 *            later.
	 * 
     * @param options
	 *            options allows you to set a timestamp, 
     *            an anonymousId, a context, and target integrations.
	 */
	public void screen(String userId, String name, String category, 
			Props properties, Options options) {
		flusher.enqueue(new Screen(userId, name, category, properties, options));
		statistics.updateScreen(1);
	}
	
	//
	// Alias
	//

	/**
	 * Aliases an anonymous user into an identified user.
	 * 
	 * @param previousId
	 *            the anonymous user's id before they are logged in.
	 * 
	 * @param userId
	 *            the identified user's id after they're logged in.
	 *           
	 */
	public void alias(String previousId, String userId) {
		alias(previousId, userId, null);
	}

	/**
	 * Aliases an anonymous user into an identified user.
	 * 
	 * @param previousId
	 *            the anonymous user's id before they are logged in.
	 * 
	 * @param userId
	 *            the identified user's id after they're logged in.
	 *        
	 * @param options
	 *            options allows you to set a timestamp, 
     *            an anonymousId, a context, and target integrations.
	 *             
	 */
	public void alias(String previousId, String userId, Options options) {
		flusher.enqueue(new Alias(previousId, userId, options));
		statistics.updateAlias(1);
	}
	
	//
	// Actions
	//


	/**
	 * Blocks until all messages in the queue are flushed.
	 */
	public void flush() {
		this.flusher.flush();
	}

	/**
	 * Closes the queue and the threads associated with flushing the queue
	 */
	public void close() {
		this.flusher.close();
		this.requester.close();
	}

	//
	// Getters and Setters
	//

	public String getWriteKey() {
		return writeKey;
	}

	public void setWriteKey(String writeKey) {
		this.writeKey = writeKey;
	}

	public Config getOptions() {
		return config;
	}

	public AnalyticsStatistics getStatistics() {
		return statistics;
	}
	
	public int getQueueDepth() {
		return flusher.getQueueDepth();
	}

}
