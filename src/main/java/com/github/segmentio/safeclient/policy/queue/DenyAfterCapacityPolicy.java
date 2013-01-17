package com.github.segmentio.safeclient.policy.queue;

public class DenyAfterCapacityPolicy 
	implements IQueueDenyPolicy {

	private int threshold;
	
	public DenyAfterCapacityPolicy(int threshold) {
		this.threshold = threshold;
	}
	
	public boolean canQueue(int currentSize) {
		return currentSize <= threshold;
	}

}
