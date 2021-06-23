package de.dailab.oven.database.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Test;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Statement;
import org.neo4j.driver.v1.StatementResult;

import de.dailab.oven.database.AbstractDatabaseTest;
import de.dailab.oven.database.exceptions.ConfigurationException;
import de.dailab.oven.database.exceptions.DatabaseException;
import de.dailab.oven.model.database.NodeLabel;

public class IdQueryTest extends AbstractDatabaseTest {
	
	private IdQuery idQuery;
	
	@Override
	public void initialize()
            throws DatabaseException, ConfigurationException {
		this.idQuery = new IdQuery(this.getGraph());
	}

	@After
	public void close() {
		if(this.idQuery != null) {
			this.idQuery.close();			
		}
	}
	
	@Test
	public void getNodeLabelsForIdTest() {
		List<String> labels = new ArrayList<String>();
		long maxId = Long.MAX_VALUE;
		
		//Valid but not used ID
		labels = this.idQuery.getNodeLabelsForId(maxId);
		assertTrue(labels.isEmpty());
		
		//Invalid ID
		labels = this.idQuery.getNodeLabelsForId(-1l);
		assertTrue(labels.isEmpty());
		
		//Valid ID
		labels = this.idQuery.getNodeLabelsForId(1l);
		assertTrue(!labels.isEmpty());
	}

	@Test
	public void getRelationshipTypeForIdTest() {
		String label = null;
		long maxId = Long.MAX_VALUE;

		//Valid but not used ID
		label = this.idQuery.getRelationshipTypeForId(maxId);
		assertTrue(label.isEmpty());
		
		//Invalid ID
		label = this.idQuery.getRelationshipTypeForId(-1l);
		assertTrue(label.isEmpty());
		
		//Valid ID
		label = this.idQuery.getRelationshipTypeForId(1l);
		assertTrue(!label.isEmpty());
	}
	
	@Test
	public void isNodeIdValidTest() {
		long maxId = Long.MAX_VALUE;

		//Check invalid ID
		assertFalse(this.idQuery.isNodeIdValid(-1l, NodeLabel.INGREDIENT));
		
		//Check with valid ID
		try (Session readSession = this.getGraph().openReadSession()) {
			Statement statement = new Statement("MATCH(n) WHERE ID(n) = 1 RETURN labels(n) AS labels");
			StatementResult result = readSession.run(statement);
			List<Object> labelObjects = result.list().get(0).get("labels").asList();
			String expectedLabel = labelObjects.get(0).toString();
			NodeLabel nodeLabel = NodeLabel.UNDEF.getNodeLabel(expectedLabel);
			assertTrue(this.idQuery.isNodeIdValid(1l, nodeLabel));
			
			//Check wrong node label
			NodeLabel invalid = NodeLabel.AUTHOR;
			if(nodeLabel == NodeLabel.AUTHOR) {
				invalid = NodeLabel.DURATION;
			}
			assertFalse(this.idQuery.isNodeIdValid(1l, invalid));
		
			//Check with valid ID but unknown label
			assertFalse(this.idQuery.isNodeIdValid(1l, NodeLabel.UNDEF));
			
			//Check with empty string / NULL and not set ID
			assertTrue(this.idQuery.isNodeIdValid(maxId, NodeLabel.UNDEF));
			assertTrue(this.idQuery.isNodeIdValid(maxId, null));
			
			//Check with empty string / NULL and given ID
			assertFalse(this.idQuery.isNodeIdValid(1l, null));
		} catch(Exception e) {
			assertEquals("No errors", e.getLocalizedMessage());
		}	
	}
	
	@Test
	public void isOneNodeLabelValidTest() {
		long maxId = Long.MAX_VALUE;

		Set<NodeLabel> nullSet1		= null;
		Set<NodeLabel> nullSet2		= new HashSet<>();
		nullSet2.add(null);
		
		Set<NodeLabel> emptySet = new HashSet<>();
		
		Set<NodeLabel> invalidSet	= EnumSet.of(NodeLabel.UNDEF);
		Set<NodeLabel> validSet		= EnumSet.of(NodeLabel.INGREDIENT);
		
		//Check invalid ID
		assertFalse(this.idQuery.isOneNodeLabelValid(-1l, validSet));
		assertFalse(this.idQuery.isOneNodeLabelValid(null, validSet));
		assertTrue(this.idQuery.isOneNodeLabelValid(null, nullSet1));
		assertTrue(this.idQuery.isOneNodeLabelValid(-1l, nullSet1));
		assertTrue(this.idQuery.isOneNodeLabelValid(-1l, emptySet));
		
		//Check with valid ID
		assertFalse(this.idQuery.isOneNodeLabelValid(1l, nullSet1));
		assertFalse(this.idQuery.isOneNodeLabelValid(1l, emptySet));
		assertTrue(this.idQuery.isOneNodeLabelValid(maxId, nullSet1));
		assertTrue(this.idQuery.isOneNodeLabelValid(maxId, nullSet2));
		assertTrue(this.idQuery.isOneNodeLabelValid(maxId, emptySet));
		assertTrue(this.idQuery.isOneNodeLabelValid(maxId, invalidSet));
		assertFalse(this.idQuery.isOneNodeLabelValid(maxId, validSet));
		
		try (Session readSession = this.getGraph().openReadSession()) {
			Statement statement = new Statement("MATCH(n) WHERE ID(n) = 1 RETURN labels(n) AS labels");
			StatementResult result = readSession.run(statement);
			List<Object> labelObjects = result.list().get(0).get("labels").asList();
			String expectedLabel = labelObjects.get(0).toString();
			NodeLabel nodeLabel = NodeLabel.UNDEF.getNodeLabel(expectedLabel);
			
			//Check valid label
			validSet.add(nodeLabel);
			
			assertTrue(this.idQuery.isOneNodeLabelValid(1l, validSet));
		
		} catch(Exception e) {
			assertEquals("No errors", e.getLocalizedMessage());
		}	
	}
	
	@Test
	public void resetGraphTest() {
		assertTrue(this.idQuery.getGraph() != null);
		this.idQuery.setGraph(null);
		assertEquals(null, this.idQuery.getGraph());
		this.idQuery.setGraph(this.getGraph());
		assertEquals(this.getGraph(), this.idQuery.getGraph());
	}
	
	@Test
	public void nullTest() {
		IdQuery nullQuery = new IdQuery(null);
		assertEquals(null, nullQuery.getGraph());
		assertTrue(nullQuery.getNodeLabelsForId(1l).isEmpty());
		assertTrue(nullQuery.getRelationshipTypeForId(1l).isEmpty());
		assertTrue(nullQuery.isNodeIdValid(1l, NodeLabel.UNDEF));
	}
}
