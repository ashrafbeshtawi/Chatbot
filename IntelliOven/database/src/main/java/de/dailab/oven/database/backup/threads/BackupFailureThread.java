package de.dailab.oven.database.backup.threads;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.swing.event.EventListenerList;

import de.dailab.oven.database.backup.events.BackupFailureEvent;
import de.dailab.oven.database.backup.listeners.CriticalDatabaseStateListener;

/**
 * Thread which initializes the CriticalDatabaseStateListeners to work.
 * Designed to work with BackupFailureEvent and CriticalDatabaseStateListener
 * @author Tristan Schroer
 * @since 04.02.2020
 */
public class BackupFailureThread extends Thread{

	@Nonnull
	private final EventListenerList listeners = new EventListenerList();
	
	/**
	 * Notifies the listeners that an error occurred during creating a backup.
	 */
	@Override
	public void run() {
		notifyBackupFailureEventOccured(new BackupFailureEvent(this));
	}
	
	/**
	 * Notifies the listeners that creating a backup failed.
	 * @param backupFailureEvent	BackupFailureEvent including the error message.
	 */
	protected synchronized void notifyBackupFailureEventOccured(@Nonnull final BackupFailureEvent backupFailureEvent) {
		for(final CriticalDatabaseStateListener listener : this.listeners.getListeners(CriticalDatabaseStateListener.class)) {
				listener.failedToCreateBackup(Objects.requireNonNull(backupFailureEvent, "BackupFailureEvent must not be null."));
		}
	}
	
	/**
	 * Adds the listener for this thread.
	 * @param listener CriticalDatabaseStateListener to add.
	 */
	public void addCriticalDatabaseStateListener(@Nonnull final CriticalDatabaseStateListener listener) {
		this.listeners.add(CriticalDatabaseStateListener.class, Objects.requireNonNull(listener, "CriticalDatabaseSListener must not be null."));
	}
	
	/**
	 * Removes the listener for this thread.
	 * @param listener CriticalDatabaseStateListener to remove.
	 */
	public void removeCriticalDatabaseStateListener(@Nonnull final CriticalDatabaseStateListener listener) {
		this.listeners.remove(CriticalDatabaseStateListener.class, Objects.requireNonNull(listener, "CriticalDatabaseStateListener must not be null."));
	}
}