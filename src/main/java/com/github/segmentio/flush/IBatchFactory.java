package com.github.segmentio.flush;

import java.util.List;

import com.github.segmentio.models.BasePayload;
import com.github.segmentio.models.Batch;

/**
 * A Factory that creates Batch objects
 *
 */
public interface IBatchFactory {

	/**
	 * Creates a batch model around a list of items
	 * @param batch A list of items that represents the batch
	 * @return {@link Batch}
	 */
	public Batch create(List<BasePayload> batch);
	
}
