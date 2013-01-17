package com.github.segmentio.safeclient.policy.queue;

public interface IQueueDenyPolicy {

	public boolean canQueue(int currentSize);
	
}
