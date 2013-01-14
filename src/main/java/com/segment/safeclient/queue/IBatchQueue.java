package com.segment.safeclient.queue;

import java.util.List;

public interface IBatchQueue<T> {

	/**
	 * Adds an item to the batch queue.
	 * @param item The item to add to the batch queue.
	 * @return The current estimated size of the queue.
	 */
	public int add(T item);
	
	/**
	 * Retrieves the estimated size of the batch queue.
	 * @return the estimated size of the batch queue.
	 */
	public int size();
	
	
	/**
	 * Retrieves the entire contents of the batch queue
	 * if that operation is not already happening.
	 * 
	 * If it is, it will return null.

	 * @param maxAmount The maximum amount of items to flush
	 * @return A list of the current contents of the batch queue, or null if that operation is already happening. 
	 * 
	 */
	public List<T> flush(int maxAmount);
	
	/**
	 * Clears the queue.
	 */
	public void clear();
	
}
