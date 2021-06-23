package de.dailab.oven.database.backup;

import static org.junit.Assert.*;

import org.junit.Test;
import de.dailab.oven.database.backup.events.BackupFailureEvent;
import de.dailab.oven.database.backup.events.KillStateEvent;
import de.dailab.oven.database.backup.listeners.CriticalStateObserver;

public class CriticalStateObserverTest {

	private static final CriticalStateObserver CRITICAL_STATE_OBSERVER = CriticalStateObserver.getInstance();
	
	/**
	 * Test correct working of CriticalStateObserver
	 * <strong>IMPORTANT</strong>: Test this manually (uncomment the test).<br>
	 * First: This test opens JOptionPanes.<br>
	 * Second: Confirmation can end up in deleting backup files and database! 
	 */
//	@Test
	public void criticalStateObserverTest() {
		
		//Test killStateReached() function
		CRITICAL_STATE_OBSERVER.killStateReached(new KillStateEvent(this, KillStateEvent.ESTABLISHING_CONNECTION_FAILED));
		CRITICAL_STATE_OBSERVER.killStateReached(new KillStateEvent(this, KillStateEvent.NO_CONNECTION_NO_BACKUP));

		//Test failedToCreateBackup() function
		CRITICAL_STATE_OBSERVER.failedToCreateBackup(new BackupFailureEvent(this));
		//JOptionPane should just be shown once
		CRITICAL_STATE_OBSERVER.failedToCreateBackup(new BackupFailureEvent(this));
		
		//Test finalErrorReached() function
		CRITICAL_STATE_OBSERVER.finalErrorReached(new KillStateEvent(this, KillStateEvent.SYSTEM_RESET_REQUIRED));
		
		//Test with wrong error codes (no JOptionPane should be shown)
		CRITICAL_STATE_OBSERVER.killStateReached(new KillStateEvent(this, KillStateEvent.SYSTEM_RESET_REQUIRED));
		CRITICAL_STATE_OBSERVER.finalErrorReached(new KillStateEvent(this, KillStateEvent.ESTABLISHING_CONNECTION_FAILED));
		
		//Ensure observer is not working with NULL as event parameters
		try {
			CRITICAL_STATE_OBSERVER.failedToCreateBackup(null);
			assertFalse(true);
		} catch (final Exception e) {
			assertEquals(NullPointerException.class, e.getClass());
		}
		try {
			CRITICAL_STATE_OBSERVER.killStateReached(null);			
			assertFalse(true);
		} catch (final Exception e) {
			assertEquals(NullPointerException.class, e.getClass());
		}
		try {
			CRITICAL_STATE_OBSERVER.finalErrorReached(null);
			assertFalse(true);
		} catch (final Exception e) {
			assertEquals(NullPointerException.class, e.getClass());
		}
	}
}