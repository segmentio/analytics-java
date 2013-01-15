package com.segment.models;

import com.ning.http.client.Response;


/**
 * Response callback from the server.
 */
public interface Callback {
	/**
	 * Called when a response is received from the server.
	 * @param response
	 */
	public void onResponse(Response response);
}