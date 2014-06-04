package com.github.segmentio.models;


public class Page extends BasePayload {

	private String userId;
	private String name;
	private String category;
	private Props properties;
	
	public Page(String userId, 
				String name,
				String category,
				Props properties, 
				Options options) {
		
		super("page", options);

		if (properties == null) properties = new Props();
		
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
	
	public Props getProperties() {
		return properties;
	}
	
	public void setProperties(Props properties) {
		this.properties = properties;
	}
	
}
