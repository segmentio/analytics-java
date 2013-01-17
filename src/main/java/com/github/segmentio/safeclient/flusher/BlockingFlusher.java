package com.github.segmentio.safeclient.flusher;

import java.util.List;

import com.github.segmentio.safeclient.BatchedOperation;

public class BlockingFlusher implements IFlusher {


	public boolean canFlush() {
		return true;
	}
	
	public <M> void flush(BatchedOperation<M> operation, List<M> batch) {
		operation.performFlush(batch);
	}

	public void close() {
		// do nothing
	}

}
