package com.github.segmentio.models;

import java.util.List;

import org.joda.time.DateTime;

public class Batch {

	private String writeKey;
	private List<BasePayload> batch;
	private DateTime requestTimestamp;
	
	public Batch(String writeKey, List<BasePayload> batch) {
		this.writeKey = writeKey;
		this.batch = batch;
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
	
	public DateTime getRequestTimestamp() {
		return requestTimestamp;
	}
	
	public void setRequestTimestamp(DateTime requestTimestamp) {
		this.requestTimestamp = requestTimestamp;
	}
	
}
