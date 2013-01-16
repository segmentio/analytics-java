package com.segment;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ning.http.client.Response;
import com.segment.models.BasePayload;
import com.segment.models.Callback;
import com.segment.models.Context;
import com.segment.models.EventProperties;
import com.segment.models.Identify;
import com.segment.models.Track;
import com.segment.models.Traits;

public class ClientTest {

	public static BasePayload[] CASES = new BasePayload[] {
		
		new Identify("DKGXt384hFDT82D", "019mr8mf4r", new DateTime(),
				new Context().setIp("192.168.1.1"), 
				new Traits().put("name", "Achilles")
							.put("email", "achilles@segment.io")
							.put("subscriptionPlan", "Premium")
							.put("friendCount", 29), null),
							
		new Track("DKGXt384hFDT82D", "019mr8mf4r", "Played a Song", new DateTime(),
				new Context().setIp("192.168.1.1"), 
				new EventProperties().put("name", "Achilles")
							.put("revenue", 39.95)
							.put("shippingMethod", "2-day"), null),
		
	};
	
	private static Client client;
	
	@BeforeClass
	public static void setup() {
		client = new Client("testsecret", new Options().setFlushAt(50));
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
					
					client.identify(identify.getSessionId(), 
								 identify.getUserId(),
								 identify.getContext(),
								 identify.getTimestamp(),
								 identify.getTraits(),
								 callback);
					
				} else if (payload instanceof Track) {
					Track track = (Track) payload;
					
					client.track(track.getSessionId(), 
								 track.getUserId(),
								 track.getEvent(),
								 track.getContext(),
								 track.getTimestamp(),
								 track.getProperties(),
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
