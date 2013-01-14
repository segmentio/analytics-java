package com.segment.safeclient.benchmark;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.segment.safeclient.flusher.ThreadPoolFlusher;

public class ThreadedBenchmark {

	private static final Logger logger = 
			LoggerFactory.getLogger(ThreadedBenchmark.class);
	
	public interface Operation {
		public void perform();
	}
	
	private int benchmarkIndex;
	
	private int threads;
	private int operations;
	
	private int sleepMs;
	
	private Operation operation;
	
	private boolean logProgress;
	
	public ThreadedBenchmark(int benchmarkIndex, int threads, int operations, 
			int sleepMs, boolean logProgress, Operation operation) {
		
		this.benchmarkIndex = benchmarkIndex;
		this.threads = threads;
		this.operations = operations;
		this.operation = operation;
		
		this.sleepMs = sleepMs;
		this.logProgress = logProgress;
	}
	
	public BenchmarkResult run() {
			
		BenchmarkResult result = new BenchmarkResult();
		
	    CountDownLatch latch = new CountDownLatch(operations);
		
		ThreadPoolExecutor executor = ThreadPoolFlusher.createNamedFixedBoundedThreadPool(
				threads,
				threads, 
				operations,
				"threaded-benchmark");
		
		for (int i = 0; i < operations; i += 1) {
			
			executor.execute(
					new Worker(
							operation, 
							latch, 
							result,
							benchmarkIndex,
							operations));
			
			try {
				Thread.sleep(sleepMs);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
			
		
		try {
			
			latch.await();
			
			executor.shutdownNow();
			
			System.gc();
			
			Thread.sleep(500);
			
		} catch (InterruptedException e) {
			
			logger.error("Interrupted while awaiting for the benchmark to finish.", e);
			
		}
		
		return result;
	}
	
	class Worker implements Runnable {
		
		private Operation operation;
		private CountDownLatch latch;
		private BenchmarkResult result;
		
		private int benchmarkIndex;
		private int totalOperations;
		
		Worker(Operation operation, 
				CountDownLatch latch, 
				BenchmarkResult result, 
				int benchmarkIndex, 
				int totalOperations) {
			
			this.operation = operation;
			this.latch = latch;
			this.result = result;
			
			this.benchmarkIndex = benchmarkIndex;
			this.totalOperations = totalOperations;
		}
		
		public void run() {
			
			long start = System.nanoTime();
			
			operation.perform();
			
			long duration = System.nanoTime() - start;
			
			result.update(duration);
			
			if (logProgress && Math.random() < 0.000003) {
				logger.info(
					String.format(
							"[Benchmark %d] - Finished operation %d / %d , or %d percent completed.",
							benchmarkIndex, 
							result.getCount(),
							totalOperations,
							(int)((result.getCount() / (double)totalOperations) * 100)
							));
			}
			
			latch.countDown();
		}
		
	}
	
}
