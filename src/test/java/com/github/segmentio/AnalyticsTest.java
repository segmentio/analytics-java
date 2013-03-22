package com.github.segmentio;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.segmentio.Analytics;
import com.github.segmentio.models.EventProperties;
import com.github.segmentio.models.Traits;

public class AnalyticsTest {

	@BeforeClass
	public static void setup() {
		Analytics.initialize("testsecret", new Options().setFlushAt(1));
	}
	
	//
	// Identify
	//
	
	@Test
	public void identify() {
		// with an accented character
		Analytics.identify("ben@agorapulse.com", new Traits("lastName", "Hédiard"));
	}
	
	@Test
	public void identify2() {
		Analytics.identify("ilya@segment.io", new Traits("subscriptionPlan", "Free"));
	}
	

	@Test
	public void identify3() {
		Analytics.identify("ilya@segment.io", new Traits("subscriptionPlan", "Free"));
	}
	
	@Test
	public void identify4() {
		Analytics.identify("ilya@segment.io", new Traits("subscriptionPlan", "Free"));
	}

	//
	// Track
	//

	@Test
	public void track() {
		Analytics.track("ilya@segment.io", "Ran a marathan", new EventProperties("time", 1000*60*60*3));
	}
	

	@Test
	public void track2() {
		Analytics.track("ilya@segment.io", "Hates Java");
	}
	
	@Test
	public void track3() {
		Analytics.track("ilya@segment.io", "Purchased an Item", new EventProperties("revenue", 10.12));
	}
	

	@Test
	public void flush() {
		Analytics.flush();
	}
	
	@AfterClass
	public static void close() {
		Analytics.close();
	}
}
