package com.github.segmentio.models;


public class Track extends PropertyPayload {

	private String userId;
	private String event;
	
	public Track(String userId, 
				 String event, 
				 Props properties, 
				 Options options) {
		
		super("track", properties, options);

		if (properties == null) properties = new Props();
		
		this.userId = userId;
		this.event = event;
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
		
}
