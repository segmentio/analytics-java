package com.github.segmentio.models;

import org.joda.time.DateTime;

public class Track extends BasePayload {

	@SuppressWarnings("unused")
	private String action = "track";
	
	private String event;
	private EventProperties properties;
	
	public Track(String userId, 
				 String event, 
				 EventProperties properties, 
				 DateTime timestamp,
				 Context context, 
				 Callback callback) {
		
		super(userId, timestamp, context, callback);

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
