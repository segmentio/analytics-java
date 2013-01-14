package com.segment.safeclient.utils;

import junit.framework.Assert;

import org.junit.Test;

import com.segment.safeclient.benchmark.BenchmarkResult;
import com.segment.safeclient.benchmark.ThreadedBenchmark;
import com.segment.safeclient.benchmark.ThreadedBenchmark.Operation;

public class StatisticTest {

	@Test
	public void statisticConcurrencyTest() {
	
		int operations = 1000000; 
			
		int[] threadCounts = {
				2,
				10,
				20
		};
		
		int index = 0;
		for (int threads : threadCounts) {
			
			index += 1;
			
			final Statistic statistic = new Statistic();
			
			ThreadedBenchmark benchmark = 
					new ThreadedBenchmark(
							index,
							threads,
							operations,
							0,
							true,
							new Operation() {

							public void perform() {
								
								statistic.update(Math.random());
								
							}
				
			});
			
			
			BenchmarkResult result = benchmark.run();

			Assert.assertEquals(operations, statistic.getCount());
			
			System.out.println("Average : " + statistic.getAverage());
			System.out.println("Std Dev : " + statistic.getStandardDeviation());
			
			System.out.println("Threads " + (index+1) + "," + result.toCSVLine());
		}
	}
	
}
