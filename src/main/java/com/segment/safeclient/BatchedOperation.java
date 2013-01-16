package com.segment.safeclient;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.segment.safeclient.flusher.IFlusher;
import com.segment.safeclient.flusher.ThreadPoolFlusher;
import com.segment.safeclient.policy.flush.FlushAtSizePolicy;
import com.segment.safeclient.policy.flush.IFlushPolicy;
import com.segment.safeclient.policy.flush.FlushAfterTimePolicy;
import com.segment.safeclient.policy.queue.DenyAfterCapacityPolicy;
import com.segment.safeclient.policy.queue.IQueueDenyPolicy;
import com.segment.safeclient.queue.IBatchQueue;
import com.segment.safeclient.queue.NonLockingQueue;
import com.segment.safeclient.utils.RateLimit;
import com.segment.safeclient.utils.Statistics;

public abstract class BatchedOperation<M> {
	
	private static final Logger logger = 
			LoggerFactory.getLogger(BatchedOperation.class);
	
	protected Iterable<IFlushPolicy> flushPolicies = createFlushPolicies();
	protected Iterable<IQueueDenyPolicy> denyPolicies = createCapacityPolicies();
	
	private IFlusher flusher = createFlusher();
	
	private IBatchQueue<M> queue = createQueue();
	
	protected RateLimit errorLoggingRateLimit = new RateLimit(1, 1000);
	protected RateLimit statisticsLoggingRateLimit = new RateLimit(1, 5000);
	
	private DateTime lastFlush;
	
	public Statistics statistics = new Statistics();
	
	public abstract boolean canFlush();
	
	/**
	 * Called when a flush needs to happen.
	 * @param batch The batch to flush.
	 * @return 
	 */
	public abstract void performFlush(List<M> batch);

	public boolean perform(M message) {
		
		boolean canEnqueue = true;
		
		int currentSize = queue.size();
		
		for (IQueueDenyPolicy denyPolicy : denyPolicies) {
			
			if (!denyPolicy.canQueue(currentSize)) {
				
				canEnqueue = false;
				
				statistics.update("Queue over Capacity => Denied Message", 1);
				
				break;
			}
		}
		
		if (canEnqueue) { 
			
			currentSize = queue.add(message);

			statistics.update("Enqueued Message", 1);
			
		} else {
			
			if (errorLoggingRateLimit.canPerform()) {
				
				logger.warn("Operation batch queue is full, and flushing operations are also " + 
						"pending. Choosing to drop this message from the queue.");
			}
		}
		
		if (canFlush()) {
		
			for (IFlushPolicy flushPolicy : flushPolicies) {
				
				if (flushPolicy.shouldFlush(currentSize, lastFlush)) {
					
					statistics.update("Asking to Flush", 1);
					
					flush();
					
					break;
				}
				
			}
			
		} else {
			
			if (errorLoggingRateLimit.canPerform()) {
				logger.warn("Batched operation can't flush.");
			}
			
			statistics.update("Batched Operation Can't Flush", 1);
		}
		
		statistics.update("Queue Size", queue.size());
		
		// should we log the statistics?
		if (shouldLogStatistics() && statisticsLoggingRateLimit.canPerform()) {
			logger.debug(statistics.toString());
		}
		
		return canEnqueue;	
	}
	
	public boolean flush() {
		
		if (flusher.canFlush()) {
			
			int maxAmount = getMaxFlushAmount();
			
			List<M> batch = queue.flush(maxAmount);
			
			if (batch != null) {
				
				flusher.flush(this, batch);
				
				statistics.update("Flushes", 1);
				
				statistics.update("Flush Batch Size", batch.size());
				
				lastFlush = new DateTime(DateTimeZone.UTC);
				
				return true;
			}
			
		} else {
			
			statistics.update("Flusher Can't Flush", 1);
		}
		
		return false;
	}
	
	public int getQueueSize() {
		return queue.size();
	}
	
	public boolean shouldLogStatistics() {
		return true;
	}
	
	protected int getMaxFlushAmount() {
		return 50;
	}
	
	protected int getMaxQueueSize() {
		return getMaxFlushAmount() * 20;
	}
	
	protected Iterable<IFlushPolicy> createFlushPolicies() {
		
		return Arrays.asList(		
			new FlushAfterTimePolicy(1000 * 10),
			new FlushAtSizePolicy(getMaxFlushAmount())
		);
		
	}
	
	protected Iterable<IQueueDenyPolicy> createCapacityPolicies() {
		
		List<IQueueDenyPolicy> policies = 
				new LinkedList<IQueueDenyPolicy>();
		
		policies.add(new DenyAfterCapacityPolicy(getMaxQueueSize()));
		
		return policies;
	}
	
	protected IFlusher createFlusher() {
		return new ThreadPoolFlusher(0, 1, 1000);
	}
	
	protected IBatchQueue<M> createQueue() {
		return new NonLockingQueue<M>();
	}
	
	public void clear() {
		if (queue != null) queue.clear();
	}
	
	public void close() {
		if (flusher != null) flusher.close();
		if (queue != null) queue.clear();
	}
	
	
}
