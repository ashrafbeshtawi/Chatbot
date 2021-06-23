package de.dailab.oven.database.backup.events;

import java.util.EventObject;
import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * Backup file number revisor event which can be fired when the number of files has been calculated as a code.<br>
 * Designed to work with DatabaseListener and BackupFileNumberRevisorThread.
 * @author Tristan Schroer
 * @since 04.02.2020
 */
public class BackupFileNumberRevisorEvent extends EventObject{

	//Serialization ID for correct synchronizing 
	private static final long serialVersionUID = -1507550922446723757L;
	private final int numberOfBackupFilesCode;
	private final int sourceStatusCode;
	
	/**
	 * Event which can be fired within a process where a number code is generated which gives an evidence about the file numbers.
	 * @param source					The source which fires the event.
	 * @param numberOfBackupFilesCode	The calculated code based on revision of the backup files number.
	 * @param sourceStatusCode			The sources status code: A code that be transferred throughout a calling process.
	 */
	public BackupFileNumberRevisorEvent(@Nonnull final Object source, final int numberOfBackupFilesCode, final int sourceStatusCode) {
		super(Objects.requireNonNull(source, "Source must not be NULL"));
		this.numberOfBackupFilesCode = numberOfBackupFilesCode;
		this.sourceStatusCode = sourceStatusCode;
	}
	
	/**
	 * @return The calculated numberCode.
	 */
	public int getNumberOfBackupFilesCode() {
		return this.numberOfBackupFilesCode;
	}
	
	/**
	 * @return The sources status code: A code that be transferred throughout a calling process.<br>
	 * <i>Example: DatabaseListener -> BackupFileNumberRevisorThread -> BackuFileNumberRevisorEvent -> DatabaseListener</i>
	 */
	public int getSourceStatusCode() {
		return this.sourceStatusCode;
	}	
}