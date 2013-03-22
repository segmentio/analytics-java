package com.github.segmentio.models;

import java.util.Date;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Props extends HashMap<String, Object> {

	private static final long serialVersionUID = -3751430322294788170L;

	private static final Logger logger = 
			LoggerFactory.getLogger(Props.class);
	
	public Props() {
		super(1);
	}
	
	public Props(Object... kvs) {
		
		super(kvs == null ? 1 : kvs.length / 2);
		
		if (kvs != null) {
			if (kvs.length % 2 != 0) {
				
				logger.warn("Segmentio properties must be initialized with an " + 
						"even number of arguments, like so: [Key, Value, Key, Value]");	
			} else {
				if (kvs.length > 1) {
					for (int i = 0; i < kvs.length; i += 2) {
						this.put(kvs[i].toString(), kvs[i+1]);
					}
				}
			}
		}
	}
	

	public Props put(String key, Object value) {
		
		if (allowed(value)) {
			super.put(key, value);
		} else {
			logger.warn(
					String.format("Key %s value %s not allowed because it is " + 
						"not of type String, Integer, Double, Boolean, or Date.",
						key, value));
		}
		
		return this;
	}
	
	public boolean allowed(Object value) {
		
		if (value instanceof String ||
			value instanceof Boolean || 
			value instanceof Integer || 
			value instanceof Double || 
			value instanceof Date || 
			value instanceof Props) {
			return true;
		} else {
			return false;
		}
	}
}
