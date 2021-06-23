package de.dailab.oven.database.backup.threads;

import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.swing.event.EventListenerList;

import de.dailab.oven.database.FileOperations;
import de.dailab.oven.database.backup.events.BackupFileDateRevisorEvent;
import de.dailab.oven.database.backup.listeners.DatabaseListener;

/**
 * Thread which checks the files for their time of last modification. Not exclusive usable for backup files.<br>
 * Returns:<br>
 * 		<strong>true</strong> At least one of the given backup files is up to date<br>
 *  	<strong>false</strong> None of the given backup files is up to date
 * @author Tristan Schroer
 * @since  04.02.2020
 */
public class BackupFileDateRevisorThread extends Thread{

	@Nonnull
	private final EventListenerList listeners = new EventListenerList();
	@Nonnull
	private final List<String> backupFilePaths;
	private final long expirationDurationInMilliseconds;
	private final int sourceStatusCode;
	
	/**
	 * Initialize with paths to revise
	 * @param backupFilePaths					File paths to the backup files to revise
	 * @param expirationDurationInMilliseconds 	Duration within a backup is up to date in milliseconds
	 * @param sourceStatusCode					The sources status code
	 */
	public BackupFileDateRevisorThread(@Nonnull final List<String> backupFilePaths,
									   final long expirationDurationInMilliseconds, final int sourceStatusCode) {
		this.backupFilePaths = Objects.requireNonNull(backupFilePaths, "Backup file paths list must not be NULL");
		this.expirationDurationInMilliseconds = expirationDurationInMilliseconds;
		this.sourceStatusCode = sourceStatusCode;
	}
	
	/**
	 * Notifies the listeners whether the files as up to date or not <br>
	 * Returns:<br>
	 * 		<strong>true</strong> At least one of the given backup files is up to date<br>
	 *  	<strong>false</strong> None of the given backup files is up to date
	 */
	@Override
	public void run() {
		if(!this.backupFilePaths.isEmpty()) {
			for(final String filePath: this.backupFilePaths) {
				if(FileOperations.fileExists(filePath)
					&& FileOperations.minutesSinceLastModified(filePath) <= this.expirationDurationInMilliseconds / 1000) {
					
					notifyBackupFileIsUpToDate(new BackupFileDateRevisorEvent(this, true, this.sourceStatusCode));
					break;				
				}
			}
		}
		notifyBackupFileIsNotUpToDate(new BackupFileDateRevisorEvent(this, false, this.sourceStatusCode));
	}
	
	/**
	 * Notifies the listeners that the backup files are up to date.
	 * @param backupFileDateRevisorEvent	BackupFileDateRevisorEvent including the sources status code.
	 */
	protected synchronized void notifyBackupFileIsUpToDate(@Nonnull final BackupFileDateRevisorEvent backupFileDateRevisorEvent) {
		for(final DatabaseListener listener : this.listeners.getListeners(DatabaseListener.class)) {
			listener.backupFilesUpToDate(Objects.requireNonNull(backupFileDateRevisorEvent, "BackupFileDateRevisorEvent must not be null."));
		}
	}

	/**
	 * Notifies the listeners that the backup files are not up to date.
	 * @param backupFileDateRevisorEvent	BackupFileDateRevisorEvent including the sources status code.
	 */
	protected synchronized void notifyBackupFileIsNotUpToDate(@Nonnull final BackupFileDateRevisorEvent backupFileDateRevisorEvent) {
		for(final DatabaseListener listener : this.listeners.getListeners(DatabaseListener.class)) {
			listener.backupFilesNotUpToDate(Objects.requireNonNull(backupFileDateRevisorEvent, "BackupFileDateRevisorEvent must not be null."));
		}
	}
	
	/**
	 * Adds the listener for this Thread.
	 * @param listener DatabaseListener to add.
	 */
	public void addDatabaseListener(@Nonnull final DatabaseListener listener) {
		this.listeners.add(DatabaseListener.class, Objects.requireNonNull(listener, "DatabaseListener must not be null."));
	}
	
	/**
	 * Removes the listener from the list of event listeners.
	 * @param listener DatabaseListener to remove.
	 */
	public void removeDatabaseListener(@Nonnull final DatabaseListener listener) {
		this.listeners.remove(DatabaseListener.class, Objects.requireNonNull(listener, "DatabaseListener must not be null."));
	}
}