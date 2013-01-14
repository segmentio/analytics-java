package com.segment.safeclient.policy.queue;

public class GreaterThanCapacityPolicy 
	implements IQueueDenyPolicy {

	private int threshold;
	
	public GreaterThanCapacityPolicy(int threshold) {
		this.threshold = threshold;
	}
	
	public boolean canQueue(int currentSize) {
		return currentSize <= threshold;
	}

}
