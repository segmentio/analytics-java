package com.segment;

import com.ning.http.client.AsyncHttpClientConfig;

/**
 * Segment.io client options
 *
 */
public class Options {
	
	/**
	 * The REST API endpoint (with scheme)
	 */
	private String host;
	
	/**
	 * Flush after these many messages are added to the queue
	 */
	private int flushAt;
	
	/**
	 * Flush after this many milliseconds have passed without a flush
	 */
	private int flushAfter;
	
	/**
	 * Stop accepting messages after the queue reaches this capacity
	 */
	private int maxQueueSize;
	
	/**
	 * The HTTP Client used to flush
	 */
	private AsyncHttpClientConfig httpConfig;

	/**
	 * Creates a default options
	 */
	public Options() {
		this(Defaults.HOST, 
		     Defaults.FLUSH_AT, 
		     Defaults.FLUSH_AFTER, 
		     Defaults.MAX_QUEUE_SIZE,
		     Defaults.CONFIG);
	}
	
	/**
	 * Creates an option with the provided settings
	 * @param flushAt
	 * @param flushAfter
	 * @param maxQueueSize
	 * @param httpConfig
	 */
	Options(String host,
				   int flushAt,
				   int flushAfter,
				   int maxQueueSize,
				   AsyncHttpClientConfig httpConfig) {
		
		this.host = host;
		this.flushAt = flushAt;
		this.flushAfter = flushAfter;
		this.maxQueueSize = maxQueueSize;
		this.httpConfig = httpConfig;
	}

	
	public int getFlushAt() {
		return flushAt;
	}
	
	public int getFlushAfter() {
		return flushAfter;
	}
	
	public String getHost() {
		return host;
	}
	
	public int getMaxQueueSize() {
		return maxQueueSize;
	}
	
	public AsyncHttpClientConfig getHttpConfig() {
		return httpConfig;
	}
	
	/**
	 * Sets the amount of messages that need to be in the queue before it is flushed
	 * @param flushAt
	 */
	public Options setFlushAt(int flushAt) {
		this.flushAt = flushAt;
		return this;
	}
	
	/**
	 * Sets the maximum amount of time to queue before invoking a flush (in milliseconds)
	 * @param flushAfter
	 */
	public Options setFlushAfter(int flushAfter) {
		this.flushAfter = flushAfter;
		return this;
	}
	
	/**
	 * Sets the maximum queue capacity, which is an emergency pressure relief valve. If we're unable
	 * to flush messages fast enough, the queue will stop accepting messages after this capacity is reached.
	 * @param maxQueueSize
	 */
	public Options setMaxQueueSize(int maxQueueSize) {
		this.maxQueueSize = maxQueueSize;
		return this;
	}
	
	/**
	 * Sets the REST API endpoint
	 * @param host
	 */
	public Options setHost(String host) {
		this.host = host;
		return this;
	}
	
	/**
	 * Sets the HTTP client async configuration
	 * @param httpConfig
	 */
	public Options setHttpConfig(AsyncHttpClientConfig httpConfig) {
		this.httpConfig = httpConfig;
		return this;
	}
	
	
}
