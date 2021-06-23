package de.dailab.oven.database.backup.events;

import java.util.EventObject;
import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * Backup file date revisor event which can be fired when the state of the given backup files has been analyzed.<br>
 * Designed to work with DatabaseListener and BackupFileDateRevisorThread.
 * @author Tristan Schroer
 * @since 04.02.2020
 */
public class BackupFileDateRevisorEvent extends EventObject{

	//Serialization ID for correct synchronizing 
	private static final long serialVersionUID = -9045987992701079138L;
	private final boolean backupFilesUpToDate;
	private final int sourceStatusCode;
	
	/**
	 * Event which can be fired within a process where a number code is generated which gives an evidence about the file numbers.
	 * @param source				The source which fires the event.
	 * @param backupFilesUpToDate	A boolean which tells if the files are up to date or not.
	 * @param sourceStatusCode		The sources status code: A code that be transferred throughout a calling process.
	 */
	public BackupFileDateRevisorEvent(@Nonnull final Object source, final boolean backupFilesUpToDate, final int sourceStatusCode) {
		super(Objects.requireNonNull(source, "Source must not be NULL"));
		this.backupFilesUpToDate = backupFilesUpToDate;
		this.sourceStatusCode = sourceStatusCode;
	}

	/**
	 * Returns a boolean which tells whether backup files are up to date or not.
	 * @return <strong>true</strong> if files are up to date.<br><strong>false</strong> if not.
	 */
	public boolean getBackupFilesUpToDate() {
		return this.backupFilesUpToDate;
	}
	
	/**
	 * @return The sources status code: A code which be transferred throughout a calling process .<br>
	 * <i>Example: DatabaseListener -> BackupFileDateRevisorThread -> BackupFileDateRevisorEvent -> DatabaseListener</i>
	 */
	public int getSourceStatusCode() {
		return this.sourceStatusCode;
	}	
}