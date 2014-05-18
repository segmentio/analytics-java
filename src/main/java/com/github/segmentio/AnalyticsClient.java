package com.github.segmentio;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import com.github.segmentio.flush.Flusher;
import com.github.segmentio.flush.IBatchFactory;
import com.github.segmentio.models.Alias;
import com.github.segmentio.models.BasePayload;
import com.github.segmentio.models.Batch;
import com.github.segmentio.models.Callback;
import com.github.segmentio.models.Context;
import com.github.segmentio.models.EventProperties;
import com.github.segmentio.models.Identify;
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
	private Config options;
	
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
	 * @param options
	 *            Options to configure the behavior of the Segment.io client
	 * 
	 * 
	 */
	public AnalyticsClient(String writeKey, Config options) {

		String errorPrefix = "analytics-java client must be initialized with a valid ";

		if (StringUtils.isEmpty(writeKey))
			throw new IllegalArgumentException(errorPrefix + "writeKey.");

		if (options == null)
			throw new IllegalArgumentException(errorPrefix + "options.");

		this.writeKey = writeKey;
		this.options = options;
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
	 * @param traits
	 *            a dictionary with keys like email, name, subscriptionPlan or
	 *            age. You only need to record a trait once, no need to send it
	 *            again.
	 */
	public void identify(String userId, Traits traits) {

		identify(userId, traits, null, null, null);
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
	 * @param context
	 *            an object that describes anything that doesn't fit into this
	 *            event's properties (such as the user's IP)
	 * 
	 */
	public void identify(String userId, Traits traits, Context context) {

		identify(userId, traits, null, context, null);
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
	 * @param timestamp
	 *            a {@link DateTime} representing when the identify took place.
	 *            If the identify just happened, leave it blank and we'll use
	 *            the server's time. If you are importing data from the past,
	 *            make sure you provide this argument.
	 * 
	 * @param context
	 *            an object that describes anything that doesn't fit into this
	 *            event's properties (such as the user's IP)
	 * 
	 */
	public void identify(String userId, Traits traits, DateTime timestamp,
			Context context) {

		identify(userId, traits, timestamp, context, null);
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
	 * @param timestamp
	 *            a {@link DateTime} representing when the identify took place.
	 *            If the identify just happened, leave it blank and we'll use
	 *            the server's time. If you are importing data from the past,
	 *            make sure you provide this argument.
	 * 
	 * @param context
	 *            an object that describes anything that doesn't fit into this
	 *            event's properties (such as the user's IP)
	 * 
	 * @param callback
	 *            a callback that is fired when this track's batch is flushed to
	 *            the server. Note: this callback is fired on the same thread as
	 *            the async event loop that made the request. You should not
	 *            perform any kind of long running operation on it.
	 */
	public void identify(String userId, Traits traits, DateTime timestamp,
			Context context, Callback callback) {

		if (context == null)
			context = new Context();
		if (traits == null)
			traits = new Traits();

		Identify identify = new Identify(userId, traits, timestamp, context,
				callback);

		flusher.enqueue(identify);
		
		statistics.updateIdentifies(1);
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

		track(userId, event, null, null, null, null);
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
	public void track(String userId, String event, EventProperties properties) {

		track(userId, event, properties, null, null, null);
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
	 * @param timestamp
	 *            a {@link DateTime} object representing when the track took
	 *            place. If the event just happened, leave it blank and we'll
	 *            use the server's time. If you are importing data from the
	 *            past, make sure you provide this argument.
	 * 
	 */
	public void track(String userId, String event, EventProperties properties,
			DateTime timestamp) {

		track(userId, event, properties, timestamp, null, null);
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
	 * @param timestamp
	 *            a {@link DateTime} object representing when the track took
	 *            place. If the event just happened, leave it blank and we'll
	 *            use the server's time. If you are importing data from the
	 *            past, make sure you provide this argument.
	 * 
	 * @param context
	 *            an object that describes anything that doesn't fit into this
	 *            event's properties (such as the user's IP)
	 * 
	 */
	public void track(String userId, String event, EventProperties properties,
			DateTime timestamp, Context context) {

		track(userId, event, properties, timestamp, context, null);
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
	 * @param timestamp
	 *            a {@link DateTime} object representing when the track took
	 *            place. If the event just happened, leave it blank and we'll
	 *            use the server's time. If you are importing data from the
	 *            past, make sure you provide this argument.
	 * 
	 * @param context
	 *            an object that describes anything that doesn't fit into this
	 *            event's properties (such as the user's IP)
	 * 
	 * @param callback
	 *            a callback that is fired when this track's batch is flushed to
	 *            the server. Note: this callback is fired on the same thread as
	 *            the async event loop that made the request. You should not
	 *            perform any kind of long running operation on it.
	 */
	public void track(String userId, String event, EventProperties properties,
			DateTime timestamp, Context context, Callback callback) {

		if (context == null)
			context = new Context();
		if (properties == null)
			properties = new EventProperties();

		Track track = new Track(userId, event, properties, timestamp, context,
				callback);

		flusher.enqueue(track);
		
		statistics.updateTracks(1);
	}

	
	//
	// Alias
	//

	/**
	 * Aliases an anonymous user into an identified user.
	 * 
	 * @param from
	 *            the anonymous user's id before they are logged in.
	 * 
	 * @param to
	 *            the identified user's id after they're logged in.
	 *           
	 */
	public void alias(String from, String to) {
		alias(from, to, null, null, null);
	}

	/**
	 * Aliases an anonymous user into an identified user.
	 * 
	 * @param from
	 *            the anonymous user's id before they are logged in.
	 * 
	 * @param to
	 *            the identified user's id after they're logged in.
	 * 
	 * 
	 * @param timestamp
	 *            a {@link DateTime} object representing when the track took
	 *            place. If the event just happened, leave it blank and we'll
	 *            use the server's time. If you are importing data from the
	 *            past, make sure you provide this argument.
	 * 
	 *           
	 */
	public void alias(String from, String to, DateTime timestamp) {
		alias(from, to, timestamp, null, null);
	}

	/**
	 * Aliases an anonymous user into an identified user.
	 * 
	 * @param from
	 *            the anonymous user's id before they are logged in.
	 * 
	 * @param to
	 *            the identified user's id after they're logged in.
	 * 
	 * 
	 * @param context
	 *            an object that describes anything that doesn't fit into this
	 *            event's properties (such as the user's IP)
	 *           
	 */
	public void alias(String from, String to, Context context) {
		alias(from, to, null, context, null);
	}

	/**
	 * Aliases an anonymous user into an identified user.
	 * 
	 * @param from
	 *            the anonymous user's id before they are logged in.
	 * 
	 * @param to
	 *            the identified user's id after they're logged in.
	 * 
	 * 
	 * @param timestamp
	 *            a {@link DateTime} object representing when the track took
	 *            place. If the event just happened, leave it blank and we'll
	 *            use the server's time. If you are importing data from the
	 *            past, make sure you provide this argument.
	 * 
	 * @param context
	 *            an object that describes anything that doesn't fit into this
	 *            event's properties (such as the user's IP)
	 *           
	 */
	public void alias(String from, String to, DateTime timestamp, Context context) {
		alias(from, to, timestamp, context, null);
	}
	

	/**
	 * Aliases an anonymous user into an identified user.
	 * 
	 * @param from
	 *            the anonymous user's id before they are logged in.
	 * 
	 * @param to
	 *            the identified user's id after they're logged in.
	 * 
	 * 
	 * @param timestamp
	 *            a {@link DateTime} object representing when the track took
	 *            place. If the event just happened, leave it blank and we'll
	 *            use the server's time. If you are importing data from the
	 *            past, make sure you provide this argument.
	 * 
	 * @param context
	 *            an object that describes anything that doesn't fit into this
	 *            event's properties (such as the user's IP)
	 *
	 * @param callback
	 *            a callback that is fired when this track's batch is flushed to
	 *            the server. Note: this callback is fired on the same thread as
	 *            the async event loop that made the request. You should not
	 *            perform any kind of long running operation on it.
	 *             
	 */
	public void alias(String from, String to, DateTime timestamp, Context context, Callback callback) {
		
		if (context == null)
			context = new Context();

		Alias alias = new Alias(from, to, timestamp, context, callback);

		flusher.enqueue(alias);
		
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
		return options;
	}

	public AnalyticsStatistics getStatistics() {
		return statistics;
	}

}
