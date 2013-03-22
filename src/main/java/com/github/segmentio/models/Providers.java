package com.github.segmentio.models;

/**
 * A context containers that helps specify which providers are enabled or disabled.
 *
 */
public class Providers extends Props {

	private static final long serialVersionUID = -4492489732893613507L;

	/**
	 * Sets all providers as enabled or disabled for default. Useful for turning
	 * every provider except for one off.
	 * @param enabled Whether every provider is turned on or off by default.
	 * @return Providers object for chaining.
	 */
	public Providers setDefault(boolean enabled) {
		this.put("all", enabled);
		return this;
	}
	
	/**
	 * Set a specific provider as enabled or disabled for this call.
	 * @param providerName The name of the analytics provider you're trying to turn on/off.
	 * Check out https://segment.io/docs/methods/identify#choosing-providers for the list of provider names. 
	 * @param enabled  Specifies whether this provider is enabled or disabled.
	 * @return Providers object for chaining.
	 */
	public Providers setEnabled(String providerName, boolean enabled) {
		this.put(providerName, enabled);
		return this;
	}
	
	
}
