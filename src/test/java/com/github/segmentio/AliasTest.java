package com.github.segmentio;

import java.io.IOException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.segmentio.models.Traits;

public class AliasTest {

	@BeforeClass
	public static void setup() throws IOException {
		Analytics.initialize("testsecret");
	}
		
	@Test
	public void testAlias() throws InterruptedException {
		int random = (int)Math.floor((Math.random() * 99999) + 50);
		
		String anonymous = "anonymous_user" + random;
		String identified = "identified" + random + "@gmail.com";

		System.out.println(String.format("Test: %s => %s", anonymous, identified));
		
		// the anonymous user does actions ...
		Analytics.track(anonymous, "Anonymous Event");
		// the anonymous user signs up and is aliased
		Analytics.alias(anonymous, identified);
		// the identified user is identified
		Analytics.identify(identified, new Traits("plan", "Free"));
		// the identified user does actions ...
		Analytics.track(identified, "Identified Action");
		
		Analytics.flush();
		
		Assert.assertEquals(4, Analytics.getStatistics().getSuccessful().getCount());
	}
}
