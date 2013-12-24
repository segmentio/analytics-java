package com.github.segmentio.request;

import java.util.concurrent.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.segmentio.AnalyticsClient;
import com.github.segmentio.Constants;
import com.github.segmentio.models.Batch;
import com.github.segmentio.stats.AnalyticsStatistics;

public class AsyncRequester implements IRequester {

	private static final Logger logger = LoggerFactory
			.getLogger(Constants.LOGGER);

    private static final int NTHREADS = 3;

    private ExecutorService executor;
    
    private BlockingRequester blockingRequester;
    
	public AsyncRequester(AnalyticsClient client) {

	    executor = Executors.newFixedThreadPool(NTHREADS);
	    
	    blockingRequester = new BlockingRequester(client);
		
	}

	private class HttpRequestCallable implements Callable<Void> {

	    private Batch batch;
	    
	    HttpRequestCallable(Batch batch) {
	        this.batch = batch;
	    }
	    
        public Void call() throws Exception {
            
            blockingRequester.send(batch);
            
            return null;
        }
	    
	}
	
	public void send(Batch batch) {
	    HttpRequestCallable callable = new HttpRequestCallable(batch);
	    Future<Void> submit = executor.submit(callable);
	    
	}
	
	public void close() {
	    executor.shutdown();
        blockingRequester.close();
	}

}
