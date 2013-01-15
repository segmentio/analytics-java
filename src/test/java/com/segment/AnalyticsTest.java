package com.segment;

import org.junit.BeforeClass;
import org.junit.Test;

import com.segment.models.EventProperties;
import com.segment.models.Traits;

public class AnalyticsTest {

	@BeforeClass
	public static void setup() {
		Analytics.initialize("testsecret");
	}
	
	//
	// Identify
	//
	
	@Test
	public void identify() {
		Analytics.identify("ilya@segment.io", "subscriptionPlan", "Free");
	}
	

	@Test
	public void identify2() {
		Analytics.identify("ilya@segment.io", new Traits("subscriptionPlan", "Free"));
	}
	
	@Test
	public void identify3() {
		Analytics.identify("session_id", "ilya@segment.io", new Traits("subscriptionPlan", "Free"));
	}

	//
	// Track
	//

	@Test
	public void track() {
		Analytics.track("ilya@segment.io", "Ran a marathan", "time", 1000*60*60*3);
	}
	

	@Test
	public void track2() {
		Analytics.track("ilya@segment.io", "Hates Java");
	}
	
	@Test
	public void track3() {
		Analytics.track("session_id", "ilya@segment.io", "Purchased an Item", new EventProperties("revenue", 10.12));
	}
}
