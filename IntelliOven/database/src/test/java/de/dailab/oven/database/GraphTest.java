package de.dailab.oven.database;

import static org.junit.Assert.*;

import javax.annotation.Nonnull;

import org.junit.Before;
import org.junit.Test;

import de.dailab.oven.database.configuration.ConfigurationLoader;
import de.dailab.oven.database.configuration.DatabaseConfiguration;
import de.dailab.oven.database.configuration.Graph;
import de.dailab.oven.database.exceptions.DatabaseException;

import org.neo4j.driver.v1.exceptions.AuthenticationException;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.driver.v1.exceptions.ServiceUnavailableException;

public class GraphTest {
	
	private Graph testGraph;
	
	private ConfigurationLoader configLoader;
	
	private String uri;
	private String user;
	private String pw;
	
	/**
	 * Ensure that environment variables point to a reachable database and are correct
	 */
	@Before
	public void initialize() {
		this.configLoader = new ConfigurationLoader();
		this.uri = this.configLoader.getUri();
		this.user = this.configLoader.getUser();
		this.pw = this.configLoader.getPw();
	}
	
	@Test
	public void nullUriGraphTest() {
		try {
			this.testGraph = new Graph(new DatabaseConfiguration(null, this.user, this.pw));
		} catch (final Exception e) {
			if(e.getClass().equals(ClientException.class)) {
				nullUriGraphTest();
			}
			else {
				assertEquals(NullPointerException.class, e.getClass());									
			}
		}
	}
	
	@Test
	public void nullUserGraphTest() {
		try {
			this.testGraph = new Graph(new DatabaseConfiguration(this.uri, null, this.pw));
		} catch (final Exception e) {
			if(e.getClass().equals(ClientException.class)) {
				nullUserGraphTest();
			}
			else {
				assertEquals(NullPointerException.class, e.getClass());					
			}
		}
	}
	
	@Test
	public void nullPasswordGraphTest() {
		try {
			this.testGraph = new Graph(new DatabaseConfiguration(this.uri, this.user, null));
		} catch (final Exception e) {
			if(e.getClass().equals(ClientException.class)) {
				nullPasswordGraphTest();
			}
			else {
				assertEquals(NullPointerException.class, e.getClass());									
			}
		}
	}
	
	@Test
	public void emptyUriGraphTest() {
		try {
			this.testGraph = new Graph(new DatabaseConfiguration("", this.user, this.pw));
		} catch (final Exception e) {
			if(e.getClass().equals(ClientException.class)) {
				emptyUriGraphTest();
			}
			else {
				assertEquals(IllegalArgumentException.class, e.getClass());									
			}
		}
	}
	
	@Test
	public void emptyUserGraphTest() {
		try {
			this.testGraph = new Graph(new DatabaseConfiguration(this.uri, "", this.pw));
		} catch (final Exception e) {
			if(e.getClass().equals(ClientException.class)) {
				emptyUserGraphTest();
			}
			else {
				boolean catched = false;
				if(e.getClass().equals(AuthenticationException.class) || e.getClass().equals(IllegalArgumentException.class)) {
					catched = true;
				}
				assertEquals(true, catched);								
			}
		}
	}
	
	@Test
	public void emptyPasswordGraphTest() {
		try {
			this.testGraph = new Graph(new DatabaseConfiguration(this.uri, this.user, ""));
		} catch (final Exception e) {
			if(e.getClass().equals(ClientException.class)) {
				emptyPasswordGraphTest();
			}
			
			else {
				boolean catched = false;
				if(e.getClass().equals(AuthenticationException.class) || e.getClass().equals(IllegalArgumentException.class)) {
					catched = true;
				}
				assertEquals(true, catched);					
			}
		}
	}
	
	@Test
	public void falseUriGraphTest() {
		try {
			this.testGraph = new Graph(new DatabaseConfiguration("bolt://255.255.255.255:7688", this.user, this.pw));
		} catch (final Exception e) {
			if(e.getClass().equals(ClientException.class)) {
				falseUriGraphTest();
			}
			else {
				boolean catched = false;
				if(e.getClass().equals(ServiceUnavailableException.class) || e.getClass().equals(IllegalArgumentException.class)) {
					catched = true;
				}
				assertEquals(true, catched);
			}
		}
	}
	
	@Test
	public void falseUserGraphTest() {
		try {
			this.testGraph = new Graph(new DatabaseConfiguration(this.uri, "test", this.pw));
		} catch (final Exception e) {
			if(e.getClass().equals(ClientException.class)) {
				falseUserGraphTest();
			}
			else {
				boolean catched = false;
				if(e.getClass().equals(AuthenticationException.class) || e.getClass().equals(IllegalArgumentException.class)) {
					catched = true;
				}
				assertEquals(true, catched);									
			}
		}
	}
	
	@Test
	public void falsePasswordGraphTest() {
		try {
			this.testGraph = new Graph(new DatabaseConfiguration(this.uri, this.user, "any"));
		} catch (final Exception e) {
			if(e.getClass().equals(ClientException.class)) {
				falsePasswordGraphTest();
			} 
			else {
				boolean catched = false;
				if(e.getClass().equals(AuthenticationException.class) || e.getClass().equals(IllegalArgumentException.class)) {
					catched = true;
				}
				assertEquals(true, catched);									
			}
		}
	}
	
	@Test
	public void ensureConnectionGraphTest() {
		try {
			this.testGraph = new Graph(new DatabaseConfiguration(this.uri, this.user, this.pw));
			this.testGraph.openWriteSession();
			this.testGraph.close();
			this.testGraph.openReadSession();
		} catch (final Exception e) {
			boolean catched = false;
			if(e.getClass().equals(DatabaseException.class) || e.getClass().equals(IllegalArgumentException.class)) {
				catched = true;
			}
			assertEquals(true, catched);		
		}
	}
	
}
