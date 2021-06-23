package de.dailab.oven.database.backup.threads;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.swing.event.EventListenerList;

import de.dailab.oven.database.backup.events.KillStateEvent;
import de.dailab.oven.database.backup.listeners.CriticalDatabaseStateListener;

/**
 * Thread which initializes the CriticalDatabaseStateListeners to work.
 * Designed to work with KillStateEvent and CriticalDatabaseStateListener.
 * @author Tristan Schroer
 * @since 04.02.2020
 */
public class KillStateThread extends Thread{

	@Nonnull
	private final EventListenerList listeners = new EventListenerList();
	public static final int ESTABLISHING_CONNECTION_FAILED = 401;
	public static final int NO_CONNECTION_NO_BACKUP = 402;
	public static final int SYSTEM_RESET_REQUIRED = 403;
	
	private int currentErrorStatusCode;
	
	/**
	 * Initialize the thread with an error code.
	 * @param errorStatusCode Error code which shall be transformed to a suitable error message. <br>
	 * Choose either KillStateThread.ESTABLISHING_CONNECTION_FAILED, 
	 * KillStateThread.NO_CONNECTION_NO_BACKUP or KillStateThread.SYSTEM_RESET_REQUIRED to make the thread work.
	 */
	public KillStateThread(final int errorStatusCode) {
		this.currentErrorStatusCode = errorStatusCode;		
	}

	/**
	 * Notifies the listeners with the correct error message mapped to the error code.
	 */
	@Override
	public void run() {
		switch(this.currentErrorStatusCode) {
		case ESTABLISHING_CONNECTION_FAILED:
			notifyKillStateEventOccurred(new KillStateEvent(this, KillStateEvent.ESTABLISHING_CONNECTION_FAILED));
			break;
		case NO_CONNECTION_NO_BACKUP:
			notifyKillStateEventOccurred(new KillStateEvent(this, KillStateEvent.NO_CONNECTION_NO_BACKUP));
			break;
		case SYSTEM_RESET_REQUIRED:
			notifyFinalErrorOccurred(new KillStateEvent(this, KillStateEvent.SYSTEM_RESET_REQUIRED));
			break;
			
		//Do not send error message if the error code is unknown
		default:
			break;
		}
	}
	
	
	/**
	 * Notifies the listeners that a full clean up of the database is required
	 * @param killStateEvent KillStateEvent including the error message
	 */
	protected synchronized void notifyKillStateEventOccurred(@Nonnull final KillStateEvent killStateEvent) {
		for(final CriticalDatabaseStateListener listener : this.listeners.getListeners(CriticalDatabaseStateListener.class)) {
			listener.killStateReached(Objects.requireNonNull(killStateEvent, "KillStateEvent must not be null."));
		}
	}
	
	/**
	 * Notifies the listeners that a system reset is required
	 * killStateEvent KillStateEvent including the error message
	 */
	protected synchronized void notifyFinalErrorOccurred(@Nonnull final KillStateEvent killStateEvent) {
		for(final CriticalDatabaseStateListener listener : this.listeners.getListeners(CriticalDatabaseStateListener.class)) {
			listener.finalErrorReached(Objects.requireNonNull(killStateEvent, "KillStateEvent must not be null."));
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