package com.github.segmentio.request;

import com.github.segmentio.models.Batch;

public interface IRequester {

	public boolean send(Batch batch);
	
	public void close();
}
