package com.github.segmentio;

import java.util.Iterator;

import org.joda.time.DateTime;

import com.github.segmentio.models.Alias;
import com.github.segmentio.models.BasePayload;
import com.github.segmentio.models.Context;
import com.github.segmentio.models.Props;
import com.github.segmentio.models.Identify;
import com.github.segmentio.models.Props;
import com.github.segmentio.models.Providers;
import com.github.segmentio.models.Track;
import com.github.segmentio.models.Traits;

public class PayloadGenerator implements Iterable<BasePayload> {

	public static BasePayload[] CASES = new BasePayload[] {
		
		new Identify("ilya@segment.io", 
				new Traits().put("name", "Achilles")
							.put("email", "achilles@segment.io")
							.put("subscriptionPlan", "Premium")
							.put("friendCount", 29)
							.put("company", new Props()
								.put("name", "Company, inc.")),
							new DateTime(),
							new Context()
								.put("ip", "192.168.1.1"), null),
							
		new Track("ilya@segment.io", "Played a Song", 
				new Props().put("name", "Achilles")
							.put("revenue", 39.95)
							.put("shippingMethod", "2-day"),
							new DateTime(), 
							new Context()
								.setIp("192.168.1.1")
								.setProviders(new Providers()
									.setDefault(true)
									.setEnabled("Mixpanel", false)
									.setEnabled("KISSMetrics", true)
									.setEnabled("Google Analytics", true)
								), null),
								
		new Alias("from", "ilya@segment.io", 
						new DateTime(), 
						new Context()
							.setProviders(new Providers()
								.setDefault(true)
								.setEnabled("Mixpanel", false)
								.setEnabled("KISSMetrics", true)
							), null),
		
	};
	
	public Iterator<BasePayload> iterator() {
		
		return new Iterator<BasePayload>() {

			public boolean hasNext() {
				return true;
			}

			public BasePayload next() {
				int index = (int)Math.floor(Math.random() * CASES.length);
				return CASES[index];
			}

			public void remove() {
				// do nothing
			}
			
		};
	}

}
