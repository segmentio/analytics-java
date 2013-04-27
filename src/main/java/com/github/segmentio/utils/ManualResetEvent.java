package com.github.segmentio.utils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


// Thanks to Pelops for implmentation
// MIT License
// from: https://code.google.com/p/pelops/source/browse/trunk/src/org/wyki/concurrency/ManualResetEvent.java?r=2
public class ManualResetEvent {

	private volatile CountDownLatch event;
	private final Integer mutex;

	public ManualResetEvent(boolean signalled) {
		mutex = new Integer(-1);
		if (signalled) {
			event = new CountDownLatch(0);
		} else {
			event = new CountDownLatch(1);
		}
	}

	public void set() {
		event.countDown();
	}

	public void reset() {
		synchronized (mutex) {
			if (event.getCount() == 0) {
				event = new CountDownLatch(1);
			}
		}
	}

	public void waitOne() throws InterruptedException {
		event.await();
	}

	public boolean waitOne(int timeout, TimeUnit unit)
			throws InterruptedException {
		return event.await(timeout, unit);
	}

	public boolean isSignalled() {
		return event.getCount() == 0;
	}
}