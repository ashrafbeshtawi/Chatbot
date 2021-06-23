package de.dailab.oven.database.backup.events;

import java.util.EventObject;
import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * Event which can be fired within a process where a critical database error occurred.<br>
 * Designed to work with KillStateThread and CriticalDatabaseStateListener
 * @author Tristan Schroer
 * @since 05.02.2020
 */
public class KillStateEvent extends EventObject{


	//Serialization ID for correct synchronizing 
	private static final long serialVersionUID = -7317321110453296503L;
	@Nonnull
	public static final String ESTABLISHING_CONNECTION_FAILED = "Establishing connection to database "
			+ "failed finally. If this happens for the first time, try to reboot the computer, "
			+ "else confirm full database clean up.";
	
	@Nonnull
	public static final String NO_CONNECTION_NO_BACKUP = "Establishing connection to database failed. "
			+ "No backup has been found for restoring data. If this happens for the first time, try "
			+ "to reboot the computer, else confirm full database clean up.";
	
	@Nonnull
	public static final String SYSTEM_RESET_REQUIRED = "Reseting the database did not help to "
			+ "make the database work again. Please reset the whole system.";
	
	@Nonnull
	private final String errorMessage;
	 
	/**
	  * Event which can be fired within a process where connection to the database 
	  * failed and / or backing up was not successful.
	  * @param source		The source which fires the event.
	  * @param errorMessage	The error message which shall be accessible for the listeners
	  */
	public KillStateEvent(@Nonnull final Object source, @Nonnull final String errorMessage) {
		super(Objects.requireNonNull(source, "Source must not be NULL"));
		this.errorMessage = Objects.requireNonNull(errorMessage, "Error message must not be NULL.");
	}
	
	/**
	 * @return The error message derived by the source which fired the event.
	 */
	@Nonnull
	public String getErrorMessage() {
		return this.errorMessage;
	}
	
}
