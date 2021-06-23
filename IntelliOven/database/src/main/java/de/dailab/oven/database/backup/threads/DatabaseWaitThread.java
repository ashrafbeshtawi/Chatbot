package de.dailab.oven.database.backup.threads;

import java.time.LocalDateTime;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.swing.event.EventListenerList;

import de.dailab.oven.database.backup.events.WaitEvent;
import de.dailab.oven.database.backup.listeners.DatabaseListener;

/**
 * Thread that waits a given amount of milliseconds.<br>
 * Designed to work with DatabaseListener and WaitEvent.
 * @author Tristan Schroer
 * @since 04.02.2020
 */
public class DatabaseWaitThread extends Thread{

	@Nonnull
	private final EventListenerList listeners = new EventListenerList();
	private final long milliSecondsToSleep;
	private final int sourceStatusCode;
	@Nonnull
	private final LocalDateTime expirationTime;
	
	/**
	 * This threads waits the given long in milliseconds and notifies the listeners if waiting failed or succeeded 
	 * @param milliSecondsToSleep	Milliseconds to wait (Readjusted to 0 in case of negative numbers)
	 * @param sourceStatusCode		The sources status code
	 * @param expirationTime		The time the waiting shall expire
	 */
	public DatabaseWaitThread(final long milliSecondsToSleep, final int sourceStatusCode, @Nonnull final LocalDateTime expirationTime) {
		if(milliSecondsToSleep < 0) {
			this.milliSecondsToSleep = 0;
		}
		else {
			this.milliSecondsToSleep = milliSecondsToSleep;			
		}
		this.sourceStatusCode = sourceStatusCode;
		this.expirationTime = Objects.requireNonNull(expirationTime, "Expiration time must not be NULL");
	}
	
	/**
	 * Tries to sleep the preset milliseconds. Fires the a wait event with given status code for success/failure
	 */
	@Override
	public void run() {
		try {
			Thread.sleep(this.milliSecondsToSleep);
			notifyWaitingSucceeded(new WaitEvent(this, this.milliSecondsToSleep, this.sourceStatusCode, this.expirationTime));
		} catch (final Exception e) {
			notifyWaitingFailed(new WaitEvent(this, this.milliSecondsToSleep, this.sourceStatusCode, this.expirationTime));
		}
	}
	
	/**
	 * Notifies the listeners that the waiting succeeded
	 * @param waitEvent The waitEvent to pass to the listeners
	 */
	protected synchronized void notifyWaitingSucceeded(@Nonnull final WaitEvent waitEvent) {
		for(final DatabaseListener listener : this.listeners.getListeners(DatabaseListener.class)) {
			listener.waitingSucceeded(Objects.requireNonNull(waitEvent, "WaitEvent must not be null."));
		}
	}
	
	/**
	 * Notifies the listeners that waiting failed
	 * @param waitEvent The waitEvent to pass to the listeners 
	 */
	protected synchronized void notifyWaitingFailed(@Nonnull final WaitEvent waitEvent) {
		for(final DatabaseListener listener : this.listeners.getListeners(DatabaseListener.class)) {
			listener.waitingFailed(Objects.requireNonNull(waitEvent, "WaitEvent must not be null."));
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