package com.github.segmentio.safeclient.flusher;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.segmentio.safeclient.BatchedOperation;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class ThreadPoolFlusher implements IFlusher {
	
	private static final Logger logger = LoggerFactory.getLogger(ThreadPoolFlusher .class);

	private ThreadPoolExecutor executor;
	private int maxRunnableQueueSize;
	
	public ThreadPoolFlusher(int coreThreads, int maxThreads, 
			int maxRunnableQueueSize) {
		
		this.maxRunnableQueueSize = maxRunnableQueueSize;
		
		executor = createNamedFixedBoundedThreadPool(
				coreThreads, 
				maxThreads, 
				maxRunnableQueueSize,
				"SafeClient - ThreadPoolFlusher");
		
	}

	public boolean canFlush() {
		// we don't want threads to race, so we go for a little bit less than the
		// maximum queue size
		return executor.getQueue().size() < Math.max(1, (maxRunnableQueueSize/2));
	}
	
	public <M> void flush(
			final BatchedOperation<M> operation, 
			final List<M> batch) {
		
		try {
			
			executor.execute(new Runnable() {
				public void run() {
					operation.performFlush(batch);
				};
			});
			
		} catch (RejectedExecutionException e) {
			
			// this is happening if we are flushing at a faster rate
			// than we can flush out. In this case, we choose to drop packets
			// rather than fill up a queue and hurt system resources
			
			logger.error(
					"Thread pool flusher can not keep up to the flush rate, dropping flush batch.", e);
		}
		
	}
	
	private static ThreadFactoryBuilder createThreadPoolBuilder(String threadPoolName)
	{
		ThreadFactoryBuilder builder = new ThreadFactoryBuilder();
		builder.setNameFormat(threadPoolName + "-%d" );
		builder.setUncaughtExceptionHandler(new UncaughtExceptionHandler()
		{
			public void uncaughtException(Thread t, Throwable e) {

				logger.error( 
						String.format("Uncaught thread pool exception in thread %s and exception %s.", 
								String.format("%s-%s", 
										t.getThreadGroup().getName(), 
										t.getName()),  
										e.getMessage()), e);
			}	
		});

		return builder;
	}
	
	
	public static ThreadPoolExecutor createNamedFixedBoundedThreadPool(
			int corePoolSize, 
			int maxPoolSize, 
			int maxRunnableQueueSize, 
			String threadPoolName)
	{
		
		ThreadFactoryBuilder builder = createThreadPoolBuilder(threadPoolName);
		
		ThreadPoolExecutor executor = new ThreadPoolExecutor(
				corePoolSize, 
				maxPoolSize, 
				5, 
				TimeUnit.SECONDS, 
				new LinkedBlockingQueue<Runnable>(maxRunnableQueueSize), 
				builder.build());
		
		return executor;
	}

	public void close() {
		if (executor != null) executor.shutdown();
	}
	
}
