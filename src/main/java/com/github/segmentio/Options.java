package com.github.segmentio;

import org.apache.commons.lang.StringUtils;


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
	 * Stop accepting messages after the queue reaches this capacity
	 */
	private int maxQueueSize;

	/**
	 * The amount of milliseconds that passes before a request is marked as timed out
	 */
	private int timeout;
	
	private RequesterType requesterType;

	/**
	 * Creates a default options
	 */
	public Options() {
		this(Defaults.HOST, Defaults.MAX_QUEUE_SIZE, Defaults.TIMEOUT, Defaults.REQUESTER_TYPE);
	}

	/**
	 * Creates an option with the provided settings
	 * 
	 * @param flushAt
	 * @param flushAfter
	 * @param maxQueueSize
	 * @param httpConfig
	 */
	Options(String host, int maxQueueSize, int timeout, RequesterType requesterType) {
		setHost(host);
		setMaxQueueSize(maxQueueSize);
		setTimeout(timeout);
		setRequesterType(requesterType);
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
	
	public RequesterType getRequesterType() {
	    return requesterType;
	}
	
	/**
	 * Sets the maximum queue capacity, which is an emergency pressure relief
	 * valve. If we're unable to flush messages fast enough, the queue will stop
	 * accepting messages after this capacity is reached.
	 * 
	 * @param maxQueueSize
	 */
	public Options setMaxQueueSize(int maxQueueSize) {
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
	public Options setHost(String host) {
		if (StringUtils.isEmpty(host))
			throw new IllegalArgumentException("Analytics#option#host must be a valid host, like 'https://api.segment.io'.");
		
		this.host = host;
		return this;
	}

	
	/**
	 * Sets the milliseconds to wait before a flush is marked as timed out.
	 * @param timeout timeout in milliseconds.
	 */
	public Options setTimeout(int timeout) {
		if (timeout < 1000)
			throw new IllegalArgumentException("Analytics#option#timeout must be at least 1000 milliseconds.");
		
		this.timeout = timeout;
		return this;
	}

	public Options setRequesterType(RequesterType requesterType) {
	    this.requesterType = requesterType;
	    return this;
	}
}
