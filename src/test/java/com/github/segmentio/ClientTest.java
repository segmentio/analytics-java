package com.github.segmentio;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.segmentio.Client;
import com.github.segmentio.Options;
import com.github.segmentio.models.BasePayload;
import com.github.segmentio.models.Callback;
import com.github.segmentio.models.Context;
import com.github.segmentio.models.EventProperties;
import com.github.segmentio.models.Identify;
import com.github.segmentio.models.Track;
import com.github.segmentio.models.Traits;
import com.ning.http.client.Response;

public class ClientTest {

	public static BasePayload[] CASES = new BasePayload[] {
		
		new Identify("ilya@segment.io", 
				new Traits().put("name", "Achilles")
							.put("email", "achilles@segment.io")
							.put("subscriptionPlan", "Premium")
							.put("friendCount", 29), 
							new DateTime(),
							new Context().setIp("192.168.1.1"), null),
							
		new Track("ilya@segment.io", "Played a Song", 
				new EventProperties().put("name", "Achilles")
							.put("revenue", 39.95)
							.put("shippingMethod", "2-day"), 
							new DateTime(), 
							new Context().setIp("192.168.1.1"), null),
		
	};
	
	private static Client client;
	
	@BeforeClass
	public static void setup() {
		client = new Client("testsecret", new Options()
												.setFlushAt(50)
												.setFlushAfter((int)TimeUnit.SECONDS.toMillis(10))
												.setMaxQueueSize(10000));
	}

	@Test
	public void testSending() {
		
		int trials = (int)(Math.random() * 100);
		final int total = trials * CASES.length;
		//final AtomicInteger processed = new AtomicInteger();
		
		final CountDownLatch latch = new CountDownLatch(trials * CASES.length);
		
		System.out.println("Running " + total +  " tests");
		
		for (int i = 0; i < trials; i += 1) {
			for (BasePayload payload : CASES) {
				Callback callback = new Callback() {
					public void onResponse(Response response) {
						//int done = processed.addAndGet(1);
						//System.out.println(done + " / " + total);
						Assert.assertEquals(200, response.getStatusCode());
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
				}
			}
		}
		
		client.flush();
		
		try {
			Assert.assertTrue(latch.await(30, TimeUnit.SECONDS));
		} catch (InterruptedException e) {
			Assert.fail();
		}
	}
	
	@AfterClass
	public static void close() {
		client.close();
	}
	
}
