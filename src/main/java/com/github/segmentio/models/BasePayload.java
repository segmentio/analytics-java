package com.github.segmentio.models;

import java.util.UUID;

import org.joda.time.DateTime;


/**
 * The base model for a Segment.io API payload
 */
public class BasePayload {

	private String type;
	private Context context;
	private String anonymousId;
	private DateTime timestamp;
	private String messageId;
	private Props integrations;
	
	public BasePayload(String type, Options options) {
		this.type = type;
		if (options == null) options = new Options();
		
		this.timestamp = options.getTimestamp();
		this.context = options.getContext();
		this.anonymousId = options.getAnonymousId();
		this.messageId = UUID.randomUUID().toString();
		this.integrations = options.getIntegrations();
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
	
	public Props getIntegrations () {
		return integrations;
	}
}
