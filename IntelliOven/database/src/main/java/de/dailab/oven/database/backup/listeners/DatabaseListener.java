package de.dailab.oven.database.backup.listeners;

import java.util.EventListener;

import javax.annotation.Nonnull;

import de.dailab.oven.database.backup.events.BackupFileDateRevisorEvent;
import de.dailab.oven.database.backup.events.BackupFileNumberRevisorEvent;
import de.dailab.oven.database.backup.events.CreateBackupEvent;
import de.dailab.oven.database.backup.events.DatabaseConnectionEvent;
import de.dailab.oven.database.backup.events.WaitEvent;

/**
 * EventListener designed for package de.dailab.oven.database.backup.<br> 
 * @author Tristan Schroer
 * @since 04.02.2020
 */
public interface DatabaseListener extends EventListener{
	
	/**
	 * Procedure which is called if the waiting process succeeded
	 * @param waitEvent	The wait event which has been fired
	 */
	void waitingSucceeded(@Nonnull WaitEvent waitEvent);
	
	/**
	 * Procedure which is called if the waiting process failed
	 * @param waitEvent The wait event which has been fired
	 */
	void waitingFailed(@Nonnull WaitEvent waitEvent);
	
	/**
	 * Procedure which is called when the number code for existing backup files is received
	 * @param backupFileNumberRevisorEvent
	 */
	void gotBackupFileNumberCode(@Nonnull BackupFileNumberRevisorEvent backupFileNumberRevisorEvent);
	
	/**
	 * Procedure which is called when the connection to the database has been established
	 * @param databaseConnectionEvent A DatabseConnectionEvent which stores a graph instance as well as the sources status code
	 */
	void connectionToDatabaseSucceeded(@Nonnull DatabaseConnectionEvent databaseConnectionEvent);
	
	/**
	 * Procedure which is called when the connection to the database could not be established
	 * @param databaseConnectionEvent A DatabseConnectionEvent which stores a graph instance as well as the sources status code
	 */
	void connectionToDatabaseFailed(@Nonnull DatabaseConnectionEvent databaseConnectionEvent);
	
	/**
	 * Procedure which is called when the backup query has been executed
	 * @param createBackupEvent CreateBackupEvent containing the source status code
	 */
	void backupQueryExecuted(@Nonnull CreateBackupEvent createBackupEvent);
	
	/**
	 * Procedure which is called when the backup query failed to execute
	 * @param createBackupEvent	CreateBackupEvent containing the source status code
	 */
	void backupQueryFailed(@Nonnull CreateBackupEvent createBackupEvent);
	
	/**
	 * Procedure which is called when the back up files (at least one) are up to date
	 * @param backupFileDateRevisorEvent	BackupFileDateRevisorEvent containing the source status code
	 */
	void backupFilesUpToDate(@Nonnull BackupFileDateRevisorEvent backupFileDateRevisorEvent);
	
	/**
	 * Procedure which is called when the back up files are not up to date
	 * @param backupFileDateRevisorEvent	BackupFileDateRevisorEvent containing the source status code
	 */
	void backupFilesNotUpToDate(@Nonnull BackupFileDateRevisorEvent backupFileDateRevisorEvent);
	
}