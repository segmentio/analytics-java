package com.github.segmentio.safeclient.policy.flush;

import org.joda.time.DateTime;

public interface IFlushPolicy {

	public boolean shouldFlush (int queueSize, DateTime lastFlush);
	
}
