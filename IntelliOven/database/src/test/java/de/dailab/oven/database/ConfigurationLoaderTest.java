package de.dailab.oven.database;

import static org.junit.Assert.*;

import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import de.dailab.oven.database.configuration.ConfigurationLoader;

public class ConfigurationLoaderTest {
	
	private static Map<String, String> envs;
	
	@Before
	public void initialize() {
		envs = System.getenv();
	}
		
	@Test
	public void ConfigurationWithVariablesTest() {
		final ConfigurationLoader cl = new ConfigurationLoader();
		final String uri = cl.getUri();
		final String user = cl.getUser();
		final String pw = cl.getPw();
		if(envs == null) {
			assertTrue(uri.contentEquals(""));
			assertTrue(user.contentEquals(""));
			assertTrue(pw.contentEquals(""));
		}
		else {
			for(final Map.Entry<String, String> entry : envs.entrySet()) {
				final String key = entry.getKey();
				final String value = entry.getValue();
				if(!key.contains("database") && !key.contains("oven")) {
					continue;
				}
				if(key.contains("uri")) {
					assertTrue(value.contentEquals(uri));
				}
				if(key.contains("user")) {
					assertTrue(value.contentEquals(user));
				}
				if(key.contains("pw")) {
					assertTrue(value.contentEquals(pw));
				}
			}			
		}
	}
	
}
