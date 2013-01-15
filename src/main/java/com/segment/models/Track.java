package com.segment.models;

import org.joda.time.DateTime;

public class Track extends BasePayload {

	private String event;
	private EventProperties properties;
	
	public Track(String sessionId, String userId, String event, DateTime timestamp,
			Context context, EventProperties properties) {
		super(sessionId, userId, timestamp, context);

		this.event = event;
		this.properties = properties;
	}

	public String getEvent() {
		return event;
	}
	
	public void setEvent(String event) {
		this.event = event;
	}
	
	public EventProperties getProperties() {
		return properties;
	}
	
	public void setProperties(EventProperties properties) {
		this.properties = properties;
	}
	
}
