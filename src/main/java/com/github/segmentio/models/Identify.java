package com.github.segmentio.models;

import com.github.segmentio.Options;

public class Identify extends BasePayload {

	private String userId;
	private Traits traits;
	
	public Identify(String userId, Traits traits, Options options) {
		super("identify", options);
		
		this.userId = userId;
		this.traits = traits;
	}
	
	public String getUserId() {
		return userId;
	}
	
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public Traits getTraits() {
		return traits;
	}
	
	public void setTraits(Traits traits) {
		this.traits = traits;
	}

}
