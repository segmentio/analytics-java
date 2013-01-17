package com.github.segmentio.models;

import org.joda.time.DateTime;

public class Identify extends BasePayload {

	@SuppressWarnings("unused")
	private String action = "identify";
	
	private Traits traits;
	
	public Identify(String userId, Traits traits, DateTime timestamp,
			Context context, Callback callback) {
		
		super(userId, timestamp, context, callback);
		
		this.traits = traits;
	}
	
	public Traits getTraits() {
		return traits;
	}
	
	public void setTraits(Traits traits) {
		this.traits = traits;
	}

}
