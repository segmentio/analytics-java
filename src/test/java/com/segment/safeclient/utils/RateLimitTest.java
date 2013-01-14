package com.segment.safeclient.utils;

import java.util.concurrent.atomic.AtomicLong;

import junit.framework.Assert;

import org.junit.Test;

import com.segment.safeclient.benchmark.BenchmarkResult;
import com.segment.safeclient.benchmark.ThreadedBenchmark;
import com.segment.safeclient.benchmark.ThreadedBenchmark.Operation;

public class RateLimitTest {

	@Test
	public void testRateLimit() {
		
		int every = 1000; // 1 second
		
		RateLimit limit = new RateLimit(1, every);
		
		long last = -1;
		
		for (int i = 0; i < every / 2 ; i += 1) {
			
			if (limit.canPerform()) {
				
				boolean firstTime = last == -1;
				
				long now = System.currentTimeMillis();
				
				if (!firstTime) {
					
					long lastPeriod = now - last;
					
					double percentError = Math.abs(((lastPeriod - every) / (double)every) * 100);
					
					System.out.println("Percent error : " + percentError + "%");
					
					Assert.assertTrue(percentError < 2);
				}
				
				last = now;
			}
			
			try {
				Thread.sleep(every / 100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	

	@Test
	public void rateLimitConcurrencyTest() {

		final int every = 1000;

		int[] threadCounts = {
				2,
				10,
				20
		};
			
		int index = -1;
		for (int threads : threadCounts) {
			
			index += 1;
			
			final RateLimit limit = new RateLimit(1, every);
			final AtomicLong atomicLast = new AtomicLong(-1);
			
			ThreadedBenchmark benchmark = 
					new ThreadedBenchmark(
							index,
							threads, 
							every,
							0,
							true,
							new Operation() {

							public void perform() {
								
								if (limit.canPerform()) {
									
									long last = atomicLast.get();
									
									boolean firstTime = (last == -1);
									
									long now = System.currentTimeMillis();
									
									if (!firstTime) {
										
										long lastPeriod = now - last;
										
										double percentError = Math.abs(((lastPeriod - every) / (double)every) * 100);
										
										System.out.println("Percent error : " + percentError + "%");
										
										Assert.assertTrue(percentError < 2);
									}
									
									atomicLast.set(now);
								}
								
								try {
									Thread.sleep(every / 100);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								
							}
				
			});
			
			BenchmarkResult result = benchmark.run();
			
			System.out.println(result.toCSVLine());
		}
	}
	
}
