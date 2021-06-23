package de.dailab.oven.database.backup;

import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
import de.dailab.oven.database.backup.listeners.CriticalDatabaseStateListener;
import de.dailab.oven.database.backup.listeners.DatabaseListener;
import de.dailab.oven.database.backup.threads.BackupFailureThread;
import de.dailab.oven.database.backup.threads.BackupFileCreatorThread;
import de.dailab.oven.database.backup.threads.BackupFileDateRevisorThread;
import de.dailab.oven.database.backup.threads.BackupFileNumberRevisorThread;
import de.dailab.oven.database.backup.threads.DatabaseConnectionThread;
import de.dailab.oven.database.backup.threads.DatabaseWaitThread;
import de.dailab.oven.database.backup.threads.KillStateThread;
import de.dailab.oven.database.configuration.Configuration;
import de.dailab.oven.database.configuration.ConfigurationLoader;
import de.dailab.oven.database.configuration.DatabaseConfiguration;
import de.dailab.oven.database.configuration.Graph;
import de.dailab.oven.database.exceptions.ConfigurationException;
import de.dailab.oven.database.exceptions.DatabaseException;

/**
 * Tests all threads for correct initialization in package de.dailab.oven.database.backup.threads
 * @author Tristan Schroer
 * @since 06.02.2020
 */
public class ThreadInitializationTest {
	
	private static final int SOURCE_STATUS_CODE = 200;
	private static final ConfigurationLoader CONFIG_LOADER = new ConfigurationLoader();
	private static final Logger LOGGER = Logger.getGlobal();
	
	private final TestListenerCritical testListenerCritical = new TestListenerCritical();
	private final TestListenerDatabase testListenerDatabase = new TestListenerDatabase();
	
	public class TestListenerCritical implements CriticalDatabaseStateListener {
		@Override
		public void killStateReached(final KillStateEvent killStateEvent) {}
		@Override
		public void failedToCreateBackup(final BackupFailureEvent backupFailureEvent) {}
		@Override
		public void finalErrorReached(final KillStateEvent killStateEvent) {}
		
	}
	
	private class TestListenerDatabase implements DatabaseListener {
		@Override
		public void waitingSucceeded(final WaitEvent waitEvent) {}
		@Override
		public void waitingFailed(final WaitEvent waitEvent) {}
		@Override
		public void gotBackupFileNumberCode(final BackupFileNumberRevisorEvent backupFileNumberRevisorEvent) {}
		@Override
		public void connectionToDatabaseSucceeded(final DatabaseConnectionEvent databaseConnectionEvent) {}
		@Override
		public void connectionToDatabaseFailed(final DatabaseConnectionEvent databaseConnectionEvent) {}
		@Override
		public void backupQueryExecuted(final CreateBackupEvent createBackupEvent) {}
		@Override
		public void backupQueryFailed(final CreateBackupEvent createBackupEvent) {}
		@Override
		public void backupFilesUpToDate(final BackupFileDateRevisorEvent backupFileDateRevisorEvent) {}
		@Override
		public void backupFilesNotUpToDate(final BackupFileDateRevisorEvent backupFileDateRevisorEvent) {}
	}
	
	/**
	 * Test initialization and methods of BackupFailureThread
	 */
	@Test
	public void backupFailureThreadTest() {
		final BackupFailureThread backupFailureThread;
		backupFailureThread = new BackupFailureThread();
		//Adding NULL as listener shall throw a NullpointerException
		try {
			backupFailureThread.addCriticalDatabaseStateListener(null);
			assertFalse(true);
		} catch (final Exception e) {
			assertEquals(NullPointerException.class, e.getClass());
		}
		backupFailureThread.addCriticalDatabaseStateListener(this.testListenerCritical);
		//Removing NULL as listener shall throw a NullpointerException
		try {
			backupFailureThread.removeCriticalDatabaseStateListener(null);
			assertFalse(true);
		} catch (final Exception e) {
			assertEquals(NullPointerException.class, e.getClass());
		}
		backupFailureThread.removeCriticalDatabaseStateListener(this.testListenerCritical);	
	}
	
	/**
	 * Test initialization and methods of BackupFileDateRevisorThread
	 */
	@Test
	public void backupFileDateRevisorEventTest() {
		BackupFileDateRevisorThread backupFileDateRevisorThread;
		//Initializing with NULL as list of backup files shall throw a NullpointerException
		try {
			backupFileDateRevisorThread = new BackupFileDateRevisorThread(null, 0, SOURCE_STATUS_CODE);
			assertFalse(true);
		} catch (final Exception e) {
			assertEquals(NullPointerException.class, e.getClass());
		}
		backupFileDateRevisorThread = new BackupFileDateRevisorThread(new ArrayList<String>(), 0, SOURCE_STATUS_CODE);
		//Adding NULL as listener shall throw a NullpointerException
		try {
			backupFileDateRevisorThread.addDatabaseListener(null);
			assertFalse(true);
		} catch (final Exception e) {
			assertEquals(NullPointerException.class, e.getClass());
		}
		backupFileDateRevisorThread.addDatabaseListener(this.testListenerDatabase);
		//Removing NULL as listener shall throw a NullpointerException
		try {
			backupFileDateRevisorThread.removeDatabaseListener(null);
			assertFalse(true);
		} catch (final Exception e) {
			assertEquals(NullPointerException.class, e.getClass());
		}
		backupFileDateRevisorThread.removeDatabaseListener(this.testListenerDatabase);	
	}
	
	/**
	 * Test initialization and methods of BackupFileCreatorThread
	 */
	@Test
	public void backupFileCreatorThreadTest() {
		BackupFileCreatorThread backupFileCreatorThread;
		//Initializing with NULL as graph instance shall throw a NullpointerException
		try {
			backupFileCreatorThread = new BackupFileCreatorThread(null, SOURCE_STATUS_CODE);
			assertFalse(true);
		} catch (final Exception e) {
			assertEquals(NullPointerException.class, e.getClass());
		}
		
		//Test real graph instance if environment variables are set
		try {
			Graph graph = null;
			if(CONFIG_LOADER.getUri().isEmpty()) {
				graph = new Graph(Configuration.getInstance().getDatabaseConfiguration());
			}
			else {
				graph = new Graph(new DatabaseConfiguration(CONFIG_LOADER.getUri(), CONFIG_LOADER.getUser(), CONFIG_LOADER.getPw()));
			}
			backupFileCreatorThread = new BackupFileCreatorThread(graph, SOURCE_STATUS_CODE);
			//Adding NULL as listener shall throw a NullpointerException
			try {
				backupFileCreatorThread.addDatabaseListener(null);
				assertFalse(true);
			} catch (final Exception e) {
				assertEquals(NullPointerException.class, e.getClass());
			}
			backupFileCreatorThread.addDatabaseListener(this.testListenerDatabase);
			//Removing NULL as listener shall throw a NullpointerException
			try {
				backupFileCreatorThread.removeDatabaseListener(null);
				assertFalse(true);
			} catch (final Exception e) {
				assertEquals(NullPointerException.class, e.getClass());
			}
			backupFileCreatorThread.removeDatabaseListener(this.testListenerDatabase);
			if(graph != null) {
				graph.close();				
			}
		} catch (final DatabaseException | IllegalArgumentException | ConfigurationException e) {
			LOGGER.log(Level.INFO, "Environment variables not set. Skipped Initialization-Test BackupFileCreatorThread with existing graph.");
		}
		
	}
	
	/**
	 * Test initialization and methods of BackupFileNumberRevisorThread
	 */
	@Test
	public void backupFileNumberRevisorThreadTest() {
		final BackupFileNumberRevisorThread backupFileDateNumberThread;

		backupFileDateNumberThread = new BackupFileNumberRevisorThread(null, null, SOURCE_STATUS_CODE);
		//Adding NULL as listener shall throw a NullpointerException
		try {
			backupFileDateNumberThread.addDatabaseListener(null);
			assertFalse(true);
		} catch (final Exception e) {
			assertEquals(NullPointerException.class, e.getClass());
		}
		backupFileDateNumberThread.addDatabaseListener(this.testListenerDatabase);
		//Removing NULL as listener shall throw a NullpointerException
		try {
			backupFileDateNumberThread.removeDatabaseListener(null);
			assertFalse(true);
		} catch (final Exception e) {
			assertEquals(NullPointerException.class, e.getClass());
		}
		backupFileDateNumberThread.removeDatabaseListener(this.testListenerDatabase);	
	}
	
	/**
	 * Test initialization and methods of DatabaseConnectionEvent
	 */
	@Test
	public void databaseConnectionThreadTest() {
		DatabaseConnectionThread databaseConnectionThread;
		//Initializing with NULL as database configuration of backup files shall throw a NullpointerException
		try {
			databaseConnectionThread = new DatabaseConnectionThread(null, SOURCE_STATUS_CODE);
			assertFalse(true);
		} catch (final Exception e) {
			assertEquals(NullPointerException.class, e.getClass());
		}
		databaseConnectionThread = new DatabaseConnectionThread(new DatabaseConfiguration("", "", ""), SOURCE_STATUS_CODE);
		//Adding NULL as listener shall throw a NullpointerException
		try {
			databaseConnectionThread.addDatabaseListener(null);
			assertFalse(true);
		} catch (final Exception e) {
			assertEquals(NullPointerException.class, e.getClass());
		}
		databaseConnectionThread.addDatabaseListener(this.testListenerDatabase);
		//Removing NULL as listener shall throw a NullpointerException
		try {
			databaseConnectionThread.removeDatabaseListener(null);
			assertFalse(true);
		} catch (final Exception e) {
			assertEquals(NullPointerException.class, e.getClass());
		}
		databaseConnectionThread.removeDatabaseListener(this.testListenerDatabase);	
	}

	/**
	 * Test initialization and methods of KillStateEvent
	 */
	@Test
	public void killStateThreadTest() {
		final KillStateThread killStateThread;
		killStateThread = new KillStateThread(0);
		//Adding NULL as listener shall throw a NullpointerException
		try {
			killStateThread.addCriticalDatabaseStateListener(null);
			assertFalse(true);
		} catch (final Exception e) {
			assertEquals(NullPointerException.class, e.getClass());
		}
		killStateThread.addCriticalDatabaseStateListener(this.testListenerCritical);
		//Removing NULL as listener shall throw a NullpointerException
		try {
			killStateThread.removeCriticalDatabaseStateListener(null);
			assertFalse(true);
		} catch (final Exception e) {
			assertEquals(NullPointerException.class, e.getClass());
		}
		killStateThread.removeCriticalDatabaseStateListener(this.testListenerCritical);	
	}
	
	/**
	 * Test initialization and methods of WaitEvent
	 */
	@Test
	public void DatabaseWaitTest() {
		DatabaseWaitThread databaseWaitThread;
		//Initializing with NULL as expiration time of backup files shall throw a NullpointerException
		try {
			databaseWaitThread = new DatabaseWaitThread(0, SOURCE_STATUS_CODE, null);
			assertFalse(true);
		} catch (final Exception e) {
			assertEquals(NullPointerException.class, e.getClass());
		}
		databaseWaitThread = new DatabaseWaitThread(0, SOURCE_STATUS_CODE, LocalDateTime.now());
		//Adding NULL as listener shall throw a NullpointerException
		try {
			databaseWaitThread.addDatabaseListener(null);
			assertFalse(true);
		} catch (final Exception e) {
			assertEquals(NullPointerException.class, e.getClass());
		}
		databaseWaitThread.addDatabaseListener(this.testListenerDatabase);
		//Removing NULL as listener shall throw a NullpointerException
		try {
			databaseWaitThread.removeDatabaseListener(null);
			assertFalse(true);
		} catch (final Exception e) {
			assertEquals(NullPointerException.class, e.getClass());
		}
		databaseWaitThread.removeDatabaseListener(this.testListenerDatabase);	
	}
}