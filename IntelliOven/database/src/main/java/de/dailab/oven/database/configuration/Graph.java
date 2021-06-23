package de.dailab.oven.database.configuration;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.neo4j.driver.v1.AccessMode;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;

import de.dailab.oven.database.exceptions.DatabaseException;

import java.util.logging.*;

public class Graph {

    @Nullable
	private Driver graphDriver;
    
    @Nonnull
    private final DatabaseConfiguration databaseConfiguration;
    
    private static final Logger LOGGER = Logger.getLogger(Graph.class.getName());
    
    /**
     * Connects to the graph defined by passed DatabaseConfiguration
     * @param configuration configuration which shall be used for connection
     * @throws DatabaseException Exception which is thrown in case connection can not be established
     */
	public Graph(@Nonnull final DatabaseConfiguration configuration) throws DatabaseException {
        Objects.requireNonNull(configuration, "Parameter 'configuration' must not be null");
		this.databaseConfiguration = configuration;
        this.graphDriver = GraphDatabase.driver(configuration.getUri(), configuration.getAuthToken());
        ensureConnection(5);
    }

	/**
	 * Opens a write session for the graph. 
	 * Sessions should encapsulated in a try-Block, yet they can't be passed to other functions/classes this way.
	 * @return write session; null in case establishing failed
	 * @throws DatabaseException Exception which is thrown in case connection can not be established
	 */
	@Nullable
	public Session openWriteSession() throws DatabaseException {
		ensureConnection(5);
		return this.graphDriver.session(AccessMode.WRITE);
	}
	
	/**
	 * Opens a read session for the graph. 
	 * Sessions should encapsulated in a try-Block, yet they can't be passed to other functions/classes this way.
	 * @return read session; null in case establishing failed
	 * @throws DatabaseException Exception which is thrown in case connection can not be established
	 */
	@Nullable
	public Session openReadSession() throws DatabaseException {
		ensureConnection(5);
        return this.graphDriver.session(AccessMode.READ);
	}
	
	/**
	 * Closes the connection to the graph if possible.
	 */
    public void close(){
    	if(this.graphDriver != null) {
    		try {
    			this.graphDriver.close();
    		} catch (final Exception e) {
    			LOGGER.log(Level.WARNING, e.getMessage());
    		}    		
    	}
    	else {
    		LOGGER.log(Level.INFO, "There is no connection which can be closed.");
    	}
    }
    
    /**
     * Ensures that there is a connection to the graph.
     * @param trials Number of trails for a reconnection
     * @throws DatabaseException Exception which is thrown in case of connection can not be established
     */
    private void ensureConnection(final int trials) throws DatabaseException {
    	if(trials <= 0) {
    		throw new DatabaseException("Connection could not be established");
    	}
    	try {
    		this.graphDriver.session();
    	} catch (final IllegalStateException iE) {
    		this.graphDriver = GraphDatabase.driver(this.databaseConfiguration.getUri(), this.databaseConfiguration.getAuthToken());
    		LOGGER.log(Level.INFO, "Number of trials for establishing connection left: ".concat(Integer.toString(trials)));
    		ensureConnection(trials -1);
    	}
    }
}
