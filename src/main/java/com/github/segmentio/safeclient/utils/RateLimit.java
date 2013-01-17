package com.github.segmentio.safeclient.utils;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.util.concurrent.AtomicDouble;

public class RateLimit {

	private int allowed;
	private int perMilliseconds;
	
	// Based on a thread-safe version of: 
	// http://stackoverflow.com/questions/667508/whats-a-good-rate-limiting-algorithm
	private AtomicDouble allowance;
	private AtomicLong lastCheck;
	
	private AtomicBoolean lock;
	
	public RateLimit(int allowed, int perMilliseconds) {
		
		this.allowed = allowed;
		this.perMilliseconds = perMilliseconds;
		
		allowance = new AtomicDouble(allowed);
		lastCheck = new AtomicLong(System.currentTimeMillis());
		
		lock = new AtomicBoolean(false);
	}
	
	/**
	 * Gets the rate in allowed per millisecond
	 * @return
	 */
	public double getRate() {
		return allowed / (double) perMilliseconds;
	}
	
	// TODO: do some more thinking about whether this will allow during
	// multiple people running at the same time
	
	public boolean canPerform() {
		
		boolean canPerform = false;
		
		if (lock.compareAndSet(false, true)) {
		
			long current = System.currentTimeMillis();
			
			long timePassed = current - lastCheck.get();
			
			lastCheck.set(current);
			
			double delta = timePassed * (allowed / (double)perMilliseconds);
			
			double allowanceVal = allowance.addAndGet(delta);
			
			if (allowanceVal > allowed) {
				allowance.set(allowed);
			} 
	
			if (allowanceVal < 1.0) {
				
				canPerform = false;
				
			} else {
				
				allowance.addAndGet(-1);
				
				canPerform = true;
			}
			
			lock.set(false);
		
		}
		
		return canPerform;
	}
	
}
