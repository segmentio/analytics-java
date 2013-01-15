package com.segment.safeclient.benchmark;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.StringUtils;

public class BenchmarkResult {

	private AtomicLong sum;
	private AtomicLong max;
	private AtomicLong min;
	private AtomicLong count;
	
	public BenchmarkResult() {
		 sum = new AtomicLong();
		 max = new AtomicLong();
		 min = new AtomicLong();
		 count = new AtomicLong();
	}
	
	public void update(long val) {
		
		if (val < min.get()) {
			min.set(val);
		}
		
		if (val > max.get()) {
			max.set(val);
		}
		
		sum.addAndGet(val);
		
		count.addAndGet(1);
	}
	
	public long getMax() {
		return max.get();
	}
	
	public long getMin() {
		return min.get();
	}
	
	public long getCount() {
		return count.get();
	}
	
	public double getAverage() {
		return sum.get() / count.get();
	}

	public String toCSVLine() {

		String line = StringUtils.join(new String[] {
				"" + min,
				"" + max,
				"" + getAverage()}, ",");
		
		return line;
	}
	
	
	
}
