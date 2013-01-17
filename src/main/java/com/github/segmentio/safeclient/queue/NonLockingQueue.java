
package com.github.segmentio.safeclient.queue;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class NonLockingQueue<T> implements IBatchQueue<T> {

	private ConcurrentLinkedQueue<T> queue;
	private AtomicBoolean lock;
	private AtomicInteger count;
	
	public NonLockingQueue() {
		queue = new ConcurrentLinkedQueue<T>();
		lock = new AtomicBoolean(false);
		count = new AtomicInteger(0);
	}
	
	public int add(T item) {
		
		int size = -1;
		
		if (queue.add(item)) {
			size = count.addAndGet(1);
		} else {
			size = count.get();
		}
		
		return size;
	}
	
	public int size() {
		return count.get();
	}
	
	public List<T> flush(int maxAmount) {
		
		List<T> list = null;
		
		if (lock.compareAndSet(false, true)) {
			
			int flushed = 0;
			
			list = new LinkedList<T>(); 
			T item = queue.poll();
			while (item != null) {
				list.add(item);
				flushed += 1;
				
				if (flushed <= maxAmount) {
					item = queue.poll();
				} else {
					break;
				}
			}
			
			// subtract the amount we just removed
			count.addAndGet(-list.size());
			
			lock.set(false);
		}
		
		return list;
	}

	public void clear() {
		queue.clear();
		count.set(0);
	}
	
}
