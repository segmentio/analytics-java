package com.segment.safeclient.policy.flush;

import org.joda.time.DateTime;

public class GreaterThanFlushPolicy implements IFlushPolicy {

	private int threshold;
	
	public GreaterThanFlushPolicy(int threshold) {
		this.threshold = threshold;
	}
	
	public boolean shouldFlush(int queueSize, DateTime lastFlush) {
		return queueSize >= threshold;
	}
	
}
