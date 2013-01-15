package com.segment.models;

import org.joda.time.DateTime;

public class Identify extends BasePayload {

	private Traits traits;
	
	public Identify(String sessionId, String userId, DateTime timestamp,
			Context context, Traits traits) {
		super(sessionId, userId, timestamp, context);
		
		this.traits = traits;
	}
	
	public Traits getTraits() {
		return traits;
	}
	
	public void setTraits(Traits traits) {
		this.traits = traits;
	}

}
