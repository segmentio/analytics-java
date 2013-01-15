package com.segment.models;

import org.joda.time.DateTime;

/**
 * The base model for for Track / Identify payload
 *
 */
public class BasePayload {

	private String sessionId;
	private String userId;
	private Context context;
	private DateTime timestamp;
	
	public BasePayload(String sessionId, String userId, 
			DateTime timestamp, Context context) {
		
		this.sessionId = sessionId;
		this.userId = userId;
		this.timestamp = timestamp;
		this.context = context;
	}
	
	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public DateTime getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(DateTime timestamp) {
		this.timestamp = timestamp;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

}
