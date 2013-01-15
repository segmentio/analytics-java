package com.segment.models;

import org.joda.time.DateTime;

import com.ning.http.client.Response;

/**
 * The base model for for Track / Identify payload
 *
 */
public class BasePayload {

	private String sessionId;
	private String userId;
	private Context context;
	private DateTime timestamp;
	
	private transient Callback callback;
	
	public BasePayload(String sessionId, String userId, 
			DateTime timestamp, Context context, Callback callback) {
		
		this.sessionId = sessionId;
		this.userId = userId;
		this.timestamp = timestamp;
		this.context = context;
		
		this.callback = callback;
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
	
	public Callback getCallback() {
		return callback;
	}
	
	public void setCallback(Callback callback) {
		this.callback = callback;
	}

}
