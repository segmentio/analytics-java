package com.github.segmentio.models;


public class Track extends BasePayload {

	private String userId;
	private String event;
	private Props properties;
	
	public Track(String userId, 
				 String event, 
				 Props properties, 
				 Options options) {
		
		super("track", options);

		if (properties == null) properties = new Props();
		
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
	
	public Props getProperties() {
		return properties;
	}
	
	public void setProperties(Props properties) {
		this.properties = properties;
	}
	
}
