package com.segment;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import com.google.gson.Gson;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import com.segment.models.BasePayload;
import com.segment.models.Batch;
import com.segment.models.Context;
import com.segment.models.EventProperties;
import com.segment.models.Identify;
import com.segment.models.Track;
import com.segment.models.Traits;
import com.segment.safeclient.AsyncHttpBatchedOperation;
import com.segment.safeclient.policy.flush.FlushAfterTimePolicy;
import com.segment.safeclient.policy.flush.FlushAtSizePolicy;
import com.segment.safeclient.policy.flush.IFlushPolicy;

/**
 * The Segment.io Client - Instantiate this to use the Segment.io API.
 * 
 * The client is an HTTP wrapper over the Segment.io REST API. 
 * It will allow you to conveniently consume the API without
 * making any HTTP requests yourself. 
 * 
 * This client is also designed to be 
 * thread-safe and to not block each of your calls to make a HTTP request.
 * It uses batching to efficiently send your requests on a separate 
 * resource-constrained thread pool.
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
	 * The client is an HTTP wrapper over the Segment.io REST API. 
	 * It will allow you to conveniently consume the API without
	 * making any HTTP requests yourself. 
	 * 
	 * This client is also designed to be 
	 * thread-safe and to not block each of your calls to make a HTTP request.
	 * It uses batching to efficiently send your requests on a separate 
	 * resource-constrained thread pool.
	 * 

	 * @param secret Your segment.io secret. You can get one of these by 
	 * registering for a project at https://segment.io 
	 * 
	 */
	public Client(String secret) {
		
		this(secret, new Options());
	}
	
	/**
	 * Creates a new Segment.io client. 
	 * 
	 * The client is an HTTP wrapper over the Segment.io REST API. 
	 * It will allow you to conveniently consume the API without
	 * making any HTTP requests yourself. 
	 * 
	 * This client is also designed to be 
	 * thread-safe and to not block each of your calls to make a HTTP request.
	 * It uses batching to efficiently send your requests on a separate 
	 * resource-constrained thread pool.
	 * 
     * 
	 * @param secret Your segment.io secret. You can get one of these by 
	 * registering for a project at https://segment.io 
	 *
	 * @param options Options to configure the behavior of the Segment.io client
	 *
	 * 
	 */
	public Client(String secret, Options options) {
	
		String errorPrefix = 
				"analytics-java client must be initialized with a valid "; 
		
		if (StringUtils.isEmpty(secret))
			throw new IllegalArgumentException(errorPrefix + "secret.");
		
		if (options == null)
			throw new IllegalArgumentException(errorPrefix + "options.");
		
		this.secret = secret;
		this.options = options;
		
		this.gson = new Gson();
		this.operation = buildOperation(new AsyncHttpClient(options.getHttpConfig()));
	}

	
	
	//
	// API Calls
	//
	
	//
	// Identify
	//
	

	public void identify(String sessionId, String userId, Object ... traits) {
		
		identify(sessionId, userId, null, null, new Traits(traits));
	}

	
	public void identify(String userId, Object ... traits) {
		
		identify(null, userId, null, null, new Traits(traits));
	}
	
	public void identify(String userId, 
			Context context, Object ... traits) {
		
		identify(null, userId, context, null, new Traits(traits));
	}
	
	public void identify(String sessionId, String userId, 
			Context context, Object ... traits) {
		
		identify(sessionId, userId, context, null, new Traits(traits));
	}
	

	public void identify(String sessionId, String userId, 
			Context context, DateTime timestamp, Traits traits) {

		Identify identify = new Identify(sessionId, userId, timestamp, context, traits);

		operation.perform(identify);
	}

	//
	// Track
	//

	public void track(String userId, String event) {
		
		track(null, userId, event, null, null, null);
	}
	
	public void track(String userId, String event, Object ... properties) {
		
		track(null, userId, event, null, new EventProperties(properties));
	}
	
	public void track(String sessionId, String userId, String event) {
		
		track(sessionId, userId, event, null, null);
	}

	public void track(String userId, String event, 
			DateTime timestamp, EventProperties properties) {
		
		track(null, userId, event, null, new EventProperties(properties));
	}
	
	public void track(String sessionId, String userId, String event, 
			Context context, DateTime timestamp, EventProperties properties) {
		
		Track track = new Track(sessionId, userId, event, timestamp, context, properties);
		
		operation.perform(track);
	}
	
	//
	// Flush Actions
	//
	
	/**
	 * Flushes the current contents of the queue
	 */
	public void flush () {
		operation.flush();
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
	
	public AsyncHttpBatchedOperation<BasePayload> buildOperation(AsyncHttpClient client) { 
		
		return new AsyncHttpBatchedOperation<BasePayload>(client) {
	
			@Override
			protected int getMaxQueueSize() {
				return options.getQueueCapacity();
			}
			
			@Override
			protected Iterable<IFlushPolicy> createFlushPolicies() {

				return Arrays.asList(		
					new FlushAfterTimePolicy(options.getFlushAfter()),
					new FlushAtSizePolicy(options.getFlushAt())
				);
			}
			
			@Override
			public Request buildRequest(List<BasePayload> batch) {
				
				Batch model = new Batch(secret, batch);
				
				String json = gson.toJson(model);
				
				return new RequestBuilder()
							.setMethod("POST")
							.setBody(json)
							.addHeader("Content-Type", "application/json")
							.setUrl(Client.this.options.getHost() + "/v1/import")
							.build();
			}
		};
	};
	
}
