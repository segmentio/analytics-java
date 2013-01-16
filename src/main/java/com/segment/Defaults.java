package com.segment;

import java.util.concurrent.TimeUnit;

import com.ning.http.client.AsyncHttpClientConfig;

public class Defaults {
	
	public static final String HOST = "https://api.segment.io";
	
	public static final int FLUSH_AT = 1;
	public static final int FLUSH_AFTER = (int) TimeUnit.SECONDS.toMillis(10);
	public static final int QUEUE_CAPACITY = 10000;
	
	public static final AsyncHttpClientConfig CONFIG = (new AsyncHttpClientConfig.Builder())
																		.setMaximumConnectionsTotal(100)
																		.build();
}
