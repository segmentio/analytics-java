package com.segment;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import com.ning.http.client.AsyncHttpClient;

/**
 * The Segmentio Client - Instantiate this to use the Segmentio API.
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
public class Analytics {

	// DEFAULTS
	public static class Defaults {
		private static final String HOST = "https://api.segment.io";
	}
	
	private String secret;
	private AsyncHttpClient client;
	private String host;
	
	
	/**
	 * Creates a new Segment.io client. 
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
	 */
	public Analytics(String secret) {
		
		this(secret, Defaults.HOST, new AsyncHttpClient());
		
	}
	
	/**
	 * Creates a new Segment.io client. 
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
	
	 * @param host Your segment.io REST endpoint
	 * 
	 */
	public Analytics(String secret, String host) {
		
		this(secret, host, new AsyncHttpClient());
	}

	
	/**
	 * Creates a new Segment.io client. 
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
	
	 * @param host Your segment.io REST endpoint
	 * 
	 * @param client The Async HTTP Client implementation that will be used to 
	 * make the HTTP requests on a different thread pool. If you want to customize
	 * this client to change settings such as connection pooling, or timeout,
	 * please use com.ning.http.client.AsyncHttpClientConfig.Builder to create
	 * your custom HTTP client object.
	 */
	public Analytics(String secret, String host, 
			AsyncHttpClient client) {
		
		String errorPrefix = 
				"analytics-java client must be initialized with a valid "; 
		
		if (StringUtils.isEmpty(secret))
			throw new IllegalArgumentException(errorPrefix + "secret.");
		
		if (StringUtils.isEmpty(host))
			throw new IllegalArgumentException(errorPrefix + "host.");
		
		if (client == null)
			throw new IllegalArgumentException(errorPrefix + "client.");
		
		this.secret = secret;
		this.host = host;
		this.client = client;
	}
	
	//
	// API Calls
	//
	
	//
	// Identify
	//
	

	public void identify(String sessionId, String visitorId, Object ... traits) {
		
		identify(sessionId, visitorId, null, null, new Traits(traits));
	}

	
	public void identify(String visitorId, Object ... traits) {
		
		identify(null, visitorId, null, null, new Traits(traits));
	}
	
	public void identify(String visitorId, 
			Context context, Object ... traits) {
		
		identify(null, visitorId, context, null, new Traits(traits));
	}
	
	public void identify(String sessionId, String visitorId, 
			Context context, Object ... traits) {
		
		identify(sessionId, visitorId, context, null, new Traits(traits));
	}
	

	public void identify(String sessionId, String visitorId, 
			Context context, DateTime timestamp, Traits traits) {
		
		
	}

	//
	// Track
	//

	public void track(String visitorId, String event) {
		
		track(null, visitorId, event, null, null, null);
	}
	
	public void track(String visitorId, String event, Object ... properties) {
		
		track(null, visitorId, event, null, new EventProperties(properties));
	}
	
	public void track(String sessionId, String visitorId, String event) {
		
		track(sessionId, visitorId, event, null, null);
	}

	public void track(String visitorId, String event, 
			DateTime timestamp, EventProperties properties) {
		
		track(null, visitorId, event, null, new EventProperties(properties));
	}
	
	public void track(String sessionId, String visitorId, String event, 
			Context context, DateTime timestamp, EventProperties properties) {
		
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
	
	public AsyncHttpClient getClient() {
		return client;
	}
	
	public String getHost() {
		return host;
	}
	
	//
	// 
	//
	
}
