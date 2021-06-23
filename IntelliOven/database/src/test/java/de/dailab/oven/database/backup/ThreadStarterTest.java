package de.dailab.oven.database.backup;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
import de.dailab.oven.database.backup.threads.KillStateThread;
import de.dailab.oven.database.backup.tools.ThreadStarter;
import de.dailab.oven.database.configuration.ConfigurationLoader;
import de.dailab.oven.database.configuration.DatabaseConfiguration;
import de.dailab.oven.database.configuration.Graph;
import de.dailab.oven.database.exceptions.DatabaseException;

/**
 * Tests all threads for correct initialization in package de.dailab.oven.database.backup.threads
 * @author Tristan Schroer
 * @since 06.02.2020
 */
public class ThreadStarterTest {
	
	private static final int SOURCE_STATUS_CODE = 200;
	private static final ConfigurationLoader CONFIG_LOADER = new ConfigurationLoader();
	private static final Logger LOGGER = Logger.getGlobal();
	
	public int countDown = 7;
	
	private final TestListenerCritical testListenerCritical = new TestListenerCritical();
	private final TestListenerDatabase testListenerDatabase = new TestListenerDatabase();
	
	private static final ThreadStarter STARTER = new ThreadStarter();
	
	public class TestListenerCritical implements CriticalDatabaseStateListener {
		@Override
		public void killStateReached(final KillStateEvent killStateEvent) {
			ThreadStarterTest.this.countDown -=1;
		}
		@Override
		public void failedToCreateBackup(final BackupFailureEvent backupFailureEvent) {
			ThreadStarterTest.this.countDown -=1;
		}
		@Override
		public void finalErrorReached(final KillStateEvent killStateEvent) {
			ThreadStarterTest.this.countDown -=1;
		}
		
	}
	
	private class TestListenerDatabase implements DatabaseListener {
		@Override
		public void waitingSucceeded(final WaitEvent waitEvent) {
			ThreadStarterTest.this.countDown -=1;
		}
		@Override
		public void waitingFailed(final WaitEvent waitEvent) {
			ThreadStarterTest.this.countDown -=1;
		}
		@Override
		public void gotBackupFileNumberCode(final BackupFileNumberRevisorEvent backupFileNumberRevisorEvent) {
			ThreadStarterTest.this.countDown -=1;
		}
		@Override
		public void connectionToDatabaseSucceeded(final DatabaseConnectionEvent databaseConnectionEvent) {
			ThreadStarterTest.this.countDown -=1;
		}
		@Override
		public void connectionToDatabaseFailed(final DatabaseConnectionEvent databaseConnectionEvent) {
			ThreadStarterTest.this.countDown -=1;
		}
		@Override
		public void backupQueryExecuted(final CreateBackupEvent createBackupEvent) {
			ThreadStarterTest.this.countDown -=1;
		}
		@Override
		public void backupQueryFailed(final CreateBackupEvent createBackupEvent) {
			ThreadStarterTest.this.countDown -=1;
		}
		@Override
		public void backupFilesUpToDate(final BackupFileDateRevisorEvent backupFileDateRevisorEvent) {
			ThreadStarterTest.this.countDown -=1;
		}
		@Override
		public void backupFilesNotUpToDate(final BackupFileDateRevisorEvent backupFileDateRevisorEvent) {
			ThreadStarterTest.this.countDown -=1;
		}	
	}
	
//	@Test
	public void allThreadStartsTest() {
		STARTER.startBackupFailureThread(this.testListenerCritical);
		try {
			final Graph graph = new Graph(new DatabaseConfiguration(CONFIG_LOADER.getUri(), CONFIG_LOADER.getUser(), CONFIG_LOADER.getPw()));
			STARTER.startBackupFileCreatorThread(SOURCE_STATUS_CODE, graph, this.testListenerDatabase);
		} catch (final DatabaseException e) {
			this.countDown -=1;
			e.printStackTrace();
		}
		STARTER.startBackupFileDateRevisorThread(SOURCE_STATUS_CODE, this.testListenerDatabase);
		STARTER.startBackupFileNumberRevisorThread(SOURCE_STATUS_CODE, this.testListenerDatabase);
		STARTER.startDatabaseConnectionThread(SOURCE_STATUS_CODE, new DatabaseConfiguration(CONFIG_LOADER.getUri(), CONFIG_LOADER.getUser(), CONFIG_LOADER.getPw()), this.testListenerDatabase);
		STARTER.startDatabaseWaitThread(1000, SOURCE_STATUS_CODE, LocalDateTime.now().plusSeconds(1), this.testListenerDatabase);
		STARTER.startKillStateThread(KillStateThread.ESTABLISHING_CONNECTION_FAILED, this.testListenerCritical);
		
		LocalDateTime max = LocalDateTime.now().plus(1, ChronoUnit.MINUTES);
		
		while(this.countDown != 0 && max.isAfter(LocalDateTime.now())) {
			try {
				wait();
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		LOGGER.log(Level.INFO, "Test succeeded");
	}
}