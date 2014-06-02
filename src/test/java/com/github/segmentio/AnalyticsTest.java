package com.github.segmentio;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.segmentio.Analytics;
import com.github.segmentio.models.Props;
import com.github.segmentio.models.Traits;

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
		// with an accented character
		Analytics.identify("ilya@segment.io", new Traits("lastName", "Hédiard"));
		Analytics.flush();
	}
	
	@Test
	public void identify2() {
		Analytics.identify("ilya@segment.io", new Traits("subscriptionPlan", "Free"));
		Analytics.flush();
	}
	
	@Test
	public void identify3() {
		Analytics.identify("ilya@segment.io", new Traits("subscriptionPlan", "Free"));
		Analytics.flush();
	}
	
	@Test
	public void identify4() {
		Analytics.identify("ilya@segment.io", new Traits("subscriptionPlan", "Free"));
		Analytics.flush();
	}

	//
	// Track
	//

	@Test
	public void track() {
		Analytics.track("ilya@segment.io", "Ran a marathan", new Props("time", 1000*60*60*3));
		Analytics.flush();
	}
	

	@Test
	public void track2() {
		Analytics.track("ilya@segment.io", "Hates Java");
		Analytics.flush();
	}
	
	@Test
	public void track3() {
		Analytics.track("ilya@segment.io", "Purchased an Item", new Props("revenue", 10.12));
		Analytics.flush();
	}
	
	//
	// Alias
	//

	@Test
	public void alias() {
		Analytics.alias("from", "ilya@segment.io");
		Analytics.flush();
	}

	//
	// Flush
	//
	
	@Test
	public void flush() {
		Analytics.flush();
	}
	
	@AfterClass
	public static void close() {
		Analytics.close();
	}
}
