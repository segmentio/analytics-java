package com.segment.safeclient.benchmarks;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;

import com.segment.safeclient.benchmark.BenchmarkResult;
import com.segment.safeclient.benchmark.ThreadedBenchmark;
import com.segment.safeclient.benchmark.ThreadedBenchmark.Operation;
import com.segment.safeclient.queue.IBatchQueue;
import com.segment.safeclient.queue.LockingQueue;
import com.segment.safeclient.queue.NonLockingQueue;

public class BatchQueueBenchmark {

	public void lockingQueue() {
		
		runAddTest(new BatchQueueFactory() {
			
			public IBatchQueue<String> create() {
				return new LockingQueue<String>();
			}
		});
	}

	
	public void nonLockingQueue() {
		
		runAddTest(new BatchQueueFactory() {
			
			public IBatchQueue<String> create() {
				return new NonLockingQueue<String>();
			}
		});
	}
	
	interface BatchQueueFactory {
		IBatchQueue<String> create();
	}
	
	public void runAddTest(BatchQueueFactory batchQueueFactory) {
		
		try {
			
			String filename = batchQueueFactory.create().getClass().getSimpleName() + "Test.csv";
			
			File file = Paths.get(filename).toFile();
			if (file.exists()) file.delete();
			
			FileWriter writer = new FileWriter(filename);
			
			int valuesToAdd = 100000;
			
			int[] threadCounts = {
					2,
					10,
					20
			};
				 
			int index = -1;
			
			for (int threads : threadCounts) {
				
				index += 1;
				
				final IBatchQueue<String> queue = batchQueueFactory.create();
				
				ThreadedBenchmark benchmark = 
						new ThreadedBenchmark(
								index,
								threads, 
								valuesToAdd,
								0,
								true,
								new Operation() {
	
								public void perform() {
									int size = queue.add(RandomStringUtils.randomAlphanumeric(20));
									
									if (size % 50 == 0) {
										List<String> flushed = queue.flush(50);
										if (flushed != null && Math.random() < 0.0003) System.out.println("Flushed " + flushed.size() + " and have " + queue.size() + " left in batch queue.");
										
									}
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
				
				writer.write(line + "\n");
				
				Thread.sleep(2000);
			}
			
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
}
