package de.dailab.oven.database.backup.events;

import java.util.EventObject;
import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * Event which can be fired in case an error occurred during creating a backup.<br>
 * Designed to work with BackupFailureThread and CriticalDatabaseStateListener.
 * @author Tristan Schroer
 * @since 05.02.2020
 */
public class BackupFailureEvent extends EventObject{

	//Serialization ID for correct synchronizing 
	private static final long serialVersionUID = 2516938702422539516L;

	/**
	 * Initialize the event with the source in case creating a backup failed.
	 * @param source The source which fires the event.
	 */
	public BackupFailureEvent(@Nonnull final Object source) {
		super(Objects.requireNonNull(source, "Source must not be NULL"));
	}
	
	/**
	 * Returns a predefined error message.
	 * @return "Failed to create backup. Graph is still running"
	 */
	@Nonnull
	public String getErrorMessage() {
		return "Failed to create backup. Graph is still running";
	}	
}