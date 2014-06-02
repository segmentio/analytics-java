package com.github.segmentio.models;

import java.util.UUID;

import org.joda.time.DateTime;

import com.github.segmentio.Options;

/**
 * The base model for a Segment.io API payload
 */
public class BasePayload {

	private String type;
	private Context context;
	private String anonymousId;
	private DateTime timestamp;
	private String messageId;
	
	public BasePayload(String type, Options options) {
		this.type = type;
		if (options == null) options = new Options();
		
		this.timestamp = options.getTimestamp();
		this.context = options.getContext();
		this.anonymousId = options.getAnonymousId();
		this.messageId = UUID.randomUUID().toString();
	}
	
	public String getType() {
		return type;
	}
	
	public String getAnonymousId() {
		return anonymousId;
	}
	
	public Context getContext() {
		return context;
	}
	
	public String getMessageId() {
		return messageId;
	}
	
	public DateTime getTimestamp() {
		return timestamp;
	}
}
