package com.github.segmentio.safeclient.policy.flush;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class FlushAfterTimePolicy implements IFlushPolicy {

	private long thresholdMs;
	
	public FlushAfterTimePolicy(long thresholdMs) {
		this.thresholdMs = thresholdMs; 
	}
	
	public boolean shouldFlush(int queueSize, DateTime lastFlush) {
		if (lastFlush == null) return true;
		else {
			long since = new DateTime(DateTimeZone.UTC).getMillis() - lastFlush.getMillis();
			return since >= thresholdMs;
		}
	}
	
}
