package de.dailab.oven.database.parse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Statement;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Values;
import org.neo4j.driver.v1.types.Node;

import de.dailab.oven.database.AbstractDatabaseTest;
import de.dailab.oven.database.configuration.Graph;
import de.dailab.oven.database.exceptions.ConfigurationException;
import de.dailab.oven.database.exceptions.DatabaseException;
import de.dailab.oven.model.data_model.Ingredient;
import zone.bot.vici.Language;

public class LanguageParserTest extends AbstractDatabaseTest {
	
	private Graph graph;
	private LanguageParser languageParser = new LanguageParser();
	
	@Override
	public void initialize()
            throws DatabaseException, ConfigurationException {
		this.graph = this.getGraph();
	}

	@After
	public void close() {
		this.graph.close();
	}
	
	@Test
	public void nullTest() {
		assertEquals(Language.UNDEF, this.languageParser.parseLanguageFromNode(null));
	}
	
	@Test
	public void invalidNodeTest() {
		Language language = Language.ENGLISH;
		
		Statement statement;
		StatementResult result;
		try(Session writeSession = this.graph.openWriteSession()) {
			statement = new Statement("MERGE (language:Language {name: $language}) "
					+ "RETURN language");
			
			result = writeSession.run(statement.withParameters(Values.parameters(
					"language", language.getLangCode2()
					)));
			
			assertTrue(result.hasNext());
		} catch (DatabaseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//Create valid ingredient
		Ingredient ingredient = new Ingredient("test ingredient", language); 
		ingredient.setCheckedForNutrition(false);
		
		Node invalidNode = null;
		try(Session writeSession = this.graph.openWriteSession()) {
			
			statement = new Statement("MERGE (ingredient:Ingredient {name: $ingredientName}) "
					+ "WITH * MERGE (language:Language {name: $languageName}) "
					+ "WITH * MERGE (ingredient)-[:IS]-(language) "
					+ "WITH * SET ingredient.checkedForNutrition = $checked "
					+ "WITH * RETURN ingredient");
			
			result = writeSession.run(statement.withParameters(Values.parameters(
					"ingredientName", ingredient.getName(),
					"languageName", ingredient.getLanguage().getLangCode2(),
					"checked", ingredient.isCheckedForNutrition()
					)));
			
			assertTrue(result.hasNext());
			
			invalidNode = result.list().get(0).get(0).asNode();
		} catch(Exception e) {
			assertEquals("No errors", e.getLocalizedMessage());
		}
		
		
		assertEquals(null, this.languageParser.parseLanguageFromNode(invalidNode));
		
		//Delete ingredient
		try (Session writeSession = this.graph.openWriteSession()){
			statement = new Statement("MATCH(ingredient:Ingredient) "
					+ "WHERE ingredient.name = $ingredientName "
					+ "DETACH DELETE ingredient");
			
			writeSession.run(statement.withParameters(Values.parameters(
					"ingredientName", ingredient.getName())));
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	@Test
	public void validTest() {
		Language language = Language.ENGLISH;
		
		Statement statement;
		StatementResult result;
		try(Session writeSession = this.graph.openWriteSession()) {
			statement = new Statement("MERGE (language:Language {name: $language}) "
					+ "RETURN language");
			
			result = writeSession.run(statement.withParameters(Values.parameters(
					"language", language.getLangCode2()
					)));
			
			assertTrue(result.hasNext());
			assertEquals(Language.ENGLISH, 
					this.languageParser.parseLanguageFromNode(result.list().get(0).get(0).asNode()));
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
