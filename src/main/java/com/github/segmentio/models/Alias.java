package com.github.segmentio.models;


public class Alias extends BasePayload {

	private String userId;
	private String previousId;
	
	public Alias(String previousId, String userId, Options options) {
		super("alias", options);
		
		this.previousId = previousId;
		this.userId = userId;
	}
	
	public String getUserId() {
		return userId;
	}
	
	public String getPreviousId() {
		return previousId;
	}

}
