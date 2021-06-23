package de.dailab.oven.database;

import java.io.IOException;
import org.junit.Before;
import org.junit.Test;

public class RaspberryFunctionalityTest {
	
	private String osName = "";
	private boolean debian = false;
	
	@Before
	public void initialize() {
		this.osName = System.getProperty("os.name");
		if(this.osName.toLowerCase().contains("debian")) {
			this.debian = true;
		}
	}
		
	@Test
	public void ConfigurationWithVariablesTest() {
		if(this.debian) {
			System.out.println("true");
			try {
				final Process process = Runtime.getRuntime().exec("service neo4j stop");
			} catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}