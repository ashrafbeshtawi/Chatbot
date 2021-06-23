package de.dailab.oven.database.backup.tools;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.dailab.oven.database.backup.BackupConfiguration;
import de.dailab.oven.database.backup.listeners.CriticalDatabaseStateListener;
import de.dailab.oven.database.backup.listeners.DatabaseListener;
import de.dailab.oven.database.backup.threads.BackupFailureThread;
import de.dailab.oven.database.backup.threads.BackupFileCreatorThread;
import de.dailab.oven.database.backup.threads.BackupFileDateRevisorThread;
import de.dailab.oven.database.backup.threads.BackupFileNumberRevisorThread;
import de.dailab.oven.database.backup.threads.DatabaseConnectionThread;
import de.dailab.oven.database.backup.threads.DatabaseWaitThread;
import de.dailab.oven.database.backup.threads.KillStateThread;
import de.dailab.oven.database.configuration.DatabaseConfiguration;
import de.dailab.oven.database.configuration.Graph;

/**
 * Tool which out sources starting the given threads correctly from other classes
 * @author Tristan Schroer
 */
public class ThreadStarter {
	
	@Nonnull
	private static final BackupConfiguration BACKUP_CONFIGURATION = BackupConfiguration.getInstance();
	@Nonnull
	private static final Logger LOGGER = Logger.getLogger(ThreadStarter.class.getName());
	@Nonnull
	private static final String DBL_WARNING = "DatabaseListener is NULL hence Thread is not started ";
	@Nonnull
	private static final String CDSL_WARNING = "CriticalDatabaseStateListener is NULL hence Thread is not started ";
	
	/**
	 * Starts a BackupFileNumberRevisorThread and adds BackupHandler as listener to it.<br>
	 * Results can be handled in gotBackupFileNumberCode() <br>
	 * Uses the file paths stored in the BackupConfiguration
	 * @param sourceStatusCode The private source status code to derive further actions
	 * @param listener			DatabaseStateListener waiting for incoming notification of this thread
	 */
	public void startBackupFileNumberRevisorThread(
			final int sourceStatusCode,
			@Nullable final DatabaseListener listener) {
		
		if(!isValidDBListener(listener, "startBackupFileNumberRevisorThread"))
			return;
		
		final BackupFileNumberRevisorThread backupFileNumberRevisorThread = new BackupFileNumberRevisorThread(
				BACKUP_CONFIGURATION.getCurrentBackupFilePath(), 
				BACKUP_CONFIGURATION.getPriorBackupFilePath(), 
						sourceStatusCode);
		
		backupFileNumberRevisorThread.addDatabaseListener(listener);
		
		backupFileNumberRevisorThread.start();
	}
	
	/**
	 * Starts a BackupFileDateRevisorThread and adds BackupHandler as listener to it.<br>
	 * Results can be handled in backupFilesUpToDate() and backupFilesNotUpToDate()<br>
	 * Uses the file paths stored in the BackupConfiguration
	 * @param sourceStatusCode The private source status code to derive further actions
	 * @param listener			DatabaseStateListener waiting for incoming notification of this thread
	 */
	public void startBackupFileDateRevisorThread(
			final int sourceStatusCode,
			@Nullable final DatabaseListener listener) {
		
		if(!isValidDBListener(listener, "startBackupFileDateRevisorThread"))
			return;
		
		final BackupFileDateRevisorThread backupFileDateRevisorThread = new BackupFileDateRevisorThread(
						BACKUP_CONFIGURATION.getBackupFilePaths(), 
						BACKUP_CONFIGURATION.getMilliSecondsUntilBackupExpires(), 
						sourceStatusCode);
		
		backupFileDateRevisorThread.addDatabaseListener(listener);
		
		backupFileDateRevisorThread.start();
	}
	
	/**
	 * Starts a DatabaseWaitThread and adds BackupHandler as listener to it. <br>
	 * Results can be handled in waitingSucceeded() and waitingFailed().
	 * @param milliSecondsToSleep	Milliseconds the thread shall sleep
	 * @param sourceStatusCode		The status code to identify where the thread has been started.
	 * @param expirationTime		Time when the waiting will expire
	 * @param listener				DatabaseStateListener waiting for incoming notification of this thread
	 */
	public void startDatabaseWaitThread(
			final long milliSecondsToSleep,
			final int sourceStatusCode,
			@Nullable LocalDateTime expirationTime,
			@Nullable final DatabaseListener listener) {
		
		if(!isValidDBListener(listener, "startDatabaseWaitThread"))
			return;
		
		if(expirationTime == null)
			expirationTime = LocalDateTime.now().plusNanos(
					TimeUnit.MILLISECONDS.toNanos(milliSecondsToSleep));
		
		final DatabaseWaitThread waitThread = new DatabaseWaitThread(milliSecondsToSleep,
				sourceStatusCode, 
				Objects.requireNonNull(expirationTime, "Expiration time must not be NULL."));
		
		waitThread.addDatabaseListener(listener);
		
		waitThread.start();
	}
	
	/**
	 * Starts a DatabaseConnectionThread with a sourceStatusCode, so that it can be used for internal 
	 * checks during establishing connection. Adds BackupHandler as listener. <br>
	 * Results can be handled in connectionToDatabaseSucceeded() and connectionToDatabaseFailed()
	 * @param sourceStatusCode 		The sources status code to handle later actions correctly.
	 * @param databaseConfiguration	The database configuration storing the database to connect to
	 * @param listener				DatabaseStateListener waiting for incoming notification of this thread
	 */
	public void startDatabaseConnectionThread(
			final int sourceStatusCode,
			@Nullable final DatabaseConfiguration databaseConfiguration,
			@Nullable final DatabaseListener listener) {
		
		if(!isValidDBListener(listener, "startDatabaseConnectionThread"))
			return;
		
		if(databaseConfiguration == null) {
			LOGGER.log(Level.INFO, "Database configuration is NULL hence Thread is not started");
			return;
		}
		
		final DatabaseConnectionThread databaseConnectionThread = new DatabaseConnectionThread(
				databaseConfiguration, sourceStatusCode);
		
		databaseConnectionThread.addDatabaseListener(listener);
		
		databaseConnectionThread.start();
	}
	
	/**
	 * Starts a BackupFileCreatorThread and adds BackupHandler as listener to it.<br>
	 * @param sourceStatusCode 	The sources status code to handle later actions correctly.
	 * @param graph				The graph instance which shall be used for performing the backup query
 	 * @param listener			DatabaseStateListener waiting for incoming notification of this thread
	 */
	public void startBackupFileCreatorThread(
			final int sourceStatusCode,
			@Nullable final Graph graph,
			@Nullable final DatabaseListener listener) {
		
		if(!isValidDBListener(listener, "startBackupFileCreatorThread"))
			return;
		
		if(graph == null) {
			LOGGER.log(Level.INFO, "Graph is NULL hence Thread is not started");
			return;
		}
		
		final BackupFileCreatorThread backupFileCreatorThread = new BackupFileCreatorThread(graph,
				sourceStatusCode);
		
		backupFileCreatorThread.addDatabaseListener(listener);
		
		backupFileCreatorThread.start();
	}
	
	/**
	 * Initiates an kill state error thread and adds the critical state observer to it.
	 * @param errorStatusCode	The given error code (Best to choose from KillStateThread.)
 	 * @param listener			CriticalDatabaseStateListener waiting for incoming notification of this thread
	 */
	public void startKillStateThread(
			final int errorStatusCode,
			@Nullable final CriticalDatabaseStateListener listener) {
		
		if(!isValidCriticalListener(listener, "startKillStateThread"))
			return;
		
		final KillStateThread killStateThread = new KillStateThread(errorStatusCode);
		
		killStateThread.addCriticalDatabaseStateListener(listener);
		
		killStateThread.start();
	}
	
	/**
	 * Initiates a backup failure meaning creation of backup file has not been possible.
 	 * @param listener			CriticalDatabaseStateListener waiting for incoming notification of this thread
	 */
	public void startBackupFailureThread(@Nullable final CriticalDatabaseStateListener listener) {
		
		if(!isValidCriticalListener(listener, "startBackupFailureThread"))
			return;
				
		final BackupFailureThread backupFailureThread = new BackupFailureThread();
		
		backupFailureThread.addCriticalDatabaseStateListener(listener);
		
		backupFailureThread.start();
	}
	
	/**
	 * Checks for listener being NULL and logs that the passed 
	 * {@link DatabaseListener} is NULL if so.
	 * @param listener	The passed listener
	 * @param source 	The calling method 
	 */
	private boolean isValidDBListener(@Nullable final DatabaseListener listener,
			@Nullable final String source) {
		
		if(listener == null) {
			LOGGER.log(Level.INFO, DBL_WARNING + "in {0}", source);
			return false;
		}
		
		return true;
	}
	
	/**
	 * Checks for listener being NULL and logs that the passed 
	 * {@link CriticalDatabaseStateListener} is NULL if so.
	 * @param listener	The passed listener
	 * @param source 	The calling method 
	 */
	private boolean isValidCriticalListener(@Nullable final CriticalDatabaseStateListener listener,
			@Nullable final String source) {
		
		if(listener == null) {
			LOGGER.log(Level.INFO, CDSL_WARNING + "in {0}", source);
			return false;
		}
		
		return true;
	}
}
