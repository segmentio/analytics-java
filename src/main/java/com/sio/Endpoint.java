package com.sio;

public enum Endpoint {
	
	IDENTIFY {
		
		public String path() {
			return "/v2/i";
		}
		
	},
	
	TRACK {
		
		public String path() {
			return "/v2/t";
		}
		
	},
	
	BATCH {
		
		public String path() {
			return "/v2/batch";
		}
		
	}
	
}
