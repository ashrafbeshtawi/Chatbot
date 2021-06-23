package de.dailab.oven.database.backup;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Statement;
import de.dailab.oven.database.backup.events.BackupFileDateRevisorEvent;
import de.dailab.oven.database.backup.events.BackupFileNumberRevisorEvent;
import de.dailab.oven.database.backup.events.CreateBackupEvent;
import de.dailab.oven.database.backup.events.DatabaseConnectionEvent;
import de.dailab.oven.database.backup.events.WaitEvent;
import de.dailab.oven.database.backup.listeners.DatabaseListener;
import de.dailab.oven.database.backup.threads.BackupFileCreatorThread;
import de.dailab.oven.database.backup.threads.BackupFileDateRevisorThread;
import de.dailab.oven.database.backup.threads.BackupFileNumberRevisorThread;
import de.dailab.oven.database.backup.threads.DatabaseConnectionThread;
import de.dailab.oven.database.backup.threads.DatabaseWaitThread;
import de.dailab.oven.database.configuration.Configuration;
import de.dailab.oven.database.configuration.ConfigurationLoader;
import de.dailab.oven.database.configuration.DatabaseConfiguration;
import de.dailab.oven.database.configuration.Graph;
import de.dailab.oven.database.exceptions.ConfigurationException;
import de.dailab.oven.database.exceptions.DatabaseException;

public class DatabaseListenerTest {
	
	private static final int SOURCE_STATUS_CODE = 200;
	private static final ConfigurationLoader CONFIG_LOADER = new ConfigurationLoader();
	private static final Logger LOGGER = Logger.getGlobal();
	public Graph testGraph = null;
	
	boolean osAccepted = BackupConfiguration.getInstance().getOperatingSystemIsAccepted();
	private static final long TEST_EXPIRATION_DURATION = TimeUnit.MINUTES.toMillis(1);
	private static final String CURRENT_DIRECTORY = Paths.get("").toAbsolutePath().toString() + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator;
	private static final String SEP = File.separator;
	
	class TestListener implements DatabaseListener {

		//Boolean to test whether the listener has been notified or not
		private boolean notified = false;
		private boolean backupQueryExecuted = false;
		private boolean backupQueryFailed = false;
		private boolean connectionToDatabaseFailed = false;
		private boolean backupFilesUpToDate = false;
		private boolean backupFilesNotUpToDate = false;
		private boolean waitingFailed = false;
		private int expectedFileNumberCode = -1;
		
		private TestListener() {}
		
		public void resetNotified () {
			this.notified = false;
		}
		
		public boolean getNotified() {
			return this.notified;
		}
		
		public boolean getBackupQueryExecuted() {
			return this.backupQueryExecuted;
		}
		
		public boolean getBackupQueryFailed() {
			return this.backupQueryFailed;
		}
		
		public boolean getConnectionToDatabaseFailed() {
			return this.connectionToDatabaseFailed;
		}
		
		public boolean getBackupFilesUpToDate() {
			return this.backupFilesUpToDate;
		}
		
		public boolean getBackupFilesNotUpToDate() {
			return this.backupFilesNotUpToDate;
		}
		
		public boolean getWaitingFailed() {
			return this.waitingFailed;
		}
		
		public void resetUpToDate () {
			this.backupFilesUpToDate = false;
			this.backupFilesNotUpToDate = false;
		}
		
		public void setExpectedFileNumberCode(final int expectedFileNumberCode) {
			this.expectedFileNumberCode = expectedFileNumberCode;
		}
		
		public void resetConnectionToDatabaseFailed () {
			this.connectionToDatabaseFailed = false;
		}
		
		//Initialize TestListener
		@Override
		public void waitingSucceeded(final WaitEvent waitEvent) {
			assertEquals(TimeUnit.SECONDS.toMillis(5), waitEvent.getMilliSecondsToWait());
			assertTrue(LocalDateTime.now().isAfter(waitEvent.getExpirationTime()) || LocalDateTime.now().equals(waitEvent.getExpirationTime()));
			assertEquals(DatabaseWaitThread.class, waitEvent.getSource().getClass());
			assertEquals(SOURCE_STATUS_CODE, waitEvent.getSourceStatusCode());
			this.notified = true;
		}

		@Override
		public void waitingFailed(final WaitEvent waitEvent) {
			assertEquals(TimeUnit.SECONDS.toMillis(5), waitEvent.getMilliSecondsToWait());
			assertTrue(LocalDateTime.now().isBefore(waitEvent.getExpirationTime()));
			assertEquals(DatabaseWaitThread.class, waitEvent.getSource().getClass());
			assertEquals(SOURCE_STATUS_CODE, waitEvent.getSourceStatusCode());
			this.waitingFailed = true;
		}

		@Override
		public void gotBackupFileNumberCode(final BackupFileNumberRevisorEvent backupFileNumberRevisorEvent) {
			assertEquals(BackupFileNumberRevisorThread.class, backupFileNumberRevisorEvent.getSource().getClass());
			assertEquals(SOURCE_STATUS_CODE, backupFileNumberRevisorEvent.getSourceStatusCode());
			assertEquals(this.expectedFileNumberCode, backupFileNumberRevisorEvent.getNumberOfBackupFilesCode());
			this.notified = true;
		}

		@Override
		public void connectionToDatabaseSucceeded(final DatabaseConnectionEvent databaseConnectionEvent) {
			assertEquals(SOURCE_STATUS_CODE, databaseConnectionEvent.getSourceStatusCode());
			assertEquals(DatabaseConnectionThread.class, databaseConnectionEvent.getSource().getClass());
			assertTrue(databaseConnectionEvent.getGraph() != null);
	    	try {
	    		final Session test = databaseConnectionEvent.getGraph().openReadSession();
	    		test.run(new Statement("MATCH(n) RETURN count(n)"));
	    	} catch (final Exception e) {
	    		assertTrue(false);
	    	}
			this.notified = true;
		}

		@Override
		public void connectionToDatabaseFailed(final DatabaseConnectionEvent databaseConnectionEvent) {
			if(databaseConnectionEvent.getSource().getClass() == BackupFileCreatorThread.class) {
				assertEquals(DatabaseListenerTest.this.testGraph, databaseConnectionEvent.getGraph());
			}
			else {
				assertEquals(null, databaseConnectionEvent.getGraph());
			}
			assertEquals(SOURCE_STATUS_CODE, databaseConnectionEvent.getSourceStatusCode());
			assertTrue((BackupFileCreatorThread.class == databaseConnectionEvent.getSource().getClass() || DatabaseConnectionThread.class == databaseConnectionEvent.getSource().getClass()));
			this.connectionToDatabaseFailed = true;
		}

		@Override
		public void backupQueryExecuted(final CreateBackupEvent createBackupEvent) {
			assertEquals(SOURCE_STATUS_CODE, createBackupEvent.getSourceStatusCode());
			assertEquals(BackupFileCreatorThread.class, createBackupEvent.getSource().getClass());
			this.backupQueryExecuted = true;
		}

		@Override
		public void backupQueryFailed(final CreateBackupEvent createBackupEvent) {
			assertEquals(SOURCE_STATUS_CODE, createBackupEvent.getSourceStatusCode());
			assertEquals(BackupFileCreatorThread.class, createBackupEvent.getSource().getClass());
			this.backupQueryFailed = true;
		}

		@Override
		public void backupFilesUpToDate(final BackupFileDateRevisorEvent backupFileDateRevisorEvent) {
			assertEquals(SOURCE_STATUS_CODE, backupFileDateRevisorEvent.getSourceStatusCode());
			assertTrue(backupFileDateRevisorEvent.getBackupFilesUpToDate());
			assertEquals(BackupFileDateRevisorThread.class, backupFileDateRevisorEvent.getSource().getClass());
			this.backupFilesUpToDate = true;
		}

		@Override
		public void backupFilesNotUpToDate(final BackupFileDateRevisorEvent backupFileDateRevisorEvent) {
			assertEquals(SOURCE_STATUS_CODE, backupFileDateRevisorEvent.getSourceStatusCode());
			assertFalse(backupFileDateRevisorEvent.getBackupFilesUpToDate());
			assertEquals(BackupFileDateRevisorThread.class, backupFileDateRevisorEvent.getSource().getClass());
			this.backupFilesNotUpToDate = true;
		}
		
	}
	
	/**
	 * Test DatabaseListener including BackupFileCreatorThread
	 * <strong>IMPORTANT</strong>: Some tests are just executed on raspbian since neo4j.config is replaced and service restarted in between 
	 */
	@Test
	public void DatabaseListenerBackupCreatorThreadTest() {
	
		final TestListener testListener = new TestListener();
		BackupFileCreatorThread backupFileCreatorThread;
		
		//Establish graph connection if not existing
		if(this.testGraph == null) {
			try {
				if(CONFIG_LOADER.getUri().isEmpty()) {
					this.testGraph = new Graph(Configuration.getInstance().getDatabaseConfiguration());
				}
				else {
					this.testGraph = new Graph(new DatabaseConfiguration(CONFIG_LOADER.getUri(), CONFIG_LOADER.getUser(), CONFIG_LOADER.getPw()));
				}
			} catch (final DatabaseException | IllegalArgumentException e) {
				LOGGER.log(Level.INFO, "Environment variables not set. Skipped BackupFileCreatorTests needing existing graph.");
			} catch (final Exception e) {
				LOGGER.log(Level.INFO, "Graph could not be instantiated since of " + e.getClass() + ": " + e.getLocalizedMessage());
			}			
		}
		
		//Test BackupFileCreatorThread for correct notifying that query has been executed or database connection has been lost
		//Graph must be initiated correctly
		if(this.testGraph != null) {
			//Test with Apoc enabled => backupQueryExecuted()
			backupFileCreatorThread = new BackupFileCreatorThread(this.testGraph, SOURCE_STATUS_CODE);
			backupFileCreatorThread.addDatabaseListener(testListener);
			backupFileCreatorThread.start();
			while(backupFileCreatorThread.isAlive()) {}
			assertTrue(testListener.getBackupQueryExecuted());
			
			//Test with stopping Neo4J service during thread running (just on raspbian)
			if(this.osAccepted) {
				//Test with lost database connection
				try {
					backupFileCreatorThread = new BackupFileCreatorThread(this.testGraph, SOURCE_STATUS_CODE);
					backupFileCreatorThread.addDatabaseListener(testListener);
					LOGGER.log(Level.INFO, "Stopping Neo4j");
					final Process process = Runtime.getRuntime().exec("sudo service neo4j stop");
					final LocalDateTime wait = LocalDateTime.now().plusSeconds(40);
					process.waitFor();
					process.destroy();
					//Wait until the system finished the query
					while(LocalDateTime.now().isBefore(wait)) {}
					backupFileCreatorThread.start();
					while(backupFileCreatorThread.isAlive()) {}
					assertTrue(testListener.getBackupQueryFailed());
				} catch (final IOException | InterruptedException e) {
					LOGGER.log(Level.INFO, "Could not stop Neo4J service. BackupFileCreatorThread not tested with lost database connection.");
				}
			}
		}
		
		
		//Test BackupCreatorThread for correct notifying when query could not be executed (e.g. Apoc service unabled)
		if(this.osAccepted) {
			boolean testNeo4JConfLoaded = false;
			//Change to neo4j configuration which disables Apoc plugin
			final String neo4jConfiguration = SEP + "etc" + SEP + "neo4j" + SEP + "neo4j.conf";
			final String testNeo4JConfiguration = System.getProperty("user.dir") + SEP + "src" + SEP + "test" + SEP + "resources" + SEP + "testNeo4J.conf";
			final String tmpNeo4JConfiguration = System.getProperty("user.dir") + SEP + "neo4j.conf";
			
			try {
				LOGGER.log(Level.INFO, "Copying: " + neo4jConfiguration + " to: " + tmpNeo4JConfiguration);
				Process process = Runtime.getRuntime().exec("sudo cp " + neo4jConfiguration + " " + tmpNeo4JConfiguration);
				process.waitFor();
				LOGGER.log(Level.INFO, "Deleting: " + neo4jConfiguration);
				process = Runtime.getRuntime().exec("sudo rm -rf " + neo4jConfiguration);
				process.waitFor();
				LOGGER.log(Level.INFO, "Copying: " + testNeo4JConfiguration + " to: " + neo4jConfiguration);
				process = Runtime.getRuntime().exec("sudo cp " + testNeo4JConfiguration + " " + neo4jConfiguration);
				process.waitFor();
				LOGGER.log(Level.INFO, "Stopping Neo4j");
				process = Runtime.getRuntime().exec("sudo service neo4j stop");
				process.waitFor();
				process.destroy();
				testNeo4JConfLoaded = true;
			} catch (final IOException | InterruptedException e) {
				LOGGER.log(Level.INFO, "Could not replace neo4j.conf. BackupFileCreatorThread not tested with Apoc Plugin disabled.");
			}
			
			//Just continue if test configuration has been loaded successfully
			if(testNeo4JConfLoaded == true) {
				try {
					LocalDateTime wait = LocalDateTime.now().plusSeconds(30);
					while(LocalDateTime.now().isBefore(wait)) {}
					LOGGER.log(Level.INFO, "Starting Neo4j");
					final Process process = Runtime.getRuntime().exec("sudo service neo4j start");
					//Wait for the service to start
					wait = LocalDateTime.now().plusMinutes(2);
					while(LocalDateTime.now().isBefore(wait)) {}
					process.waitFor();
					process.destroy();
					//Reestablish connection
					try {
						if(CONFIG_LOADER.getUri().isEmpty()) {
							this.testGraph = new Graph(Configuration.getInstance().getDatabaseConfiguration());
						}
						else {
							this.testGraph = new Graph(new DatabaseConfiguration(CONFIG_LOADER.getUri(), CONFIG_LOADER.getUser(), CONFIG_LOADER.getPw()));
						}
					} catch (final DatabaseException | IllegalArgumentException e) {
						LOGGER.log(Level.INFO, "Environment variables not set. Skipped BackupFileCreatorTests needing existing graph.");
					} catch (final Exception e) {
						LOGGER.log(Level.INFO, "Graph could not be instantiated since of " + e.getClass() + ": " + e.getLocalizedMessage());
					}
					//Start Thread
					backupFileCreatorThread = new BackupFileCreatorThread(this.testGraph, SOURCE_STATUS_CODE);
					backupFileCreatorThread.addDatabaseListener(testListener);
					backupFileCreatorThread.start();
					while(backupFileCreatorThread.isAlive()) {}
					//The thread should get an exception an notify backup query failed
					assertTrue(testListener.getBackupQueryFailed());				
				} catch (final IOException | InterruptedException e) {
					LOGGER.log(Level.INFO, "Could not start neo4j service. BackupFileCreatorThread not tested with Apoc Plugin disabled.");
				}
				
				//Stop Neo4j service to restore old config
				try {
					LOGGER.log(Level.INFO, "Stopping Neo4j");
					final Process process = Runtime.getRuntime().exec("sudo service neo4j stop");
					final LocalDateTime wait = LocalDateTime.now().plusSeconds(30);
					process.waitFor();
					process.destroy();
					//Wait until the system finished the query
					while(LocalDateTime.now().isBefore(wait)) {}
				} catch (final IOException | InterruptedException e) {
					LOGGER.log(Level.INFO, "Could not stop Neo4J service. Correct neo4.conf may not be adapted (stored in user.dir/neo4j.conf).");
				}
				//Restore old configuration
				try {
					LOGGER.log(Level.INFO, "Deleting: " + neo4jConfiguration);
					Process process = Runtime.getRuntime().exec("sudo rm -rf " + neo4jConfiguration);
					process.waitFor();
					LOGGER.log(Level.INFO, "Copying: " + tmpNeo4JConfiguration + " to: " + neo4jConfiguration);
					process = Runtime.getRuntime().exec("sudo cp " + tmpNeo4JConfiguration + " " + neo4jConfiguration);
					process.waitFor();
					LOGGER.log(Level.INFO, "Deleting: " + tmpNeo4JConfiguration);
					process = Runtime.getRuntime().exec("sudo rm -rf " + tmpNeo4JConfiguration);
					process.waitFor();
					process.destroy();
				} catch (final IOException | InterruptedException e) {
					LOGGER.log(Level.INFO, "Could not restore original neo4.conf. (stored in user.dir/neo4j.conf).");
				}
				//Reestablish connection
				try {
					LOGGER.log(Level.INFO, "Starting Neo4j");
					final Process process = Runtime.getRuntime().exec("sudo service neo4j start");
					//Wait for the service to start
					final LocalDateTime wait = LocalDateTime.now().plusMinutes(2);
					while(LocalDateTime.now().isBefore(wait)) {}
					process.waitFor();
					process.destroy();
				} catch (final IOException | InterruptedException e) {
					LOGGER.log(Level.INFO, "Could not start neo4j service. BackupFileCreatorThread not tested with Apoc Plugin disabled.");
				}
				try {
					if(CONFIG_LOADER.getUri().isEmpty()) {
						this.testGraph = new Graph(Configuration.getInstance().getDatabaseConfiguration());
					}
					else {
						this.testGraph = new Graph(new DatabaseConfiguration(CONFIG_LOADER.getUri(), CONFIG_LOADER.getUser(), CONFIG_LOADER.getPw()));
					}
				} catch (final DatabaseException | IllegalArgumentException e) {
					LOGGER.log(Level.INFO, "Environment variables not set. Skipped BackupFileCreatorTests needing existing graph.");
				} catch (final Exception e) {
					LOGGER.log(Level.INFO, "Graph could not be instantiated since of " + e.getClass() + ": " + e.getLocalizedMessage());
				}
			}			
		}
	}
	
	/**
	 * Test DatabaseListener including BackupFileDateRevisorThread
	 */
	@Test 
	public void DatabaseListenerBackupFileDateRevisorTest() {
		final TestListener testListener = new TestListener();
		BackupFileDateRevisorThread backupFileDateRevisorThread;
		
	
		final List<String> testFiles = new ArrayList<>();
		//Test with empty list should notify backupFilesNotUpToDate
		backupFileDateRevisorThread = new BackupFileDateRevisorThread(testFiles, TEST_EXPIRATION_DURATION, SOURCE_STATUS_CODE);
		backupFileDateRevisorThread.addDatabaseListener(testListener);
		backupFileDateRevisorThread.start();
		while(backupFileDateRevisorThread.isAlive()) {}
		assertTrue(testListener.getBackupFilesNotUpToDate());
		testListener.resetUpToDate();
		
		//Test with one (expired file) should notify backupFilesNotUpToDate
		final String expiredFile =  CURRENT_DIRECTORY + "BackupFileDateRevisorTest.txt";
		testFiles.add(expiredFile);
		backupFileDateRevisorThread = new BackupFileDateRevisorThread(testFiles, TEST_EXPIRATION_DURATION, SOURCE_STATUS_CODE);
		backupFileDateRevisorThread.addDatabaseListener(testListener);
		backupFileDateRevisorThread.start();
		while(backupFileDateRevisorThread.isAlive()) {}
		assertTrue(testListener.getBackupFilesNotUpToDate());
		testListener.resetUpToDate();
		
		//Test with an additional not expired file, should notify backupFilesUpToDate
		try {
			final File newTestFile = new File(CURRENT_DIRECTORY + "tmpFile.txt");
			final BufferedWriter empty = new BufferedWriter(new FileWriter(newTestFile));
			empty.write("");
			empty.close();
			testFiles.add(newTestFile.getAbsolutePath());
			backupFileDateRevisorThread = new BackupFileDateRevisorThread(testFiles, TEST_EXPIRATION_DURATION, SOURCE_STATUS_CODE);
			backupFileDateRevisorThread.addDatabaseListener(testListener);
			backupFileDateRevisorThread.start();
			while(backupFileDateRevisorThread.isAlive()) {}
			assertTrue(testListener.getBackupFilesUpToDate());
			testListener.resetUpToDate();
			newTestFile.delete();	
		} catch (final IOException e) {
			LOGGER.log(Level.INFO, "Couldn't create up to date test file, hence BackupFileDateRevisorThread is not tested with up to date file");
		}
	}
	
	/**
	 * Test DatabaseListener including BackupFileNumberRevisorThread
	 */
	@Test
	public void DatabaseListenerBackupFileNumberRevisorThreadTest() {
		final TestListener testListener = new TestListener();
		BackupFileNumberRevisorThread backupFileNumberRevisorThread;
		
		//Test with null as file paths (strings) should notify gotBackupFileNumberCode() with 0 as code
		testListener.setExpectedFileNumberCode(0);
		backupFileNumberRevisorThread = new BackupFileNumberRevisorThread(null, null, SOURCE_STATUS_CODE);
		backupFileNumberRevisorThread.addDatabaseListener(testListener);
		backupFileNumberRevisorThread.start();
		while(backupFileNumberRevisorThread.isAlive()) {}
		assertTrue(testListener.getNotified());
		testListener.resetNotified();
		
		//Test with not existing file paths (strings) should notify gotBackupFileNumberCode() with 0 as code
		testListener.setExpectedFileNumberCode(0);
		backupFileNumberRevisorThread = new BackupFileNumberRevisorThread(CURRENT_DIRECTORY + "nofile.txt", CURRENT_DIRECTORY + "secondNoFile.txt", SOURCE_STATUS_CODE);
		backupFileNumberRevisorThread.addDatabaseListener(testListener);
		backupFileNumberRevisorThread.start();
		while(backupFileNumberRevisorThread.isAlive()) {}
		assertTrue(testListener.getNotified());
		testListener.resetNotified();
		
		//Test with first existing file path (string) should notify gotBackupFileNumberCode() with 1 as code
		testListener.setExpectedFileNumberCode(1);
		backupFileNumberRevisorThread = new BackupFileNumberRevisorThread(CURRENT_DIRECTORY + "BackupFileNumberRevisorTest1.txt", CURRENT_DIRECTORY + "secondNoFile.txt", SOURCE_STATUS_CODE);
		backupFileNumberRevisorThread.addDatabaseListener(testListener);
		backupFileNumberRevisorThread.start();
		while(backupFileNumberRevisorThread.isAlive()) {}
		assertTrue(testListener.getNotified());
		testListener.resetNotified();
		
		//Test with second existing file path (string) should notify gotBackupFileNumberCode() with 2 as code
		testListener.setExpectedFileNumberCode(2);
		backupFileNumberRevisorThread = new BackupFileNumberRevisorThread(CURRENT_DIRECTORY + "nofile.txt", CURRENT_DIRECTORY + "BackupFileNumberRevisorTest2.txt", SOURCE_STATUS_CODE);
		backupFileNumberRevisorThread.addDatabaseListener(testListener);
		backupFileNumberRevisorThread.start();
		while(backupFileNumberRevisorThread.isAlive()) {}
		assertTrue(testListener.getNotified());
		testListener.resetNotified();
		
		//Test with both existing file paths (strings) should notify gotBackupFileNumberCode() with 3 as code
		testListener.setExpectedFileNumberCode(3);
		backupFileNumberRevisorThread = new BackupFileNumberRevisorThread(CURRENT_DIRECTORY + "BackupFileNumberRevisorTest1.txt", CURRENT_DIRECTORY + "BackupFileNumberRevisorTest2.txt", SOURCE_STATUS_CODE);
		backupFileNumberRevisorThread.addDatabaseListener(testListener);
		backupFileNumberRevisorThread.start();
		while(backupFileNumberRevisorThread.isAlive()) {}
		assertTrue(testListener.getNotified());
		testListener.resetNotified();
				
	}
	
	/**
	 * Test DatabaseListener including DatabaseConnectionThread
	 * <strong>IMPORTANT</strong>: Some tests are just executed on raspbian since neo4j service is restarted in between 
	 */
	@Test
	public void DatabaseListenerDatabaseConnectionThreadTest() {
		final TestListener testListener = new TestListener();
		DatabaseConfiguration databaseConfiguration = null;
		if(CONFIG_LOADER.getUri().isEmpty()) {
			try {
				databaseConfiguration = Configuration.getInstance().getDatabaseConfiguration();
			} catch (final ConfigurationException e) {
				e.printStackTrace();
			}
		}
		else {
			databaseConfiguration = new DatabaseConfiguration(CONFIG_LOADER.getUri(), CONFIG_LOADER.getUser(), CONFIG_LOADER.getPw());			
		}
		DatabaseConnectionThread databaseConnectionThread;
		
		if(this.osAccepted) {
			//Test with stopping Neo4J service during thread running (just on raspbian) == lost database connection should notify connectionToDatabaseFailed
			//Test with lost database connection
			try {
				databaseConnectionThread = new DatabaseConnectionThread(databaseConfiguration, SOURCE_STATUS_CODE);
				databaseConnectionThread.addDatabaseListener(testListener);
				LOGGER.log(Level.INFO, "Stopping Neo4j");
				final Process process = Runtime.getRuntime().exec("sudo service neo4j stop");
				final LocalDateTime wait = LocalDateTime.now().plusSeconds(40);
				process.waitFor();
				process.destroy();
				//Wait until the system finished the query
				while(LocalDateTime.now().isBefore(wait)) {}
				databaseConnectionThread.start();
				while(databaseConnectionThread.isAlive()) {}
				assertTrue(testListener.getConnectionToDatabaseFailed());
				testListener.resetConnectionToDatabaseFailed();
			} catch (final IOException | InterruptedException e) {
				LOGGER.log(Level.INFO, "Could not stop/start Neo4J service. DatabaseConnectionThread not tested with lost database connection.");
			}
			
			//Test with running graph, should notify connectionToDatabaseSucceeded()
			try {
				LOGGER.log(Level.INFO, "Start Neo4j");
				final Process process = Runtime.getRuntime().exec("sudo service neo4j start");
				//Wait for the service to start
				final LocalDateTime wait = LocalDateTime.now().plusMinutes(2);
				while(LocalDateTime.now().isBefore(wait)) {}
				process.waitFor();
				process.destroy();
				//Start Thread
				databaseConnectionThread = new DatabaseConnectionThread(databaseConfiguration, SOURCE_STATUS_CODE);
				databaseConnectionThread.addDatabaseListener(testListener);
				databaseConnectionThread.start();
				while(databaseConnectionThread.isAlive()) {}
				assertTrue(testListener.getNotified());
				testListener.resetNotified();			
			} catch (final IOException | InterruptedException e) {
				LOGGER.log(Level.INFO, "Could not start neo4j service. DatabaseConnectionThread not tested with running graph.");
			}
			
		}	
	}
	
	/**
	 * Test DatabaseListener including DatabaseWaitThread
	 */
	@Test
	public void DatabaseListenerDatabaseWaitThreadTest() {
		final TestListener testListener = new TestListener ();
		DatabaseWaitThread databaseWaitThread;
		final long testSleepTime = TimeUnit.SECONDS.toMillis(5);
		
		//Test waiting without interrupt. Should notify waitingSucceeded
		databaseWaitThread = new DatabaseWaitThread(testSleepTime, SOURCE_STATUS_CODE, LocalDateTime.now().plusSeconds(5));
		databaseWaitThread.addDatabaseListener(testListener);
		databaseWaitThread.start();
		while(databaseWaitThread.isAlive()) {}
		assertTrue(testListener.getNotified() || testListener.getWaitingFailed());
		testListener.resetNotified();
		
		//Test waiting with interrupting the thread
		databaseWaitThread = new DatabaseWaitThread(testSleepTime, SOURCE_STATUS_CODE, LocalDateTime.now().plusSeconds(5));
		databaseWaitThread.addDatabaseListener(testListener);
		databaseWaitThread.start();
		databaseWaitThread.interrupt();
		while(databaseWaitThread.isAlive()) {}
		assertTrue(testListener.getWaitingFailed());	
	}
}