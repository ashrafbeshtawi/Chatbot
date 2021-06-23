package de.dailab.oven.database.backup.threads;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.event.EventListenerList;

import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Statement;
import org.neo4j.driver.v1.StatementResult;

import de.dailab.oven.database.backup.BackupConfiguration;
import de.dailab.oven.database.backup.events.CreateBackupEvent;
import de.dailab.oven.database.backup.events.DatabaseConnectionEvent;
import de.dailab.oven.database.backup.listeners.DatabaseListener;
import de.dailab.oven.database.configuration.Graph;
import de.dailab.oven.database.exceptions.DatabaseException;

/**
 * Thread that executes a query to create a copy of the given graph as GraphML file.<br>
 * Designed to work with DatabaseListener, CreateBackupEvent and WaitEvent.
 * @author Tristan Schroer
 * @since 05.02.2020
 */
public class BackupFileCreatorThread extends Thread{

	@Nonnull
	private final EventListenerList listeners = new EventListenerList();
	@Nonnull
	private static final BackupConfiguration BACKUP_CONFIGURATION = BackupConfiguration.getInstance();
	@Nullable
	private final Graph graph;
	private final int sourceStatusCode;
	
	/**
	 * Initializes the thread querying the graph.
	 * @param graph				The graph to copy as GraphML file
	 * @param sourceStatusCode	The sources status code
	 */
	public BackupFileCreatorThread(@Nonnull final Graph graph, final int sourceStatusCode) {
		this.graph = Objects.requireNonNull(graph, "Graph must not be NULL");
		this.sourceStatusCode = sourceStatusCode;
	}
	
	/**
	 * Tries to execute the query for extracting the graph and notifies listeners in case of success,
	 * database failure or an external failure
	 */
	@Override
	public void run() {
		try (final Session readSession = this.graph.openReadSession()){
			
			final Statement statement = new Statement("CALL apoc.export.graphml.all('"
									+ BACKUP_CONFIGURATION.getCurrentBackupName() 
									+ "', {useTypes: true})");
			//Statement result needs to be interpreted to actually make a failure obvious for the thread
			final StatementResult result = readSession.run(statement);
			result.list();
			//Notify listeners about successful executing the query
			notifyBackupQueryExecuted(new CreateBackupEvent(this, this.sourceStatusCode));
			
		} catch (final DatabaseException dE) {
			//Notify listeners that database connection has been lost
			notifyConnectionLost(new DatabaseConnectionEvent(this, this.graph, this.sourceStatusCode));		
			
		} catch (final Exception e) {
			//Notify listeners that query failed to execute correctly
			notifyBackupQueryFailed(new CreateBackupEvent (this, this.sourceStatusCode));
		}
	}
	
	/**
	 * Notifies listeners that connection to database is lost
	 * @param databaseConnectionEvent DatabaseConnectionEvent including the graph instance and the sources status code
	 */
	protected synchronized void notifyConnectionLost(@Nonnull final DatabaseConnectionEvent databaseConnectionEvent) {
		for(final DatabaseListener listener : this.listeners.getListeners(DatabaseListener.class)) {
			listener.connectionToDatabaseFailed(Objects.requireNonNull(databaseConnectionEvent, "DatabaseConnectionEvent must not be null."));
		}
	}

	/**
	 * Notifies listeners that query has been executed
	 * @param createBackupEvent CreateBackupEvent including the sources status code
	 */
	protected synchronized void notifyBackupQueryExecuted(@Nonnull final CreateBackupEvent createBackupEvent) {
		for(final DatabaseListener listener : this.listeners.getListeners(DatabaseListener.class)) {
			listener.backupQueryExecuted(Objects.requireNonNull(createBackupEvent, "CreateBackupEvent must not be null."));
		}
	}
	
	/**
	 * Notifies listeners that query could not be executed
	 * @param createBackupEvent CreateBackupEvent including the sources status code
	 */
	protected synchronized void notifyBackupQueryFailed(@Nonnull final CreateBackupEvent createBackupEvent) {
		for(final DatabaseListener listener : this.listeners.getListeners(DatabaseListener.class)) {
			listener.backupQueryFailed(Objects.requireNonNull(createBackupEvent, "CreateBackupEvent must not be null."));
		}
	}
	
	/**
	 * Add the listener for this thread
	 * @param listener DatabaseListener to add
	 */
	public void addDatabaseListener(@Nonnull final DatabaseListener listener) {
		this.listeners.add(DatabaseListener.class, Objects.requireNonNull(listener, "DatabaseListener must not be null."));
	}
	
	/**
	 * Removes the listener from the list of event listeners
	 * @param listener DatabaseListener to remove
	 */
	public void removeDatabaseListener(@Nonnull final DatabaseListener listener) {
		this.listeners.remove(DatabaseListener.class, Objects.requireNonNull(listener, "DatabaseListener must not be null."));
	}
}