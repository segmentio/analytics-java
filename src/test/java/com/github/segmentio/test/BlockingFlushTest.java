package com.github.segmentio.test;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.segmentio.AnalyticsClient;

public class BlockingFlushTest {

	private AnalyticsClient client;
	
	@Before
	public void setup() {
		client = new AnalyticsClient(Constants.WRITE_KEY);
	}

	@Test
	public void testBlockingFlush() {
		int trials = 50; 
		for (int i = 0; i < trials; i += 1) Actions.random(client);
		client.flush();
		Assert.assertEquals(trials, client.getStatistics().getInserted().getCount());
		Assert.assertEquals(trials, client.getStatistics().getSuccessful().getCount());
	}
		
	@After
	public void close() {
		client.close();
	}
}
