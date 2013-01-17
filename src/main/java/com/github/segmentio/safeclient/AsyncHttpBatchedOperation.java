package com.github.segmentio.safeclient;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Request;
import com.ning.http.client.Response;

public abstract class AsyncHttpBatchedOperation<M> 
	extends BatchedOperation<M> {

	private static final Logger logger = 
			LoggerFactory.getLogger(AsyncHttpBatchedOperation.class);
	
    private AsyncHttpClient asyncHttpClient;
    private int maximumOutstandingConnections;
    
    private AtomicInteger outstanding;
    
    public AsyncHttpBatchedOperation() {
    	
    	maximumOutstandingConnections = 100;
    	outstanding = new AtomicInteger(0);
    	
    	asyncHttpClient = new AsyncHttpClient(
	    	
    		new AsyncHttpClientConfig.Builder()
	    		.setMaxRequestRetry(1)
	    		.setIdleConnectionTimeoutInMs(30 * 1000)
	    		.setMaximumConnectionsTotal(maximumOutstandingConnections)
	    		.setAllowPoolingConnection(true)
	    		.build()
	    );

    	if (maximumOutstandingConnections < 1) 
    		throw new IllegalArgumentException("Outstanding connections must be greater than 0.");
    }
    
    public AsyncHttpBatchedOperation(AsyncHttpClient client) {
    	
    	this.asyncHttpClient = client;
    	
    	maximumOutstandingConnections = client.getConfig().getMaxTotalConnections();
    	outstanding = new AtomicInteger(0);
    	
    	if (maximumOutstandingConnections < 1) 
    		throw new IllegalArgumentException("Outstanding connections must be greater than 0.");
    }
	
	public abstract Request buildRequest(List<M> batch);
	
	@Override
	public boolean canFlush() {
		return outstanding.get() < maximumOutstandingConnections;
	}
	
	@Override
	public void performFlush(final List<M> batch) {
	
		Request request = buildRequest(batch);
		
		statistics.update("Request Body Size (bytes)", request.getContentLength());
		
		outstanding.incrementAndGet();
		
		try {
			
			final long start = System.currentTimeMillis(); 
			
			asyncHttpClient.executeRequest(request, new AsyncCompletionHandler<Response>() {

				@Override
				public Response onCompleted(Response response) throws Exception {
					
					outstanding.decrementAndGet();
					
					long duration = System.currentTimeMillis() - start;
					statistics.update("Request Duration (ms)", duration);
					
					int statusCode = response.getStatusCode(); 
					if (statusCode == 200) {
						statistics.update("Successful Requests", 1);
					} else {
						if (errorLoggingRateLimit.canPerform()) {
							logger.error("Response [code = " + statusCode + 
									"]. Response = " + response.getResponseBody());
						}
						statistics.update("Failed Requests", 1);
					}
					
					onFlush(batch, response);
					
					return response;
				}
				
			});
			
		} catch (IOException e) {
			
			if (errorLoggingRateLimit.canPerform()) {
				logger.error("Async HTTP flush failed.", e);
			}
			
			outstanding.decrementAndGet();
			
		}
	}
	
	/**
	 * Called when a flush occurs on a batch
	 * @param batch
	 * @param response
	 */
	public void onFlush(List<M> batch, Response response) {
		// do nothing
	}
	
}
