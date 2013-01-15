package com.segment.safeclient.policy.flush;

import org.joda.time.DateTime;

public class FlushAtSizePolicy implements IFlushPolicy {

	private int threshold;
	
	public FlushAtSizePolicy(int threshold) {
		this.threshold = threshold;
	}
	
	public boolean shouldFlush(int queueSize, DateTime lastFlush) {
		return queueSize >= threshold;
	}
	
}
