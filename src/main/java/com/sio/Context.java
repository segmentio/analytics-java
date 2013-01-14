package com.sio;

public class Context extends SafeProperties {

	private static final long serialVersionUID = 2042634726363431396L;

	private static final String USER_AGENT_KEY = "userAgent";
	private static final String IP_KEY = "ip";
	
	public Context() {
		super();
	}
	
	public Context(Object... kvs) {
		super(kvs);
	}
	
	public Context setUserAgent(String userAgent) {
		this.put(USER_AGENT_KEY, userAgent);
		return this;
	}
	
	public String getUserAgent() {
		return (String)this.get(USER_AGENT_KEY);
	}
	
	public Context setIp(String ip) {
		this.put(IP_KEY, ip);
		return this;
	}
	
	public String getIp() {
		return (String)this.get(IP_KEY);
	}
	
}
