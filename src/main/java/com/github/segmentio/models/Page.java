package com.github.segmentio.models;

import com.github.segmentio.Options;

public class Page extends BasePayload {

	private String userId;
	private String name;
	private String category;
	private EventProperties properties;
	
	public Page(String userId, 
				String name,
				String category,
				EventProperties properties, 
				Options options) {
		
		super("page", options);

		if (properties == null) properties = new EventProperties();
		
		this.userId = userId;
		this.name = name;
		this.category = category;
		this.properties = properties;
	}

	public String getUserId() {
		return userId;
	}
	
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getCategory() {
		return category;
	}
	
	public void setCategory(String category) {
		this.category = category;
	}
	
	public EventProperties getProperties() {
		return properties;
	}
	
	public void setProperties(EventProperties properties) {
		this.properties = properties;
	}
	
}
