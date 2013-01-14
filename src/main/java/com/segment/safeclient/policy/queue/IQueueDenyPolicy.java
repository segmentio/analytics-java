package com.segment.safeclient.policy.queue;

public interface IQueueDenyPolicy {

	public boolean canQueue(int currentSize);
	
}
