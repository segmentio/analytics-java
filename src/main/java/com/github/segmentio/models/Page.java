package com.github.segmentio.models;


public class Page extends PropertyPayload {

	private String userId;
	private String name;
	private String category;
	
	public Page(String userId, 
				String name,
				String category,
				Props properties, 
				Options options) {
		
		super("page", properties, options);

		this.userId = userId;
		this.name = name;
		this.category = category;
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
	
}
