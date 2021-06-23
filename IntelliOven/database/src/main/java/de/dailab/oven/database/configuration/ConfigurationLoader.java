package de.dailab.oven.database.configuration;

import java.util.Map;

import javax.annotation.Nonnull;

public class ConfigurationLoader {
	
	@Nonnull
	private String uri = "";
	@Nonnull
	private String user = "";
	@Nonnull
	private String pw = "";
	
	/**
	 * Loads database configuration based on environment variables
	 */
	public ConfigurationLoader() {
		final Map<String, String> envs = System.getenv();
		if(envs != null) {
			for(final Map.Entry<String, String> entry : envs.entrySet()) {
				final String key = entry.getKey().toLowerCase();
				if(key.contains("database") && key.contains("oven")) {
					if(key.contains("uri")) 
						this.uri = entry.getValue();
					
					else if(key.contains("user")) 
						this.user = entry.getValue();
					
					else if(key.contains("pw")) 
						this.pw = entry.getValue();
										
				}
			}			
		}
	}
	
	/**
	 * Returns the URI of the database. Empty string in case there is nothing stored in the environment variables 
	 * @return URI
	 */
	@Nonnull
	public String getUri() {
		return this.uri;
	}
	
	/**
	 * Returns the user of the database. Empty string in case there is nothing stored in the environment variables
	 * @return user
	 */
	@Nonnull
	public String getUser() {
		return this.user;
	}
	
	/**
	 * Returns the password of the database. Empty string in case there is nothing stored in the environment variables
	 * @return password
	 */
	@Nonnull
	public String getPw() {
		return this.pw;
	}
}
