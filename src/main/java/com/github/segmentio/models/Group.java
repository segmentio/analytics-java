package com.github.segmentio.models;


public class Group extends BasePayload {

	private String userId;
	private String groupId;
	private Traits traits;
	
	public Group(String userId, String groupId, Traits traits, Options options) {
		super("group", options);
		
		if (traits == null) traits = new Traits();
		
		this.userId = userId;
		this.groupId = groupId;
		this.traits = traits;
	}
	
	public String getUserId() {
		return userId;
	}
	
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public String getGroupId() {
		return groupId;
	}
	
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	
	public Traits getTraits() {
		return traits;
	}
	
	public void setTraits(Traits traits) {
		this.traits = traits;
	}

}
