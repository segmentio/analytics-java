package com.segment.models;

public class Traits extends SafeProperties {

	private static final long serialVersionUID = 5446264732089518289L;

	public Traits() {
		super();
	}
	
	public Traits(Object... kvs) {
		super(kvs);
	}
	
	@Override
	public Traits put(String key, Object value) {
		super.put(key, value);
		return this;
	}
}
