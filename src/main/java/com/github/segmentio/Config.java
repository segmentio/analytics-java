package com.github.segmentio;

import org.apache.commons.lang.StringUtils;


/**
 * Segment.io client options
 * 
 */
public class Config {

	/**
	 * The REST API endpoint (with scheme)
	 */
	private String host;

	/**
	 * Stop accepting messages after the queue reaches this capacity
	 */
	private int maxQueueSize;

	/**
	 * The amount of milliseconds that passes before a request is marked as timed out
	 */
	private int timeout;
	
	/**
	 * How many times to retry the request.
	 */
	
	private int retries;
	
	/**
	 * Backoff in milliseconds between retries.
	 */
	
	private int backoff;
	
	/**
	 * Creates a default options
	 */
	public Config() {
		this(Defaults.HOST, Defaults.MAX_QUEUE_SIZE, Defaults.TIMEOUT, Defaults.RETRIES, Defaults.BACKOFF);
	}

	/**
	 * Creates an option with the provided settings
	 * 
	 * @param host
	 * @param maxQueueSize
	 * @param timeout
	 * @param retries
	 * @param backoff
	 */
	Config(String host, int maxQueueSize, int timeout, int retries, int backoff) {
		setHost(host);
		setMaxQueueSize(maxQueueSize);
		setTimeout(timeout);
		setRetries(retries);
		setBackoff(backoff);
	}

	public String getHost() {
		return host;
	}

	public int getMaxQueueSize() {
		return maxQueueSize;
	}

	public int getTimeout() {
		return timeout;
	}

	public int getRetries() {
		return retries;
	}

	public int getBackoff() {
		return backoff;
	}
	
	/**
	 * Sets the maximum queue capacity, which is an emergency pressure relief
	 * valve. If we're unable to flush messages fast enough, the queue will stop
	 * accepting messages after this capacity is reached.
	 * 
	 * @param maxQueueSize
	 */
	public Config setMaxQueueSize(int maxQueueSize) {
		if (maxQueueSize < 1)
			throw new IllegalArgumentException("Analytics#option#maxQueueSize must be greater than 0.");
		
		this.maxQueueSize = maxQueueSize;
		return this;
	}

	/**
	 * Sets the REST API endpoint
	 * 
	 * @param host
	 */
	public Config setHost(String host) {
		if (StringUtils.isEmpty(host))
			throw new IllegalArgumentException("Analytics#option#host must be a valid host, like 'https://api.segment.io'.");
		
		this.host = host;
		return this;
	}

	
	/**
	 * Sets the milliseconds to wait before a flush is marked as timed out.
	 * @param timeout timeout in milliseconds.
	 */
	public Config setTimeout(int timeout) {
		if (timeout < 1000)
			throw new IllegalArgumentException("Analytics#option#timeout must be at least 1000 milliseconds.");
		
		this.timeout = timeout;
		return this;
	}
	
	/**
	 * Sets the amount of request retries.
	 * @param retries number of times to retry the request
	 */
	public Config setRetries(int retries) {
		if (timeout < 0)
			throw new IllegalArgumentException("Analytics#option#retries must be greater or equal to 0.");
		
		this.retries = retries;
		return this;
	}

	/**
	 * Sets the milliseconds to wait between request retries
	 * @param timeout backoff in milliseconds.
	 */
	public Config setBackoff(int backoff) {
		if (timeout < 0)
			throw new IllegalArgumentException("Analytics#option#timeout must be greater or equal to 0 milliseconds.");
		
		this.backoff = backoff;
		return this;
	}
}
