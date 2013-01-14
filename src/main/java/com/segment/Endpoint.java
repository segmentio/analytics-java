package com.segment;

/**
 * Represents the REST endpoint
 *
 */
public enum Endpoint {
	
	IDENTIFY {
		
		public String path() {
			return "/v1/identify";
		}
		
	},
	
	TRACK {
		
		public String path() {
			return "/v1/track";
		}
		
	},
	
	BATCH {
		
		public String path() {
			return "/v1/import";
		}
		
	}
	
}
