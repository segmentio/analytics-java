package com.segment.safeclient.flusher;

import java.util.List;

import com.segment.safeclient.BatchedOperation;

public interface IFlusher {
	
	public boolean canFlush();
	
	public <M> void flush (BatchedOperation<M> operation, List<M> batch);
	
	public void close();
	
}
