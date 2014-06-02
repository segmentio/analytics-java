package com.github.segmentio.test;

import java.util.Random;

import org.joda.time.DateTime;

import com.github.segmentio.AnalyticsClient;
import com.github.segmentio.Options;
import com.github.segmentio.models.Context;
import com.github.segmentio.models.Props;
import com.github.segmentio.models.Traits;

public class Actions {

	private static Random random = new Random();
	
	public static Props properties () {
		return new Props (
			"Success", true, 
			"When", DateTime.now()
		);
	}

	public static Traits traits () {
		return new Traits(
			"Subscription Plan", "Free",
			"Friends", 30,
			"Joined", DateTime.now(),
			"Cool", true,
			"Company", new Props("name", "Initech, Inc "),
			"Revenue", 40.32,
			"Don't Submit This, Kids", new Exception ()
		);
	}

	public static Options options () {
		return new Options() 
			.setTimestamp(DateTime.now())
			.setIntegration ("all", false)
			.setIntegration ("Mixpanel", true)
			.setIntegration ("Salesforce", true)
			.setContext (new Context(
				"ip", "12.212.12.49",
				"language", "en-us"
			)
		);
	}

	public static void identify(AnalyticsClient client) {
		client.identify("user", traits(), options());
	}

	public static void group(AnalyticsClient client) {
		client.group("user", "group", traits(), options());
	}

	public static void track(AnalyticsClient client) {
		client.track("user", "Ran Java test", properties(), options());
	}

	public static void alias(AnalyticsClient client) {
		client.alias("previousId", "to");
	}

	public static void page(AnalyticsClient client) {
		client.page("user", "name", "category", properties(), options());
	}

	public static void screen(AnalyticsClient client) {
		client.screen("user", "name", "category", properties(), options());
	}

	public static void random(AnalyticsClient client)
	{
		switch (random.nextInt(6)) {
			case 0:
				identify(client);
				return;
			case 1:
				track(client);
				return;
			case 2:
				alias(client);
				return;
			case 3:
				group(client);
				return;
			case 4:
				page(client);
				return;
			default:
				screen(client);
				return;
		}
	}
	
}
