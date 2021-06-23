package de.dailab.oven.database.backup.events;

import java.util.EventObject;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.dailab.oven.database.configuration.Graph;

/**
 * Event which can be fired within a process where establishing a database connection is tried.<br>
 * Designed to work with DatabaseListener and DatabaseConnectionThread.
 * @author	Tristan Schroer
 * @since	05.02.2020 	
 */
public class DatabaseConnectionEvent extends EventObject{


	//Serialization ID for correct synchronizing 
	private static final long serialVersionUID = 6693778918918699442L;
	@Nullable
	private final transient Graph graph;
	private final int sourceStatusCode;
	
	/**
	 * Event which can be fired within a process where establishing connection to the database is tried.
	 * @param source			The source which fires the event.
	 * @param graph				The graphs instance where the connection has been tried to establish too.
	 * @param sourceStatusCode	The sources status code: A code that be transferred throughout a calling process.
	 */
	public DatabaseConnectionEvent(@Nonnull final Object source, @Nullable final Graph graph, final int sourceStatusCode) {
		super(Objects.requireNonNull(source, "Source must not be NULL"));
		this.graph = graph;
		this.sourceStatusCode = sourceStatusCode;
	}
	
	/**
	 * @return The graphs instance which can be used for querying the graph.<br><strong>null</strong> if connection failed.
	 */
	@Nullable
	public Graph getGraph() {
		return this.graph;
	}
	
	/**
	 * @return The sources status code: A code which be transferred throughout a calling process .<br>
	 * <i>Example: DatabaseListener -> DatabaseConnectionThread -> DatabaseConnectionEvent -> DatabaseListener</i>
	 */
	public int getSourceStatusCode() {
		return this.sourceStatusCode;
	}
}
