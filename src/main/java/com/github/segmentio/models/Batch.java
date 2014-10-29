package com.github.segmentio.models;

import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;

public class Batch {

	private String writeKey;
	private List<BasePayload> batch;
	private String messageId;
	private DateTime sentAt;
	
	public Batch(String writeKey, List<BasePayload> batch) {
		this.writeKey = writeKey;
		this.batch = batch;
		this.messageId = UUID.randomUUID().toString();
	}
	
	public String getWriteKey() {
		return writeKey;
	}
	
	public void setWriteKey(String writeKey) {
		this.writeKey = writeKey;
	}
	
	public List<BasePayload> getBatch() {
		return batch;
	}
	
	public void setBatch(List<BasePayload> batch) {
		this.batch = batch;
	}
	
	public String getMessageId() {
		return messageId;
	}
	
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	
	public DateTime getSentAt() {
		return sentAt;
	}
	
	public void setSentAt(DateTime sentAt) {
		this.sentAt = sentAt;
	}
	
}
