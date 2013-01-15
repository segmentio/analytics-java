package com.segment.models;


public class Context extends SafeProperties {

	private static final long serialVersionUID = 2042634726363431396L;

	private static final String IP_KEY = "ip";
	private static final String LIBRARY_KEY = "library";
	
	public Context() {
		super();
		addLibraryContext();
	}
	
	public Context(Object... kvs) {
		super(kvs);
		addLibraryContext();
	}
	
	private void addLibraryContext() {
		this.put(LIBRARY_KEY, "analytics-java");
	}
	
	public Context setIp(String ip) {
		this.put(IP_KEY, ip);
		return this;
	}
	
	public String getIp() {
		return (String)this.get(IP_KEY);
	}
	
}
