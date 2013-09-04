package com.github.segmentio;

import java.util.Iterator;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.segmentio.models.Alias;
import com.github.segmentio.models.BasePayload;
import com.github.segmentio.models.Identify;
import com.github.segmentio.models.Track;

public class BlockingFlushTest {

	private static AnalyticsClient client;
	
	@BeforeClass
	public static void setup() {
		client = new AnalyticsClient("testsecret");
	}

	@Test
	public void testSending() {
		
		int trials = (int)(Math.random() * 500);
		
		Iterator<BasePayload> iterator = new PayloadGenerator().iterator();
		
		System.out.println("Running " + trials +  " tests");
		
		for (int i = 0; i < trials; i += 1) {
			BasePayload payload = iterator.next();
			
			if (payload instanceof Identify) {
				Identify identify = (Identify) payload;
				
				client.identify(identify.getUserId(),
						 		identify.getTraits(),
						 		identify.getTimestamp(),
						 		identify.getContext(),
						 		null);
				
			} else if (payload instanceof Track) {
				Track track = (Track) payload;
				
				client.track(track.getUserId(),
							 track.getEvent(),
							 track.getProperties(),
							 track.getTimestamp(),
							 track.getContext(),
							 null);
				
			} else if (payload instanceof Alias) {
				Alias alias = (Alias) payload;
				
				client.alias(alias.getFrom(),
							 alias.getTo(),
							 alias.getTimestamp(),
							 alias.getContext(),
							 null);
			} else {
				System.err.println(payload.getClass() + " is unexpected.");
			}
		}
		
		// do a blocking flush
		client.flush();
		
		Assert.assertEquals(trials, client.getStatistics().getSuccessful().getCount());
	}
	
	@AfterClass
	public static void close() {
		client.close();
	}
	
}
