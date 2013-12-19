package com.github.segmentio;

import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.segmentio.models.Alias;
import com.github.segmentio.models.BasePayload;
import com.github.segmentio.models.Callback;
import com.github.segmentio.models.Identify;
import com.github.segmentio.models.Track;

public class ClientTest {
	
	private static AnalyticsClient client;
	
	@BeforeClass
	public static void setup() {
		client = new AnalyticsClient("testsecret");
	}

	@Test
	public void testSending() {
		
		int trials = (int)(Math.random() * 100);
		
		final CountDownLatch latch = new CountDownLatch(trials);
		
		Iterator<BasePayload> iterator = new PayloadGenerator().iterator();
		
		System.out.println("Running " + trials +  " tests");
		
		for (int i = 0; i < trials; i += 1) {
			BasePayload payload = iterator.next();
			
			Callback callback = new Callback() {
				public void onResponse(boolean success, String message) {
					Assert.assertEquals(true, success);
					latch.countDown();
				}
			};
			
			if (payload instanceof Identify) {
				Identify identify = (Identify) payload;
				
				client.identify(identify.getUserId(),
						 		identify.getTraits(),
						 		identify.getTimestamp(),
						 		identify.getContext(),
						 		callback);
				
			} else if (payload instanceof Track) {
				Track track = (Track) payload;
				
				client.track(track.getUserId(),
							 track.getEvent(),
							 track.getProperties(),
							 track.getTimestamp(),
							 track.getContext(),
							 callback);
				
			} else if (payload instanceof Alias) {
				Alias alias = (Alias) payload;
				
				client.alias(alias.getFrom(),
							 alias.getTo(),
							 alias.getTimestamp(),
							 alias.getContext(),
							 callback);
			} else {
				System.err.println(payload.getClass() + " is unexpected.");
			}
		}
		
		client.flush();
		
		try {
			Assert.assertTrue(latch.await(40, TimeUnit.SECONDS));
		} catch (InterruptedException e) {
			Assert.fail();
		}
	}
	
	@AfterClass
	public static void close() {
		client.close();
	}
	
}
