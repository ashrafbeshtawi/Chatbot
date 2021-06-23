package de.dailab.oven.database.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.After;
import org.junit.Test;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Statement;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Values;

import de.dailab.oven.database.AbstractDatabaseTest;
import de.dailab.oven.database.configuration.Configuration;
import de.dailab.oven.database.configuration.Graph;
import de.dailab.oven.database.exceptions.ConfigurationException;
import de.dailab.oven.database.exceptions.DatabaseException;
import de.dailab.oven.database.query.Query;
import de.dailab.oven.model.database.NodeLabel;

public class ImageQueryTest extends AbstractDatabaseTest {
	
	private Graph graph;
	private ImageQuery imageQuery;
	private String dataDirectory;
	
	private NodeLabel recipe 	= NodeLabel.RECIPE;
	private NodeLabel user 		= NodeLabel.USER;
	private NodeLabel undef		= NodeLabel.UNDEF;
			
	private static final String SEP = File.separator;
	private static final String TEST_IMAGE_SOURCE = Paths.get("").toAbsolutePath().toString() + SEP
								+ "src" + SEP + "test" + SEP + "resources" + SEP + "testImage.png";
	
	@Override
	public void initialize()
            throws DatabaseException, ConfigurationException {
		this.graph = this.getGraph();
		this.imageQuery = new ImageQuery(this.getGraph());
		this.dataDirectory = Configuration.getInstance().getProgramDataDirectory();
		new Query(this.graph);
	}

	@After
	public void close() {
		if(this.imageQuery != null) {
			this.imageQuery.close();			
		}
	}
	
	@Test
	public void resetGraphTest() {
		assertTrue(this.imageQuery.getGraph() != null);
		this.imageQuery.setGraph(null);
		assertEquals(null, this.imageQuery.getGraph());
		this.imageQuery.setGraph(this.graph);
		assertEquals(this.graph, this.imageQuery.getGraph());
	}
	
//	@Test
	public void saveRenderedImageTest() {
		
		//NULL image test
		RenderedImage nullImage = null;
		try {
			assertEquals(null, this.imageQuery.saveImage(nullImage, recipe));
		} catch (IOException e) {
			assertEquals("No errors", e.getLocalizedMessage());
		}
		
		//NULL node label test
		File validImageFile = new File(TEST_IMAGE_SOURCE);
		String filePath = "";
		try {
			RenderedImage validImage = ImageIO.read(validImageFile);
			assertTrue(validImage != null);
			filePath = this.imageQuery.saveImage(validImage, null);
			assertTrue(filePath != null);
			assertFalse(filePath.isEmpty());
			assertTrue(filePath.contains("undef-images"));
			assertTrue(new File(this.dataDirectory + filePath).exists());
			new File(this.dataDirectory + filePath).delete();
		} catch (IOException e) {
			assertEquals("No errors", e.getLocalizedMessage());
		}
		
		//Valid test
		try {
			RenderedImage validImage = ImageIO.read(validImageFile);
			assertTrue(validImage != null);
			filePath = this.imageQuery.saveImage(validImage, user);
			assertTrue(filePath != null);
			assertFalse(filePath.isEmpty());
			assertTrue(filePath.contains("user-images"));
			assertTrue(new File(this.dataDirectory + filePath).exists());
			new File(this.dataDirectory + filePath).delete();
		} catch (IOException e) {
			assertEquals("No errors", e.getLocalizedMessage());
		}
	}

//	@Test
	public void saveImageFileTest() {
		
		File validImageFile = new File(TEST_IMAGE_SOURCE);
		
		//NULL image test
		File nullImage = null;
		try {
			assertEquals(null, this.imageQuery.saveImage(nullImage, recipe));
		} catch (IOException e) {
			assertEquals("No errors", e.getLocalizedMessage());
		}
		
		//NULL node label test
		String filePath = "";
		try {
			assertTrue(validImageFile != null);
			filePath = this.imageQuery.saveImage(validImageFile, null);
			assertTrue(filePath != null);
			assertFalse(filePath.isEmpty());
			assertTrue(filePath.contains("undef-images"));
			assertTrue(new File(this.dataDirectory + filePath).exists());
			new File(this.dataDirectory + filePath).delete();
		} catch (IOException e) {
			assertEquals("No errors", e.getLocalizedMessage());
		}
		
		//Valid test
		try {
			filePath = this.imageQuery.saveImage(validImageFile, user);
			assertTrue(filePath != null);
			assertFalse(filePath.isEmpty());
			assertTrue(filePath.contains("user-images"));
			assertTrue(new File(this.dataDirectory + filePath).exists());
			new File(this.dataDirectory + filePath).delete();
		} catch (IOException e) {
			assertEquals("No errors", e.getLocalizedMessage());
		}
	}
	
//	@Test
	public void loadImageTest() {
		//Check invalid ID

		assertEquals(null, this.imageQuery.loadImage(-10l, recipe));
		
		//Check invalid/null node label
		assertEquals(null, this.imageQuery.loadImage(1l, null));
		
		assertEquals(null, this.imageQuery.loadImage(1l, undef));
		
		//ImagePath = empty
		//Get node without image path
		NodeLabel gottenLabel = null;
		Long gottenID = null;
		Statement statement = new Statement("MATCH (n) WHERE n.imagePath IS NULL "
									+ "RETURN labels(n) AS labels, ID(n) AS id");
		
		try(Session readSession = this.graph.openReadSession()) {
			StatementResult result = readSession.run(statement);
			
			if(result.hasNext()) {
				List<Record> records = result.list();
				for(Record record : records) {
					//Get Labels
					List<Object> labels = record.get("labels", new ArrayList<>());
					for(Object label : labels) {
						gottenLabel = NodeLabel.UNDEF.getNodeLabel(label.toString());
						if(gottenLabel != NodeLabel.UNDEF) {
							break;
						}
					}
					
					if(gottenLabel == NodeLabel.UNDEF) {
						gottenLabel = null;
						continue;
					}
					else {
						gottenID = record.get("id", -1l);
					}
					
					if(gottenID != null && !(Math.abs(Math.abs(gottenID) -1l) < 0.001)) {
						break;
					}
					else {
						gottenID = null;
					}
				}
			}
		} catch (DatabaseException e2) {
			assertEquals("No errors", e2.getLocalizedMessage());
		}
		
		if(gottenID != null && gottenLabel != null) {
			assertEquals(null, this.imageQuery.loadImage(gottenID, gottenLabel));

			
			//File does not exist
			//Set invalid path
			statement = new Statement("MATCH(n) WHERE ID(n) = $id SET n.imagePath = 'invalid.png'");
			Statement statement2 = new Statement("MATCH(n) WHERE ID(n) = $id REMOVE n.imagePath");
			try(Session writeSession = this.graph.openWriteSession()) {
				writeSession.run(statement.withParameters(Values.parameters("id", gottenID)));
				assertEquals(null, this.imageQuery.loadImage(gottenID, gottenLabel));
				writeSession.run(statement2.withParameters(Values.parameters("id", gottenID)));
			} catch (DatabaseException e2) {
				assertEquals("No errors", e2.getLocalizedMessage());
			}
			
			//Set valid path
			File validImageFile = new File(TEST_IMAGE_SOURCE);
			String filePath = null;
			try {
				filePath = this.imageQuery.saveImage(validImageFile, gottenLabel);
			} catch (IOException e1) {
				assertEquals("No errors", e1.getLocalizedMessage());
			}
			assertTrue(filePath != null);
			
			statement = new Statement("MATCH(n) WHERE ID(n) = $id SET n.imagePath = $imgPath");
			try(Session writeSession = this.graph.openWriteSession()) {
				writeSession.run(statement.withParameters(Values.parameters(
						"id", gottenID,
						"imgPath", filePath)));
				
				assertTrue(this.imageQuery.loadImage(gottenID, gottenLabel) != null);
				writeSession.run(statement2.withParameters(Values.parameters("id", gottenID)));
			} catch (DatabaseException e1) {
				assertEquals("No errors", e1.getLocalizedMessage());
			}
		}
	}
	
//	@Test
	public void loadImageFileTest() {
		//Check invalid ID
		assertEquals(null, this.imageQuery.loadImageFile(-10, recipe));
		
		
		//Check invalid/null node label
		assertEquals(null, this.imageQuery.loadImageFile(1, null));
		assertEquals(null, this.imageQuery.loadImageFile(1, undef));
		
		//ImagePath = empty
		//Get node without image path
		NodeLabel gottenLabel = null;
		Long gottenID = null;
		Statement statement = new Statement("MATCH (n) WHERE n.imagePath IS NULL "
									+ "RETURN labels(n) AS labels, ID(n) AS id");
		
		try(Session readSession = this.graph.openReadSession()) {
			StatementResult result = readSession.run(statement);
			
			if(result.hasNext()) {
				List<Record> records = result.list();
				for(Record record : records) {
					//Get Labels
					List<Object> labels = record.get("labels", new ArrayList<>());
					for(Object label : labels) {
						gottenLabel = NodeLabel.UNDEF.getNodeLabel(label.toString());
						if(gottenLabel != NodeLabel.UNDEF) {
							break;
						}
					}
					
					if(gottenLabel == NodeLabel.UNDEF) {
						gottenLabel = null;
						continue;
					}
					else {
						gottenID = record.get("id", -1l);
					}
					
					if(gottenID != null && !(Math.abs(Math.abs(gottenID) -1l) < 0.001)) {
						break;
					}
					else {
						gottenID = null;
					}
				}
			}
		} catch (DatabaseException e) {
			assertEquals("No errors", e.getLocalizedMessage());
		}
		
		if(gottenID != null && gottenLabel != null) {
			assertEquals(null, this.imageQuery.loadImageFile(gottenID, gottenLabel));
			
			//File does not exist
			//Set invalid path
			statement = new Statement("MATCH(n) WHERE ID(n) = $id SET n.imagePath = 'invalid.png'");
			Statement statement2 = new Statement("MATCH(n) WHERE ID(n) = $id REMOVE n.imagePath");
			try(Session writeSession = this.graph.openWriteSession()) {
				writeSession.run(statement.withParameters(Values.parameters("id", gottenID)));
				assertEquals(null, this.imageQuery.loadImageFile(gottenID, gottenLabel));
				writeSession.run(statement2.withParameters(Values.parameters("id", gottenID)));
			} catch (DatabaseException e2) {
				assertEquals("No errors", e2.getLocalizedMessage());
			} 
			
			//Set valid path
			File validImageFile = new File(TEST_IMAGE_SOURCE);
			String filePath = null;
			try {
				filePath = this.imageQuery.saveImage(validImageFile, gottenLabel);
			} catch (IOException e1) {
				assertEquals("No errors", e1.getLocalizedMessage());
			}
			assertTrue(filePath != null);
			
			statement = new Statement("MATCH(n) WHERE ID(n) = $id SET n.imagePath = $imgPath");
			try(Session writeSession = this.graph.openWriteSession()) {
				writeSession.run(statement.withParameters(Values.parameters(
						"id", gottenID,
						"imgPath", filePath)));
				
				assertTrue(this.imageQuery.loadImageFile(gottenID, gottenLabel) != null);
				writeSession.run(statement2.withParameters(Values.parameters("id", gottenID)));
			} catch (DatabaseException e) {
				assertEquals("No errors", e.getLocalizedMessage());
			} 
		}
	}
}
