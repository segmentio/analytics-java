package com.github.segmentio.models;

import java.util.List;

public class Batch {

	private String secret;
	private List<BasePayload> batch;
	
	public Batch(String secret, List<BasePayload> batch) {
		this.secret = secret;
		this.batch = batch;
	}
	
	public String getSecret() {
		return secret;
	}
	
	public void setSecret(String secret) {
		this.secret = secret;
	}
	
	public List<BasePayload> getBatch() {
		return batch;
	}
	
	public void setBatch(List<BasePayload> batch) {
		this.batch = batch;
	}
	
}
