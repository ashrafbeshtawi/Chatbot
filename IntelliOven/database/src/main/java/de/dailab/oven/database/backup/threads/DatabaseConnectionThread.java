package de.dailab.oven.database.backup.threads;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.event.EventListenerList;

import de.dailab.oven.database.backup.events.DatabaseConnectionEvent;
import de.dailab.oven.database.backup.listeners.DatabaseListener;
import de.dailab.oven.database.configuration.DatabaseConfiguration;
import de.dailab.oven.database.configuration.Graph;

/**
 * Thread which tries to create a graph instance to the graph defined by the database configuration.<br>
 * Implements a source status code for deriving further actions.<br>
 * Notifies DatabaseListeners about success / failure.
 * @author	Tristan Schroer
 * @since	05.02.2020
 */
public class DatabaseConnectionThread extends Thread{

	@Nonnull
	private final EventListenerList listeners = new EventListenerList();
	@Nullable
	private Graph graph;
	@Nonnull
	private final DatabaseConfiguration databaseConfiguration;
	private final int sourceStatusCode;
	
	/**
	 * Initialize the thread with the database configuration for the graph to connect to.
	 * @param databaseConfiguration	The databases configuration for accessing the graph.
	 * @param sourceStatusCode		A status code which can be passed to derive further actions later on.
	 */
	public DatabaseConnectionThread(@Nonnull final DatabaseConfiguration databaseConfiguration, final int sourceStatusCode) {
		this.databaseConfiguration = Objects.requireNonNull(databaseConfiguration, "DatabaseConfiguration must not be NULL");
		this.sourceStatusCode = sourceStatusCode;
	}
	
	/**
	 * Tries to initialize the graph instance. Notifies listeners about success / failure.
	 */
	@Override
	public void run() {
		try {
			this.graph = new Graph(this.databaseConfiguration);
			notifyConnectionSucceeded(new DatabaseConnectionEvent(this, this.graph, this.sourceStatusCode));
		} catch (final Exception e) {
			notifyConnectionFailed(new DatabaseConnectionEvent(this, null, this.sourceStatusCode));
		}
	}
	
	/**
	 * Adds the listener for this Thread.
	 * @param listener DatabaseListener to add.
	 */
	public void addDatabaseListener(@Nonnull final DatabaseListener listener) {
		this.listeners.add(DatabaseListener.class, Objects.requireNonNull(listener, "DatabaseListener must not be null."));
	}
	
	/**
	 * Removes the listener from the list of event listeners.
	 * @param listener DatabaseListener to remove.
	 */
	public void removeDatabaseListener(@Nonnull final DatabaseListener listener) {
		this.listeners.remove(DatabaseListener.class, Objects.requireNonNull(listener, "DatabaseListener must not be null."));
	}
	
	/**
	 * Notifies the listeners that connection has been established.
	 * @param databaseConnectionEvent The databaseConnectionEvent containing the source status code as well as the graph instance.
	 */
	protected synchronized void notifyConnectionSucceeded(@Nonnull final DatabaseConnectionEvent databaseConnectionEvent) {
		for(final DatabaseListener listener : this.listeners.getListeners(DatabaseListener.class)) {
			listener.connectionToDatabaseSucceeded(Objects.requireNonNull(databaseConnectionEvent, "DatabaseConnectionEvent must not be null."));
		}
	}
	
	/**
	 * Notifies the listeners that connection could not be established.
	 * @param databaseConnectionEvent The databaseConnectionEvent containing the source status code as well as the graph instance.
	 */
	protected synchronized void notifyConnectionFailed(@Nonnull final DatabaseConnectionEvent databaseConnectionEvent) {
		for(final DatabaseListener listener : this.listeners.getListeners(DatabaseListener.class)) {
			listener.connectionToDatabaseFailed(Objects.requireNonNull(databaseConnectionEvent, "DatabaseConnectionEvent must not be null."));
		}
	}
}
