package com.segment.safeclient.benchmarks;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;

import com.segment.safeclient.benchmark.BenchmarkResult;
import com.segment.safeclient.benchmark.ThreadedBenchmark;
import com.segment.safeclient.benchmark.ThreadedBenchmark.Operation;

public class BasicQueueComparisonBenchmark {
	
	
	public void linkedBlockQueue() {
		
		runAddTest(new QueueFactory() {
			
			public Queue<String> create() {
				return new LinkedBlockingQueue<String>();
			}
		});
	}
	
	
	public void concurrentLinkedQueue() {
		
		runAddTest(new QueueFactory() {
			
			public Queue<String> create() {
				return new ConcurrentLinkedQueue<String>();
			}
		});
	}
	
	interface QueueFactory {
		Queue<String> create();
	}
	
	public void runAddTest(QueueFactory queueFactory) {
		
		int valuesToAdd = 100000;
		
		int[] threadCounts = {
				2,
				10,
				20
		};
			
		int index = -1;
		for (int threads : threadCounts) {
			
			index += 1;
			
			final Queue<String> queue = queueFactory.create();
			
			ThreadedBenchmark benchmark = 
					new ThreadedBenchmark(
							index,
							threads, 
							valuesToAdd,
							0,
							true,
							new Operation() {

							public void perform() {
								queue.add(RandomStringUtils.randomAlphanumeric(20));

								//queue.size();
							}
				
			});
			
			BenchmarkResult result = benchmark.run();
			
			queue.clear();
			
			String line = StringUtils.join(Arrays.asList(
					"" + (index+1),
					"" + result.getMin(),
					"" + result.getMax(),
					"" + result.getAverage()), ",");
			
			System.out.println(line);
		}
		
	}
	
}
