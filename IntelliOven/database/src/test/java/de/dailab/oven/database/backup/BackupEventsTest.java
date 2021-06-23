package de.dailab.oven.database.backup;

import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;
import de.dailab.oven.database.backup.events.BackupFailureEvent;
import de.dailab.oven.database.backup.events.BackupFileDateRevisorEvent;
import de.dailab.oven.database.backup.events.BackupFileNumberRevisorEvent;
import de.dailab.oven.database.backup.events.CreateBackupEvent;
import de.dailab.oven.database.backup.events.DatabaseConnectionEvent;
import de.dailab.oven.database.backup.events.KillStateEvent;
import de.dailab.oven.database.backup.events.WaitEvent;
import de.dailab.oven.database.configuration.Configuration;
import de.dailab.oven.database.configuration.ConfigurationLoader;
import de.dailab.oven.database.configuration.DatabaseConfiguration;
import de.dailab.oven.database.configuration.Graph;
import de.dailab.oven.database.exceptions.ConfigurationException;
import de.dailab.oven.database.exceptions.DatabaseException;

/**
 * Tests all events in package de.dailab.oven.database.backup.events
 * @author Tristan Schroer
 * @since 06.02.2020
 */
public class BackupEventsTest {
	
	private static final int SOURCE_STATUS_CODE = 200;
	private static final ConfigurationLoader CONFIG_LOADER = new ConfigurationLoader();
	private static final Logger LOGGER = Logger.getGlobal();
	
	/**
	 * Test initialization and methods of BackupFailureEvent
	 */
	@Test
	public void backupFailureEventTest() {
		BackupFailureEvent backupFailureEvent;
		//Initialize with source equals NULL which shall throw a NullPointerException
		try {
			backupFailureEvent = new BackupFailureEvent(null);
			assertFalse(true);
		} catch (final Exception e) {
			assertEquals(NullPointerException.class, e.getClass());
		}
		backupFailureEvent = new BackupFailureEvent(this);
		//Test if error message is correct
		assertEquals("Failed to create backup. Graph is still running", backupFailureEvent.getErrorMessage());
		//Test correct source
		assertEquals(this, backupFailureEvent.getSource());
	}
	
	/**
	 * Test initialization and methods of BackupFileDateRevisorEvent
	 */
	@Test
	public void backupFileDateRevisorEventTest() {
		BackupFileDateRevisorEvent backupFileDateRevisorEvent;
		//Initialize with source equals NULL which shall throw a NullPointerException
		try {
			backupFileDateRevisorEvent = new BackupFileDateRevisorEvent(null, false, SOURCE_STATUS_CODE);
			assertFalse(true);
		} catch (final Exception e) {
			assertEquals(NullPointerException.class, e.getClass());
		}
		backupFileDateRevisorEvent = new BackupFileDateRevisorEvent(this, true, SOURCE_STATUS_CODE);
		//Test correct stored boolean 1/2
		assertTrue(backupFileDateRevisorEvent.getBackupFilesUpToDate());
		//Test correct stored source status code
		assertEquals(SOURCE_STATUS_CODE, backupFileDateRevisorEvent.getSourceStatusCode());
		//Test correct source
		assertEquals(this, backupFileDateRevisorEvent.getSource());
		backupFileDateRevisorEvent = new BackupFileDateRevisorEvent(this, false, 200);
		//Test correct stored boolean 2/2
		assertEquals(false, backupFileDateRevisorEvent.getBackupFilesUpToDate());
	}
	
	/**
	 * Test initialization and methods of BackupFileNumberRevisorEvent
	 */
	@Test
	public void backupFileNumberRevisorEventTest() {
		BackupFileNumberRevisorEvent backupFileNumberRevisorEvent;
		final int numberCode = 1;
		//Initialize with source equals NULL which shall throw a NullPointerException
		try {
			backupFileNumberRevisorEvent = new BackupFileNumberRevisorEvent(null, numberCode, SOURCE_STATUS_CODE);
			assertFalse(true);
		} catch (final Exception e) {
			assertEquals(NullPointerException.class, e.getClass());
		}
		backupFileNumberRevisorEvent = new BackupFileNumberRevisorEvent(this, numberCode, SOURCE_STATUS_CODE);
		//Test correct stored number code
		assertEquals(numberCode, backupFileNumberRevisorEvent.getNumberOfBackupFilesCode());
		//Test correct stored source status code
		assertEquals(SOURCE_STATUS_CODE, backupFileNumberRevisorEvent.getSourceStatusCode());
		//Test correct source
		assertEquals(this, backupFileNumberRevisorEvent.getSource());
	}
	
	/**
	 * Test initialization and methods of CreateBackupEvent
	 */
	@Test
	public void createBackupEventTest() {
		CreateBackupEvent createBackupEvent;
		//Initialize with source equals NULL which shall throw a NullPointerException
		try {
			createBackupEvent = new CreateBackupEvent(null, SOURCE_STATUS_CODE);
			assertFalse(true);
		} catch (final Exception e) {
			assertEquals(NullPointerException.class, e.getClass());
		}
		createBackupEvent = new CreateBackupEvent(this, SOURCE_STATUS_CODE);
		//Test correct stored source status code
		assertEquals(SOURCE_STATUS_CODE,createBackupEvent.getSourceStatusCode());
		//Test correct source
		assertEquals(this, createBackupEvent.getSource());
	}
	
	/**
	 * Test initialization and methods of DatabaseConnectionEvent
	 */
	@Test
	public void databaseConnectionEventTest() {
		DatabaseConnectionEvent databaseConnectionEvent;
		//Initialize with source equals NULL which shall throw a NullPointerException
		try {
			databaseConnectionEvent = new DatabaseConnectionEvent(null, null, SOURCE_STATUS_CODE);
			assertFalse(true);
		} catch (final Exception e) {
			assertEquals(NullPointerException.class, e.getClass());
		}
		//Test with NULL as graphs instance
		databaseConnectionEvent = new DatabaseConnectionEvent(this, null, SOURCE_STATUS_CODE);
		//Test correct stored graph instance
		assertEquals(null, databaseConnectionEvent.getGraph());
		//Test correct stored source status code
		assertEquals(SOURCE_STATUS_CODE, databaseConnectionEvent.getSourceStatusCode());
		//Test correct source
		assertEquals(this, databaseConnectionEvent.getSource());
		//Test real graph instance if environment variables are set
		try {
			Graph graph = null;
			if(CONFIG_LOADER.getUri().isEmpty()) {
				graph = new Graph(Configuration.getInstance().getDatabaseConfiguration());
			}
			else {
				graph = new Graph(new DatabaseConfiguration(CONFIG_LOADER.getUri(), CONFIG_LOADER.getUser(), CONFIG_LOADER.getPw()));
			}
			databaseConnectionEvent = new DatabaseConnectionEvent(this, graph, SOURCE_STATUS_CODE);
			assertEquals(graph, databaseConnectionEvent.getGraph());
			graph.close();
		} catch (final DatabaseException | IllegalArgumentException | ConfigurationException e) {
			LOGGER.log(Level.INFO, "Environment variables not set. Skipped Test DatabseConnectionEvent with existing graph.");
		}
	}

	/**
	 * Test initialization and methods of KillStateEvent
	 */
	@Test
	public void killStateEventTest() {
		KillStateEvent killStateEvent;
		//Initialize with source equals NULL which shall throw a NullPointerException
		try {
			killStateEvent = new KillStateEvent(null, "Test");
			assertFalse(true);
		} catch (final Exception e) {
			assertEquals(NullPointerException.class, e.getClass());
		}
		killStateEvent = new KillStateEvent(this, "Test message");
		//Test correct stored error message
		assertEquals("Test message", killStateEvent.getErrorMessage());
		//Test correct source
		assertEquals(this, killStateEvent.getSource());
		//Test with sample preset error message
		killStateEvent = new KillStateEvent(this, KillStateEvent.ESTABLISHING_CONNECTION_FAILED);
		assertEquals(KillStateEvent.ESTABLISHING_CONNECTION_FAILED, killStateEvent.getErrorMessage());
	}
	
	/**
	 * Test initialization and methods of WaitEvent
	 */
	@Test
	public void waitEventTest() {
		WaitEvent waitEvent;
		final long milliSecondsToWait = 1000;
		final LocalDateTime expirationTime = LocalDateTime.now();
		//Initialize with source equals NULL which shall throw a NullPointerException
		try {
			waitEvent = new WaitEvent(null, 0, SOURCE_STATUS_CODE, expirationTime);
			assertFalse(true);
		} catch (final Exception e) {
			assertEquals(NullPointerException.class, e.getClass());
		}
		//Initialize with expirationTime equals NULL which shall throw a NullPointerException
		try {
			waitEvent = new WaitEvent(this, 0, SOURCE_STATUS_CODE, null);
			assertFalse(true);
		} catch (final Exception e) {
			assertEquals(NullPointerException.class, e.getClass());
		}
		waitEvent = new WaitEvent(this, milliSecondsToWait, SOURCE_STATUS_CODE, expirationTime);
		//Test correct source
		assertEquals(this, waitEvent.getSource());
		//Test correct stored milliSecondsToWait
		assertEquals(milliSecondsToWait, waitEvent.getMilliSecondsToWait());
		//Test correct stored source status code
		assertEquals(SOURCE_STATUS_CODE, waitEvent.getSourceStatusCode());
		//Test correct stored expirationTime
		assertEquals(expirationTime, waitEvent.getExpirationTime());
	}
}