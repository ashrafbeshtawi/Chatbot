package de.dailab.oven.database.backup;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.junit.Test;

/**
 * Tests BackupConfiguration for setting <i>osAndConfigurationAccepted</i> correctly.
 * <strong>IMPORTANT</strong> For correct working, this test needs to be run on Raspbian AND on Linux
 * @author Tristan Schroer
 * @since 06.02.2020
 */
public class BackupConfigurationTest {
	
	
	/**
	 * Test BackupConfiguration with Windows 10
	 */
	@Test
	public void windows10Test() {
		System.setProperty("os.name", "Windows 10");
		final BackupConfiguration testConfiguration = BackupConfiguration.getInstance();
		assertFalse(testConfiguration.getOperatingSystemIsAccepted());
	}
	
	/**
	 * Test BackupConfiguration with Linux
	 */
	@Test
	public void linuxAndRaspbianTest() {
		//Setting to Linux will not effect the result on Raspbian
		System.setProperty("os.name", "Linux");
		final BackupConfiguration testConfiguration = BackupConfiguration.getInstance();
		try {
			final Process process = Runtime.getRuntime().exec("cat /etc/issue");
			final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			if(!bufferedReader.readLine().toLowerCase().contains("raspbian")) {
				assertFalse(testConfiguration.getOperatingSystemIsAccepted());
			} else {
				assertTrue(testConfiguration.getOperatingSystemIsAccepted());
			}
			process.destroy();
		} catch (final Exception e) {
			assertEquals(IOException.class, e.getClass());
			assertFalse(testConfiguration.getOperatingSystemIsAccepted());
		}
	}
}