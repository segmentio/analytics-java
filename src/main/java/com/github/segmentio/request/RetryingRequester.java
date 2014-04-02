package com.github.segmentio.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.segmentio.AnalyticsClient;
import com.github.segmentio.Constants;
import com.github.segmentio.models.Batch;

public class RetryingRequester extends BlockingRequester {

	private static final Logger logger = LoggerFactory
			.getLogger(Constants.LOGGER);
	
	private int retries;
	private int backoff;
	
	public RetryingRequester(AnalyticsClient client) {
		super(client);
		retries = client.getOptions().getRetries();
		backoff = client.getOptions().getBackoff();
	}
	
	@Override
	public boolean send(Batch batch) {
		int attempts = 0;
		boolean success = super.send(batch);
		while (!success && attempts < retries) {
			attempts += 1;
			try {
				Thread.sleep(backoff);
			} catch (InterruptedException e) {
				logger.warn("Interrupted during backoff", e);
			}
			logger.info("Retrying request [attempt " + attempts + "] ..");
			success = super.send(batch);
		}
		return success;
	}
	
}
