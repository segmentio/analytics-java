package com.segment.safeclient.queue;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class LockingQueue<T> implements IBatchQueue<T> {

	private LinkedBlockingQueue<T> queue;
	
	public LockingQueue() {
		queue = new LinkedBlockingQueue<T>();
	}
	
	public int add(T item) {
		queue.add(item);
		return queue.size();
	}

	public int size() {
		return queue.size();
	}

	public List<T> flush(int maxAmount) {
		List<T> list = new LinkedList<T>();
		queue.drainTo(list, maxAmount);
		return list;
	}

	public void clear() {
		queue.clear();
	}

	
	
}
