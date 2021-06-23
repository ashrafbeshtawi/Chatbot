package de.dailab.oven.database.backup.events;

import java.util.EventObject;
import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * Event which can be fired within a procedure where a backup file shall be created.<br>
 * Designed to work with DatabaseListener and BackupFileCreatorThread.
 * @author Tristan Schroer
 * @since 05.02.2020
 */
public class CreateBackupEvent extends EventObject{

	//Serialization ID for correct synchronizing 
	private static final long serialVersionUID = 6666248684872495369L;
	private final int sourceStatusCode;
	
	/**
	 * Event which can be fired within backup creating event
	 * @param source 				The source which fires the event  
	 * @param sourceStatusCode		The sources status code: A code that be transferred throughout a calling process.
	 */
	public CreateBackupEvent(@Nonnull final Object source, final int sourceStatusCode) {
		super(Objects.requireNonNull(source, "Source must not be NULL"));
		this.sourceStatusCode = sourceStatusCode;
	}
	
	/**
	 * @return The sources status code: A code that be transferred throughout a calling process<br>
	 * <i>Example: DatabaseListener -> BackupFileCreatorThread -> CreateBackupEvent -> DatabaseListener</i>
	 */
	public int getSourceStatusCode() {
		return this.sourceStatusCode;
	}	
}