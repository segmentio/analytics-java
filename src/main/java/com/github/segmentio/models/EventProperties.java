package com.github.segmentio.models;

public class EventProperties extends Props {

	private static final long serialVersionUID = -946938748259365871L;

	public EventProperties(Object... kvs) {
		super(kvs);
	}
	
	@Override
	public EventProperties put(String key, Object value) {
		super.put(key, value);
		return this;
	}
}
