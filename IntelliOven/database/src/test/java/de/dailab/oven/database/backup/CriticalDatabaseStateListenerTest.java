package de.dailab.oven.database.backup;

import static org.junit.Assert.*;

import org.junit.Test;
import de.dailab.oven.database.backup.events.BackupFailureEvent;
import de.dailab.oven.database.backup.events.KillStateEvent;
import de.dailab.oven.database.backup.listeners.CriticalDatabaseStateListener;
import de.dailab.oven.database.backup.threads.BackupFailureThread;
import de.dailab.oven.database.backup.threads.KillStateThread;

public class CriticalDatabaseStateListenerTest {
	
	/**
	 * Test CriticalDatabaseStateListener including KillStateThread and BackupFailureThread
	 */
	@Test
	public void criticalDatabaseStateListenerTest() {
		
		//Initialize TestListener
		class TestListener implements CriticalDatabaseStateListener {
			
			//Boolean to test whether the listener has been notified or not
			private boolean notified = false;
			
			private TestListener() {}
			
			public void resetNotified () {
				this.notified = false;
			}
			
			public boolean getNotified() {
				return this.notified;
			}
			
			@Override
			public void killStateReached(final KillStateEvent killStateEvent) {
				assertTrue((KillStateEvent.ESTABLISHING_CONNECTION_FAILED.contentEquals(killStateEvent.getErrorMessage()) || KillStateEvent.NO_CONNECTION_NO_BACKUP.contentEquals(killStateEvent.getErrorMessage())));
				assertEquals(KillStateThread.class, killStateEvent.getSource().getClass());
				this.notified = true;
			}

			@Override
			public void failedToCreateBackup(final BackupFailureEvent backupFailureEvent) {
				assertTrue("Failed to create backup. Graph is still running".contentEquals(backupFailureEvent.getErrorMessage()));
				assertEquals(BackupFailureThread.class, backupFailureEvent.getSource().getClass());
				this.notified = true;
			}

			@Override
			public void finalErrorReached(final KillStateEvent killStateEvent) {
				assertTrue(KillStateEvent.SYSTEM_RESET_REQUIRED.contentEquals(killStateEvent.getErrorMessage()));
				assertEquals(KillStateThread.class, killStateEvent.getSource().getClass());
				this.notified = true;
			}
			
		}
		
		final TestListener testListener = new TestListener();
		
		KillStateThread killStateThread; 
		final BackupFailureThread backupFailureThread;
		
		//Test with error code KillStateThread.ESTABLISHING_CONNECTION_FAILED (listener should be notified on killStateReached()
		killStateThread = new KillStateThread(KillStateThread.ESTABLISHING_CONNECTION_FAILED);
		killStateThread.addCriticalDatabaseStateListener(testListener);
		killStateThread.start();
		//Wait for the thread to finish
		while(killStateThread.isAlive()) {}
		//Test if testListener has been notified
		assertTrue(testListener.getNotified());
		testListener.resetNotified();
		
		//Test with error code KillStateThread.NO_CONNECTION_NO_BACKUP (listener should be notified on killStateReached()
		killStateThread = new KillStateThread(KillStateThread.NO_CONNECTION_NO_BACKUP);
		killStateThread.addCriticalDatabaseStateListener(testListener);
		killStateThread.start();
		//Wait for the thread to finish
		while(killStateThread.isAlive()) {}
		//Test if testListener has been notified
		assertTrue(testListener.getNotified());
		testListener.resetNotified();
		
		//Test with error code KillStateThread.SYSTEM_RESET_REQUIRED (listener should be notified on finalErrorReached()
		killStateThread = new KillStateThread(KillStateThread.SYSTEM_RESET_REQUIRED);
		killStateThread.addCriticalDatabaseStateListener(testListener);
		killStateThread.start();
		//Wait for the thread to finish
		while(killStateThread.isAlive()) {}
		//Test if testListener has been notified
		assertTrue(testListener.getNotified());
		testListener.resetNotified();
		
		//Test with error code, KillSateThread should not pass anywhere, meaning, listener should not be notified
		killStateThread = new KillStateThread(404);
		killStateThread.addCriticalDatabaseStateListener(testListener);
		killStateThread.start();
		//Wait for the thread to finish
		while(killStateThread.isAlive()) {}
		//Test if testListener has been notified
		assertFalse(testListener.getNotified());
		testListener.resetNotified();

		//BackupFailureThread can be initialized without parameters. Listener should be notified on failedToCreateBackup()
		backupFailureThread = new BackupFailureThread();
		backupFailureThread.addCriticalDatabaseStateListener(testListener);
		backupFailureThread.start();
		//Wait for the thread to finish
		while(backupFailureThread.isAlive()) {}
		//Test if testListener has been notified
		assertTrue(testListener.getNotified());
		testListener.resetNotified();
	}
}