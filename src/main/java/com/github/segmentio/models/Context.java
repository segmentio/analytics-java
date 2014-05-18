package com.github.segmentio.models;

import com.github.segmentio.Analytics;


public class Context extends Props {

	private static final long serialVersionUID = 2042634726363431396L;
	
	public Context() {
		super();
		addDefaults();
	}
	
	public Context(Object... kvs) {
		super(kvs);
		addDefaults();
	}
	
	private void addDefaults() {
		this.put("library", new Props()
			.put("name", "analytics-java")
			.put("version", Analytics.VERSION));
	}
	
	@Override
	public Props put(String key, Object value) {
		super.put(key, value);
		return this;
	}
}
