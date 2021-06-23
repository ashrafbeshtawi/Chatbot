package de.dailab.oven.database.backup.threads;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.event.EventListenerList;

import de.dailab.oven.database.FileOperations;
import de.dailab.oven.database.backup.events.BackupFileNumberRevisorEvent;
import de.dailab.oven.database.backup.listeners.DatabaseListener;


/**
 * Extracts a number code based on the existence of two (backup) files.
 * Works perfectly with BackumFileNumberRevisorEvent and DatabaseListener
 * Number codes: 
 *  0: No backup file exists (Or given file path are NULL)
 *  1: First backup file exists
 *  2: Second backup file exists
 *  3: Both backup files exist
 * @author Tristan Schroer
 * @since 05.02.2020
 */
public class BackupFileNumberRevisorThread extends Thread{
	@Nonnull
	private final EventListenerList listeners = new EventListenerList();
	@Nullable
	private final String currentBackupFilePath;
	@Nullable
	private final String priorBackupFilePath;
	private final int sourceStatusCode;
	
	/**
	 * Initialize with paths to revise
	 * @param currentBackupFilePath	File path to the expected current backup file (first file)
	 * @param priorBackupFilePath	File path to the expected prior backup file (second file)
	 * @param sourceStatusCode		A status code which can be passed to derive further actions later on
	 */
	public BackupFileNumberRevisorThread(@Nullable final String currentBackupFilePath,
										 @Nullable final String priorBackupFilePath, final int sourceStatusCode) {
		this.currentBackupFilePath = currentBackupFilePath;
		this.priorBackupFilePath = priorBackupFilePath;
		this.sourceStatusCode = sourceStatusCode;
	}
	
	/**
	 * Tries to generate the number code for the existing backup files
	 */
	@Override
	public void run() {
		int revisionCode = 0;		
		if(FileOperations.fileExists(this.currentBackupFilePath)) {
			revisionCode += 1;
		}
		if(FileOperations.fileExists(this.priorBackupFilePath)) {
			revisionCode += 2;
		}
		notifyGotNumberOfFiles(new BackupFileNumberRevisorEvent(this, revisionCode, this.sourceStatusCode));
	}
	
	/**
	 * Notifies the listeners, that the numberCode for the number of backup files was received
	 * @param backupFileNumberRevisorEvent
	 */
	protected synchronized void notifyGotNumberOfFiles(@Nonnull final BackupFileNumberRevisorEvent backupFileNumberRevisorEvent) {
		for(final DatabaseListener listener : this.listeners.getListeners(DatabaseListener.class)) {
			listener.gotBackupFileNumberCode(Objects.requireNonNull(backupFileNumberRevisorEvent, "BackupFileNumberRevisorEvent must not be null."));
		}
	}
	
	/**
	 * Adds the listener for this Thread
	 * @param listener DatabaseListener to add
	 */
	public void addDatabaseListener(@Nonnull final DatabaseListener listener) {
		this.listeners.add(DatabaseListener.class, Objects.requireNonNull(listener, "DatabaseListener must not be null."));
	}
	
	/**
	 * Removes the listener from the list of event listeners
	 * @param listener DatabaseListener to remove
	 */
	public void removeDatabaseListener(@Nonnull final DatabaseListener listener) {
		this.listeners.remove(DatabaseListener.class, Objects.requireNonNull(listener, "DatabaseListener must not be null."));
	}
}
