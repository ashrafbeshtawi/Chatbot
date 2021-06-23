package de.dailab.oven.database.backup.listeners;

import java.util.EventListener;
import javax.annotation.Nonnull;

import de.dailab.oven.database.backup.events.BackupFailureEvent;
import de.dailab.oven.database.backup.events.KillStateEvent;

/**
 * Listener for failures regarding to backup or critical database failures
 * @author Tristan Schroer
 * @since 04.02.2020
 */
public interface CriticalDatabaseStateListener extends EventListener{
	
	/**
	 * Procedure which is called when a kill state event occurred.
	 * @param killStateEvent	The kill state event including the error message. 
	 */
	void killStateReached(@Nonnull KillStateEvent killStateEvent);
	
	/**
	 * Procedure which is called when a backup file has not been able to create.
	 * @param backupFailureEvent	The backup failure event including the error message.
	 */
	void failedToCreateBackup(@Nonnull BackupFailureEvent backupFailureEvent);
	
	/**
	 * Procedure which is called when system is broken (database can not be initialized anymore)
	 * @param killStateEvent	The kill state event including the error message.
	 */
	void finalErrorReached(@Nonnull KillStateEvent killStateEvent);
}