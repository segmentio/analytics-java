package com.sio;

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
public class Segmentio {

	// DEFAULTS
	public static class Defaults {
		private static final boolean SECURE = true;
		private static final String ENVIRONMENT = "development";
		private static final String HOST = "api.segment.io";
	}
	
	private String apiKey;
	private String environment;
	private AsyncHttpClient client;
	private boolean secure;
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
	 * @param apiKey Your segment.io API KEY. You can get one of these by 
	 * registering a project at http://segment.io 
	 */
	public Segmentio(String apiKey) {
		
		this(apiKey, Defaults.ENVIRONMENT, new AsyncHttpClient(), Defaults.SECURE);
		
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
	 * @param apiKey Your segment.io API KEY. You can get one of these by 
	 * registering a project at http://segment.io 
	 * @param environment An environment allows you to separate your development,
	 * testing, and production data. Use "development", "testing", "production",
	 * or whatever best describes your current execution context.  
	 * Defaults to "development".
	 * @param secure Whether or not to use SSL to make the requests. Defaults to true.
	 */
	public Segmentio(String apiKey, String environment) {
		
		this(apiKey, environment, new AsyncHttpClient(), Defaults.SECURE);
		
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
	 * @param apiKey Your segment.io API KEY. You can get one of these by 
	 * registering a project at http://segment.io 
	 * @param secure Whether or not to use SSL to make the requests. Defaults to true.
	 */
	public Segmentio(String apiKey, boolean secure) {
		
		this(apiKey, Defaults.ENVIRONMENT, new AsyncHttpClient(), secure);
		
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
	 * @param apiKey Your segment.io API KEY. You can get one of these by 
	 * registering a project at http://segment.io 
	 * @param environment An environment allows you to separate your development,
	 * testing, and production data. Use "development", "testing", "production",
	 * or whatever best describes your current execution context.  
	 * Defaults to "development".
	 * @param secure Whether or not to use SSL to make the requests. Defaults to true.
	 */
	public Segmentio(String apiKey, String environment, 
			boolean secure) {
		
		this(apiKey, environment, new AsyncHttpClient(), secure);
		
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
	 * @param apiKey Your segment.io API KEY. You can get one of these by 
	 * registering a project at http://segment.io 
	 * @param environment An environment allows you to separate your development,
	 * testing, and production data. Use "development", "testing", "production",
	 * or whatever best describes your current execution context.  
	 * Defaults to "development".
	 * @param client The Async HTTP Client implementation that will be used to 
	 * make the HTTP requests on a different thread pool. If you want to customize
	 * this client to change settings such as connection pooling, or timeout,
	 * please use com.ning.http.client.AsyncHttpClientConfig.Builder to create
	 * your custom HTTP client object.
	 */
	public Segmentio(String apiKey, String environment, 
			AsyncHttpClient client) {
		
		this(apiKey, environment, client, Defaults.SECURE);
		
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
	 * @param apiKey Your segment.io API KEY. You can get one of these by 
	 * registering a project at http://segment.io 
	 * @param environment An environment allows you to separate your development,
	 * testing, and production data. Use "development", "testing", "production",
	 * or whatever best describes your current execution context.  
	 * Defaults to "development".
	 * @param client The Async HTTP Client implementation that will be used to 
	 * make the HTTP requests on a different thread pool. If you want to customize
	 * this client to change settings such as connection pooling, or timeout,
	 * please use com.ning.http.client.AsyncHttpClientConfig.Builder to create
	 * your custom HTTP client object.
	 * @param secure Whether or not to use SSL to make the requests. Defaults to true.
	 */
	public Segmentio(String apiKey, String environment, 
			AsyncHttpClient client, boolean secure) {
		
		String errorPrefix = 
				"Segmentio client must be initialized with a valid "; 
		
		if (StringUtils.isEmpty(apiKey))
			throw new IllegalArgumentException(errorPrefix + "apiKey.");
		
		if (StringUtils.isEmpty(environment))
			throw new IllegalArgumentException(errorPrefix + "environment.");
		
		if (client == null)
			throw new IllegalArgumentException(errorPrefix + "client.");
		
		this.apiKey = apiKey;
		this.environment = environment;
		this.client = client;
		this.secure = secure;
	}
	
	
	Segmentio(String apiKey, String environment, 
			AsyncHttpClient client, boolean secure, String host) {
	
		this(apiKey, environment, client, secure);
		
		if (StringUtils.isEmpty(host))
			throw new IllegalArgumentException("Host required.");
		
		this.host = host;
	}
	
	//
	// API Calls
	//
	
	//
	// Identify
	//
	
	/**
	 * Identifying a visitor ties all of their actions to an ID you 
	 * recognize and records visitor traits you can segment by.

	 * @param sessionId The visitor's anonymous identifier until they log in, or
	 * until your system knows who they are. In web systems, this is usually
	 * the ID of this user in the sessions table.  
	 * 
	 * @param visitorId The visitor's identifier after they log in, or you know
	 * who they are. This is usually an email, but any unique ID will work. 
	 * By explicitly identifying a user, you tie all of their actions to 
	 * their identity. This makes it possible for you to run things like 
	 * segment-based email campaigns.
	 * 
	 * @param context A dictionary with additional information thats related
	 * to the visit. Examples are userAgent, and IP address of the visitor. Feel
	 * free to pass in null if you don't have this information.
	 * 
	 * @param traits A dictionary with keys like “Subscription Plan” or 
	 * “Favorite Genre”. You can segment your users by any trait you record. 
	 * Pass in values in key-value format. String key, then its value { String,
	 * Integer, Boolean, Double, or Date are acceptable types for a value. }
	 * So, traits array could be: "Subscription Plan", "Premium", "Friend Count",
	 * 13 , and so forth.  
	 * 
	 */
	public void identify(String sessionId, String visitorId, 
			Context context, Object ... traits) {
		
		identify(sessionId, visitorId, context, null, new Traits(traits));
		
	}
	
	/**
	 * Identifying a visitor ties all of their actions to an ID you 
	 * recognize and records visitor traits you can segment by.

	 * @param sessionId The visitor's anonymous identifier until they log in, or
	 * until your system knows who they are. In web systems, this is usually
	 * the ID of this user in the sessions table.  
	 * 
	 * @param visitorId The visitor's identifier after they log in, or you know
	 * who they are. This is usually an email, but any unique ID will work. 
	 * By explicitly identifying a user, you tie all of their actions to 
	 * their identity. This makes it possible for you to run things like 
	 * segment-based email campaigns.
	 *
	 * @param context A dictionary with additional information thats related
	 * to the visit. Examples are userAgent, and IP address of the visitor. Feel
	 * free to pass in null if you don't have this information.
	 * 
	 * @param timestamp If this event happened in the past, the time stamp 
	 * can be used to designate when the identification happened. Use null to 
	 * let our server if it just happened.
	 * 
	 * @param traits A dictionary with keys like “Subscription Plan” or 
	 * “Favorite Genre”. You can segment your users by any trait you record. 
	 * Pass in values in key-value format. String key, then its value { String,
	 * Integer, Boolean, Double, or Date are acceptable types for a value. }
	 * So, traits array could be: "Subscription Plan", "Premium", "Friend Count",
	 * 13 , and so forth.  
	 * 
	 */
	public void identify(String sessionId, String visitorId, 
			Context context, DateTime timestamp, Traits traits) {
		
		
	}

	//
	// Track
	//
	
	/**
	 * Whenever a user triggers an event on your site, you’ll want to track it 
	 * so that you can analyze and segment by those events later.
	 * 
	 * @param visitorId The ID that you identified the visitor with. Use the best 
	 * identification you have of the visitor. If the visitor is anonymous (not 
	 * logged in), use the sessionId. If the visitor is logged in, use the 
	 * visitorId, which is the email or their username.
	 * 
	 * @param event The event name you are tracking. It is recommended that it
	 * is in human readable form. For example, "Bought T-Shirt" or 
	 * "Started an exercise"
	 * 
	 * @param properties A dictionary with items that describe the event in 
	 * more detail. This argument is optional, but highly recommended—you’ll 
	 * find these properties extremely useful later.
	 * 
	 */
	public void track(String visitorId, String event, Object ... properties) {
		
		track(visitorId, event, null, new EventProperties(properties));
		
	}
	
	/**
	 * Whenever a user triggers an event on your site, you’ll want to track it 
	 * so that you can analyze and segment by those events later.
	 * 
	 * @param visitorId The ID that you identified the visitor with. Use the best 
	 * identification you have of the visitor. If the visitor is anonymous (not 
	 * logged in), use the sessionId. If the visitor is logged in, use the 
	 * visitorId, which is the email or their username.
	 * 
	 * @param event The event name you are tracking. It is recommended that it
	 * is in human readable form. For example, "Bought T-Shirt" or 
	 * "Started an exercise"
	 * 
	 * @param timestamp If the user did this event in the past, use the timestamp
	 * to specify when it happened. Use null to let our server if it just happened.
	 * 
	 * @param properties A dictionary with items that describe the event in 
	 * more detail. This argument is optional, but highly recommended—you’ll 
	 * find these properties extremely useful later.
	 * 
	 * 
	 */
	public void track(String visitorId, String event, 
			DateTime timestamp, EventProperties properties) {
		
		
	}
	
	
	//
	// Getters and Setters
	//
	
	public String getApiKey() {
		return apiKey;
	}
	
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	
	public String getEnvironment() {
		return environment;
	}
	
	public void setEnvironment(String environment) {
		this.environment = environment;
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
