package com.segment;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.Response;
import com.segment.gson.DateTimeTypeConverter;
import com.segment.models.BasePayload;
import com.segment.models.Batch;
import com.segment.models.Callback;
import com.segment.models.Context;
import com.segment.models.EventProperties;
import com.segment.models.Identify;
import com.segment.models.Track;
import com.segment.models.Traits;
import com.segment.safeclient.AsyncHttpBatchedOperation;
import com.segment.safeclient.policy.flush.FlushAfterTimePolicy;
import com.segment.safeclient.policy.flush.FlushAtSizePolicy;
import com.segment.safeclient.policy.flush.IFlushPolicy;
import com.segment.safeclient.utils.Statistics;

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
public class Client {

	private String secret;
	private Options options;

	private Gson gson;
	private AsyncHttpBatchedOperation<BasePayload> operation;

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
	 * @param secret
	 *            Your segment.io secret. You can get one of these by
	 *            registering for a project at https://segment.io
	 * 
	 */
	public Client(String secret) {

		this(secret, new Options());
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
	 * @param secret
	 *            Your segment.io secret. You can get one of these by
	 *            registering for a project at https://segment.io
	 * 
	 * @param options
	 *            Options to configure the behavior of the Segment.io client
	 * 
	 * 
	 */
	public Client(String secret, Options options) {

		String errorPrefix = "analytics-java client must be initialized with a valid ";

		if (StringUtils.isEmpty(secret))
			throw new IllegalArgumentException(errorPrefix + "secret.");

		if (options == null)
			throw new IllegalArgumentException(errorPrefix + "options.");

		this.secret = secret;
		this.options = options;

		this.gson = new GsonBuilder().registerTypeAdapter(DateTime.class,
				new DateTimeTypeConverter()).create();

		this.operation = buildOperation(new AsyncHttpClient(
				options.getHttpConfig()));
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

		operation.perform(identify);
	}

	/**
	 * Enqueue an identify or track payload
	 * 
	 * @param payload
	 */
	public void enqueue(BasePayload payload) {
		operation.perform(payload);
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

		operation.perform(track);
	}

	//
	// Actions
	//

	/**
	 * Flushes the current contents of the queue
	 */
	public void flush() {
		operation.flush();
	}

	/**
	 * Closes the queue and the threads associated with flushing the queue
	 */
	public void close() {
		operation.close();
	}

	//
	// Getters and Setters
	//

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public Options getOptions() {
		return options;
	}

	public Statistics getStatistics() {
		return operation.statistics;
	}

	public AsyncHttpBatchedOperation<BasePayload> buildOperation(
			AsyncHttpClient client) {

		return new AsyncHttpBatchedOperation<BasePayload>(client) {

			@Override
			protected int getMaxQueueSize() {
				return options.getMaxQueueSize();
			}

			@Override
			protected Iterable<IFlushPolicy> createFlushPolicies() {

				return Arrays.asList(
						new FlushAfterTimePolicy(options.getFlushAfter()),
						new FlushAtSizePolicy(options.getFlushAt()));
			}

			@Override
			public Request buildRequest(List<BasePayload> batch) {

				Batch model = new Batch(secret, batch);

				String json = gson.toJson(model);

				return new RequestBuilder().setMethod("POST").setBody(json)
						.addHeader("Content-Type", "application/json")
						.setUrl(Client.this.options.getHost() + "/v1/import")
						.build();
			}

			@Override
			public void onFlush(List<BasePayload> batch, Response response) {

				for (BasePayload payload : batch) {
					Callback callback = payload.getCallback();

					if (callback != null) {
						callback.onResponse(response);
					}
				}
			}
		};
	};

}
