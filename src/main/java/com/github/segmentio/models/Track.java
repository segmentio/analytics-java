package com.github.segmentio.models;

import com.github.segmentio.Options;

public class Track extends BasePayload {

	private String userId;
	private String event;
	private EventProperties properties;
	
	public Track(String userId, 
				 String event, 
				 EventProperties properties, 
				 Options options) {
		
		super("track", options);

		if (properties == null) properties = new EventProperties();
		
		this.userId = userId;
		this.event = event;
		this.properties = properties;
	}

	public String getUserId() {
		return userId;
	}
	
	public void setUserId(String userId) {
		this.userId = userId;
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
