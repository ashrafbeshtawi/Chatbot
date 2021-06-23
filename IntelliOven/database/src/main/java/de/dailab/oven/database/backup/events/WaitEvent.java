package de.dailab.oven.database.backup.events;

import java.time.LocalDateTime;
import java.util.EventObject;
import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * Generic wait event designed which can be fired within a waiting procedure.<br>
 * Designed to work with DatabaseListener and DatabaseWaitThread.
 * @author Tristan Schroer
 * @since 04.02.2020
 */
public class WaitEvent extends EventObject{

	//Serialization ID for correct synchronizing 
	private static final long serialVersionUID = -2937834361938038418L;
	private final long milliSecondsToWait;
	private final int sourceStatusCode;
	@Nonnull
	private final LocalDateTime expirationTime;
	
	/**
	 * Event which can be fired within a waiting process.
	 * @param source 				The source which fires the event.  
	 * @param milliSecondsToWait	The milliseconds the source tried to wait.
	 * @param sourceStatusCode		The sources status code: A code that be transferred throughout a calling process.
	 * @param ExpirationTime		The time when waiting will expire.
	 */
	public WaitEvent(@Nonnull final Object source, final long milliSecondsToWait, final int sourceStatusCode, @Nonnull final LocalDateTime expirationTime) {
		super(Objects.requireNonNull(source, "Source must not be NULL"));
		this.milliSecondsToWait = milliSecondsToWait;
		this.sourceStatusCode = sourceStatusCode;
		this.expirationTime = Objects.requireNonNull(expirationTime, "Expiration time must not be NULL");
	}
	
	/**
	 * @return The milliseconds the source tried to wait.
	 */
	public long getMilliSecondsToWait() {
		return this.milliSecondsToWait;
	}
	
	/**
	 * @return The sources status code: A code that be transferred throughout a calling process.<br>
	 * <i>Example: DatabaseListener -> DatabaseWaitThread -> WaitEvent -> DatabaseListener</i>
	 */
	public int getSourceStatusCode() {
		return this.sourceStatusCode;
	}
	
	/**
	 * @return The time when waiting will expire.
	 */
	@Nonnull
	public LocalDateTime getExpirationTime() {
		return this.expirationTime;
	}
}
