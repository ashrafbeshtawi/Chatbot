package de.dailab.oven.database.validate.model;

import javax.annotation.Nullable;

/**
 * Helper class for {@link NameValidator}
 * @author Tristan Schroer
 * @version 2.0.0
 */
public class NameRequest {

	private final int maxLength;
	private final String key;
	private final String value;
	
	/**
	 * Initialize a new name request for the {@link NameValidator}
	 * @param key		The key description for the value 
	 * @param value		The string (name) itself
	 * @param maxLength The maxLength for the value
	 */
	public NameRequest(@Nullable String key, @Nullable String value, int maxLength) {
		this.key = key;
		this.value = value;
		this.maxLength = maxLength;
	}
	
	/**
	 * @return The key description for the value
	 */
	public String getKey() {return this.key;}	
	
	/**
	 * @return The maxLength for the value
	 */
	public int getMaxLength() {return maxLength;}
	
	/**
	 * @return The strings value
	 */
	public String getValue() {return value;}
}