package com.github.segmentio;

import java.io.IOException;

import org.junit.*;

import com.github.segmentio.models.Traits;
import com.github.segmentio.request.StubHttpServer;

public class AliasTest {

    static StubHttpServer server;
    
	@BeforeClass
	public static void setup() throws IOException {
	    
        server = new StubHttpServer();
        
        Options options = new Options();
        options.setHost("http://localhost:" + server.getServerPort());
        
		Analytics.initialize("testsecret", options);
		
	}
	
	@AfterClass
	public static void teardown() throws IOException {
	    server.stop();
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
