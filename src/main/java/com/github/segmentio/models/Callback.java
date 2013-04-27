package com.github.segmentio.models;



/**
 * Response callback from the server.
 */
public interface Callback {
	/**
	 * Called when a response is received from the server.
	 * @param response
	 */
	public void onResponse(boolean success, String message);
}