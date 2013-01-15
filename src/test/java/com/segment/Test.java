package com.segment;

import org.joda.time.DateTime;

import com.segment.models.BasePayload;
import com.segment.models.Context;
import com.segment.models.EventProperties;
import com.segment.models.Identify;
import com.segment.models.Track;
import com.segment.models.Traits;

public class Test {

	static BasePayload[] CASES = new BasePayload[] {
		
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
	
}
