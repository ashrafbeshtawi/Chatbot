package de.dailab.oven.database.backup.listeners;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.swing.JOptionPane;

import de.dailab.oven.database.backup.events.BackupFailureEvent;
import de.dailab.oven.database.backup.events.KillStateEvent;

/**
 * Sample class for CriticalDatabaseStateListener
 * @author Tristan Schroer
 * @since 04.02.2020
 */
public class CriticalStateObserver implements CriticalDatabaseStateListener{

	private boolean backupFailureDisplayed = false;
	
	private static CriticalStateObserver instance;
	
	//Empty constructor
	private CriticalStateObserver() {}
	
	/**
	 * @return The synchronized instance of this class
	 */
    public static CriticalStateObserver getInstance() {
        synchronized (CriticalStateObserver.class) {
            if (instance == null) {
                instance = new CriticalStateObserver();
            }
        }
        return instance;
    }
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void killStateReached(final KillStateEvent killStateEvent) {
		Objects.requireNonNull(killStateEvent, "KillStateEvent must not be NULL");
		//Ensure it is the right error code for this failure
		if(killStateEvent.getErrorMessage().contentEquals(KillStateEvent.ESTABLISHING_CONNECTION_FAILED) || 
				killStateEvent.getErrorMessage().contentEquals(KillStateEvent.NO_CONNECTION_NO_BACKUP)) {
			//Get confirmation to delete database and backup files
//			int confirmation = JOptionPane.showConfirmDialog(null, killStateEvent.getErrorMessage(), 
//											"Critical Failure", JOptionPane.YES_NO_OPTION, 
//											JOptionPane.ERROR_MESSAGE);
//			
//			//Chose different options based on confirmation
//			switch(confirmation) {
//			
//			//No confirmation given (-1 == canceled; 1 == no)
//			//Do nothing and do not try to call backup handler
//			case -1:
//			case  1:
//				break;
//				
//			//Confirmation given, call backup handler to clean the database and delete backup files. (0 == yes)
//			case 0:
//				BackupHandler.getInstance().cleanDatabase();
//				break;
//				
//			//Do nothing if an invalid integer is sent from JOptionPane
//			default:
//				break;
//			}			
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void failedToCreateBackup(@Nonnull final BackupFailureEvent backupFailureEvent) {
		Objects.requireNonNull(backupFailureEvent, "BackupFailureEvent must not be NULL");
		//If the error occurred for the first time, display it - hide it otherwise
		if(!this.backupFailureDisplayed) {
//			JOptionPane.showMessageDialog(null, backupFailureEvent.getErrorMessage(), "Backup Failure", JOptionPane.ERROR_MESSAGE);
			this.backupFailureDisplayed = true;			
		}
		//Continue handling to get possible kill states
		BackupHandler.getInstance().startHandling();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void finalErrorReached(@Nonnull final KillStateEvent killStateEvent) {
		Objects.requireNonNull(killStateEvent, "KillStateEvent must not be NULL");
		//Ensure it is the right error message for this failure
		if(killStateEvent.getErrorMessage().contentEquals(KillStateEvent.SYSTEM_RESET_REQUIRED)) {
			//If this error occurred notify the user about the broken system
//			JOptionPane.showMessageDialog(null, killStateEvent.getErrorMessage(), "Fatal system error", 
//					JOptionPane.ERROR_MESSAGE);			
		}
	}
}