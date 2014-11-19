package com.github.segmentio.models;

import com.google.common.collect.ImmutableMap;

/**
 * A payload that has a list of properties
 * 
 * @author jpollak
 *
 */
public class PropertyPayload extends BasePayload {

	private ImmutableMap<String, Object> properties;
	
	public PropertyPayload(String type, 
			Props properties, 
			Options options) {
		super(type, options);
		
		if (properties == null) properties = new Props();

		this.properties = ImmutableMap.copyOf(properties);
	}
	
	public ImmutableMap<String, Object> getProperties() {
		return properties;
	}
	
	public void setProperties(Props properties) {
		this.properties = ImmutableMap.copyOf(properties);
	}

}
