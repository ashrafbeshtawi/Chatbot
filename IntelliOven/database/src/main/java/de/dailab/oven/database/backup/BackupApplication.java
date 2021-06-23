package de.dailab.oven.database.backup;

import de.dailab.oven.database.backup.listeners.BackupHandler;
import de.dailab.oven.database.backup.listeners.CriticalStateObserver;

/**
 * Sample class for initializing the backup features.
 * @author Tristan Schroer
 * @since 05.02.2020
 */
public class BackupApplication {

	private static final BackupHandler B_HANDLER = BackupHandler.getInstance();
	
	/**
	 * Constructor displaying how backup features can be implemented.
	 */
	private BackupApplication() {
		//Call the startHandling() method to tell the backup handler that it should start progressing. 
		//Keep in mind that it waits 5 minutes after initializing by its own to ensure 
		//all services are started probably
		B_HANDLER.setCriticalDatabaseStateListener(CriticalStateObserver.getInstance());
		B_HANDLER.startHandling();
	}	
}