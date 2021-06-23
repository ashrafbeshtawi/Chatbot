package de.dailab.oven.database.parse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.After;
import org.junit.Test;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Statement;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Values;
import org.neo4j.driver.v1.types.Node;

import de.dailab.oven.database.AbstractDatabaseTest;
import de.dailab.oven.database.configuration.Graph;
import de.dailab.oven.database.exceptions.ConfigurationException;
import de.dailab.oven.database.exceptions.DatabaseException;
import de.dailab.oven.database.model.IngredientMap;
import de.dailab.oven.database.validate.UserValidator;
import de.dailab.oven.model.data_model.Amount;
import de.dailab.oven.model.data_model.Ingredient;
import de.dailab.oven.model.data_model.Nutrition;
import de.dailab.oven.model.data_model.User;
import zone.bot.vici.Language;

public class IngredientParserTest extends AbstractDatabaseTest {
	
	private Graph graph;
	private IngredientParser ingredientParser = new IngredientParser();
	
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
	public void parseValidIngredientFromNodeTest() {
		//Create valid ingredients
		float standard = 5.0f;
		Set<Ingredient> ingredients = new HashSet<>();
		
		Ingredient validIngredient1 = new Ingredient("test ingredient 1", Language.ENGLISH); 

		validIngredient1.addNutrition(Nutrition.CARBOHYDRATE, 
				new Amount(standard, Nutrition.CARBOHYDRATE.getStandartUnit()));
		
		validIngredient1.addNutrition(Nutrition.ENERGY, 
				new Amount(standard, Nutrition.ENERGY.getStandartUnit()));
		
		validIngredient1.addNutrition(Nutrition.FAT, 
				new Amount(standard, Nutrition.FAT.getStandartUnit()));
		
		validIngredient1.addNutrition(Nutrition.FIBER, 
				new Amount(standard, Nutrition.FIBER.getStandartUnit()));
		
		validIngredient1.addNutrition(Nutrition.PROTEIN, 
				new Amount(standard, Nutrition.PROTEIN.getStandartUnit()));
		
		validIngredient1.addNutrition(Nutrition.SATFAT, 
				new Amount(standard, Nutrition.SATFAT.getStandartUnit()));
		
		validIngredient1.addNutrition(Nutrition.SUGAR, 
				new Amount(standard, Nutrition.SUGAR.getStandartUnit()));
		
		validIngredient1.addNutrition(Nutrition.WATER, 
				new Amount(standard, Nutrition.WATER.getStandartUnit()));
		
		validIngredient1.setCheckedForNutrition(true);
		
		ingredients.add(validIngredient1);
		
		Ingredient validIngredient2 = new Ingredient("test ingredient 2", Language.ENGLISH); 

		validIngredient2.addNutrition(Nutrition.CARBOHYDRATE, 
				new Amount(standard, Nutrition.CARBOHYDRATE.getStandartUnit()));
		
		validIngredient2.setCheckedForNutrition(true);
		
		ingredients.add(validIngredient2);

		Ingredient validIngredient3 = new Ingredient("test ingredient 3", Language.ENGLISH); 

		validIngredient3.setCheckedForNutrition(false);
		
		ingredients.add(validIngredient3);
		
		//Put and get ingredient
		Statement statement;
		StatementResult result;
		long id;
		try(Session writeSession = this.graph.openWriteSession()) {
			for(Ingredient ingredient : ingredients) {
				id = -1l;
				
				statement = new Statement("MERGE (ingredient:Ingredient {name: $ingredientName}) "
						+ "WITH * MERGE (language:Language {name: $languageName}) "
						+ "WITH * MERGE (ingredient)-[:IS]-(language) "
						+ "WITH * SET ingredient.checkedForNutrition = $checked "
						+ "WITH * RETURN ID(ingredient) AS id");
				
				result = writeSession.run(statement.withParameters(Values.parameters(
						"ingredientName", ingredient.getName(),
						"languageName", ingredient.getLanguage().getLangCode2(),
						"checked", ingredient.isCheckedForNutrition()
						)));
				
				id = result.list().get(0).get("id").asLong();
				assertTrue(id != -1);
				
				ingredient.setID(id);
				
				for(Entry<Nutrition, Amount> entry : ingredient.getNutrition().entrySet()) {
					statement = new Statement("MATCH (ingredient:Ingredient) "
							+ "WHERE ID(ingredient) = $id "
							+ "SET ingredient." + entry.getKey().name().toLowerCase() + " = '"
								+ entry.getValue().toString() + "'");					
					
					writeSession.run(statement.withParameters(Values.parameters("id", ingredient.getID())));
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			assertEquals("No errors", e.getLocalizedMessage());
		}
		
		List<Node> validIngredientNodes = new ArrayList<>();
		
		try(Session readSession = this.graph.openReadSession()) {
			for(Ingredient ingredient : ingredients) {
				statement = new Statement("MATCH (ingredient:Ingredient) "
						+ "WHERE ID(ingredient) = $id "
						+ "RETURN ingredient");
				
				result = readSession.run(statement.withParameters(Values.parameters("id", ingredient.getID())));
				
				validIngredientNodes.add(result.single().get(0).asNode());
			}
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertFalse(validIngredientNodes.isEmpty());
		
		//NULL test
		assertEquals(null, 
				this.ingredientParser.parseIngredientFromNode(null, Language.ENGLISH));
		
		Language nullLanguage = null;
		assertEquals(Language.UNDEF, 
				this.ingredientParser.parseIngredientFromNode(validIngredientNodes.get(0), nullLanguage).getLanguage());
		
		//Check nutrition
		for(Node node : validIngredientNodes) {
			Ingredient parsedIngredient = this.ingredientParser.parseIngredientFromNode(node, Language.ENGLISH);
			assertTrue(ingredients.contains(parsedIngredient));
			for(Ingredient storedIngredient : ingredients) {
				if(storedIngredient.equals(parsedIngredient)) {
					assertEquals(storedIngredient.isCheckedForNutrition(), 
							parsedIngredient.isCheckedForNutrition());
				}
			}
		}
		
		//Delete ingredients
		try (Session writeSession = this.graph.openWriteSession()){
			for(Ingredient ingredient : ingredients) {
				statement = new Statement("MATCH(ingredient:Ingredient) "
						+ "WHERE ID(ingredient) = $id "
						+ "DETACH DELETE ingredient");
				
				writeSession.run(statement.withParameters(Values.parameters("id", ingredient.getID())));
			}
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	@Test
	public void parseInvalidIngredientFromNodeTest() {
		//Create non ingredient node
		User user = new User();
		String userName = "tmpingredientuser"; 
		user.setName(userName);
		user.setCurrentlySpokenLanguage(Language.ENGLISH);
		assertTrue(new UserValidator().isValid(user));
		Node userNode = null;
		Statement statement;
		StatementResult result;
		try(Session writeSession = this.graph.openWriteSession()){
			statement = new Statement("MERGE (user:User {name: $userName}) "
					+ "WITH * MERGE (language:Language {name: $language}) "
					+ "WITH * MERGE (user)-[:SPEAKS]-(language) "
					+ "WITH * RETURN user");
			
			result = writeSession.run(statement.withParameters(Values.parameters(
					"userName", user.getName(),
					"language", user.getCurrentlySpokenLanguage().getLangCode2()
					)));
			
			userNode = result.list().get(0).get(0).asNode();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertTrue(userNode != null);
		
		//Check with invalid label
		assertEquals(null, this.ingredientParser.parseIngredientFromNode(userNode, Language.ENGLISH));
		
		//Delete user
		try(Session writeSession = this.graph.openWriteSession()){
			statement = new Statement("MATCH (user:User) "
					+ "WHERE user.name = $userName "
					+ "DETACH DELETE user");
			
			result = writeSession.run(statement.withParameters(Values.parameters(
					"userName", user.getName())));
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Test not set nutrition
		Ingredient nutritionIngredient = new Ingredient("test ingredient", Language.ENGLISH); 

		Node ingredientNode = null;
		try(Session writeSession = this.graph.openWriteSession()) {
				statement = new Statement("MERGE (ingredient:Ingredient {name: $ingredientName}) "
						+ "WITH * MERGE (language:Language {name: $languageName}) "
						+ "WITH * MERGE (ingredient)-[:IS]-(language) "
						+ "WITH * RETURN ingredient");
				
				result = writeSession.run(statement.withParameters(Values.parameters(
						"ingredientName", nutritionIngredient.getName(),
						"languageName", nutritionIngredient.getLanguage().getLangCode2()
						)));
				
				ingredientNode = result.list().get(0).get(0).asNode();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Ingredient parsed = this.ingredientParser.parseIngredientFromNode(ingredientNode, nutritionIngredient.getLanguage());
		assertTrue(ingredientNode != null);
		assertEquals(nutritionIngredient, parsed);
		
		//Delete ingredient
		try (Session writeSession = this.graph.openWriteSession()){
			statement = new Statement("MATCH(ingredient:Ingredient) "
					+ "WHERE ID(ingredient) = $id "
					+ "DETACH DELETE ingredient");
				
			writeSession.run(statement.withParameters(Values.parameters("id", parsed.getID())));
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		//Test falsely set nutrition
		nutritionIngredient = new Ingredient("test ingredient", Language.ENGLISH); 


		try(Session writeSession = this.graph.openWriteSession()) {
				statement = new Statement("MERGE (ingredient:Ingredient {name: $ingredientName}) "
						+ "WITH * MERGE (language:Language {name: $languageName}) "
						+ "WITH * MERGE (ingredient)-[:IS]-(language) "
						+ "WITH * SET ingredient.fat = 5 "
						+ "WITH * RETURN ingredient");
				
				result = writeSession.run(statement.withParameters(Values.parameters(
						"ingredientName", nutritionIngredient.getName(),
						"languageName", nutritionIngredient.getLanguage().getLangCode2()
						)));
				
				ingredientNode = result.list().get(0).get(0).asNode();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		parsed = this.ingredientParser.parseIngredientFromNode(ingredientNode, nutritionIngredient.getLanguage());
		assertTrue(ingredientNode != null);
		assertEquals(nutritionIngredient, parsed);
		
		//Delete ingredient
		try (Session writeSession = this.graph.openWriteSession()){
			statement = new Statement("MATCH(ingredient:Ingredient) "
					+ "WHERE ID(ingredient) = $id "
					+ "DETACH DELETE ingredient");
				
			writeSession.run(statement.withParameters(Values.parameters("id", parsed.getID())));
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Test falsely set checked key
		nutritionIngredient = new Ingredient("test ingredient", Language.ENGLISH); 

		try(Session writeSession = this.graph.openWriteSession()) {
				statement = new Statement("MERGE (ingredient:Ingredient {name: $ingredientName}) "
						+ "WITH * MERGE (language:Language {name: $languageName}) "
						+ "WITH * MERGE (ingredient)-[:IS]-(language) "
						+ "WITH * SET ingredient.checkedForNutrition = 5 "
						+ "WITH * RETURN ingredient");
				
				result = writeSession.run(statement.withParameters(Values.parameters(
						"ingredientName", nutritionIngredient.getName(),
						"languageName", nutritionIngredient.getLanguage().getLangCode2()
						)));
				
				ingredientNode = result.list().get(0).get(0).asNode();
		} catch (DatabaseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		parsed = this.ingredientParser.parseIngredientFromNode(ingredientNode, nutritionIngredient.getLanguage());
		assertTrue(ingredientNode != null);
		assertEquals(nutritionIngredient, parsed);

		//Delete ingredient
		try (Session writeSession = this.graph.openWriteSession()){
			statement = new Statement("MATCH(ingredient:Ingredient) "
					+ "WHERE ID(ingredient) = $id "
					+ "DETACH DELETE ingredient");
				
			writeSession.run(statement.withParameters(Values.parameters("id", parsed.getID())));
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void parseValidIngredientsFromNodesTest() {
		//Create valid ingredients
		float standard = 5.0f;
		Set<Ingredient> ingredients = new HashSet<>();
		
		Ingredient validIngredient1 = new Ingredient("test ingredient 1", Language.ENGLISH); 

		validIngredient1.addNutrition(Nutrition.CARBOHYDRATE, 
				new Amount(standard, Nutrition.CARBOHYDRATE.getStandartUnit()));
		
		validIngredient1.addNutrition(Nutrition.ENERGY, 
				new Amount(standard, Nutrition.ENERGY.getStandartUnit()));
		
		validIngredient1.addNutrition(Nutrition.FAT, 
				new Amount(standard, Nutrition.FAT.getStandartUnit()));
		
		validIngredient1.addNutrition(Nutrition.FIBER, 
				new Amount(standard, Nutrition.FIBER.getStandartUnit()));
		
		validIngredient1.addNutrition(Nutrition.PROTEIN, 
				new Amount(standard, Nutrition.PROTEIN.getStandartUnit()));
		
		validIngredient1.addNutrition(Nutrition.SATFAT, 
				new Amount(standard, Nutrition.SATFAT.getStandartUnit()));
		
		validIngredient1.addNutrition(Nutrition.SUGAR, 
				new Amount(standard, Nutrition.SUGAR.getStandartUnit()));
		
		validIngredient1.addNutrition(Nutrition.WATER, 
				new Amount(standard, Nutrition.WATER.getStandartUnit()));
		
		validIngredient1.setCheckedForNutrition(true);
		
		ingredients.add(validIngredient1);
		
		Ingredient validIngredient2 = new Ingredient("test ingredient 2", Language.ENGLISH); 

		validIngredient2.addNutrition(Nutrition.CARBOHYDRATE, 
				new Amount(standard, Nutrition.CARBOHYDRATE.getStandartUnit()));
		
		validIngredient2.setCheckedForNutrition(true);
		
		ingredients.add(validIngredient2);

		Ingredient validIngredient3 = new Ingredient("test ingredient 3", Language.ENGLISH); 

		validIngredient3.setCheckedForNutrition(false);
		
		ingredients.add(validIngredient3);
		
		//Put and get ingredient
		Statement statement;
		StatementResult result;
		long id;
		
		try(Session writeSession = this.graph.openWriteSession()) {
			for(Ingredient ingredient : ingredients) {
				id = -1l;
				
				statement = new Statement("MERGE (ingredient:Ingredient {name: $ingredientName}) "
						+ "WITH * MERGE (language:Language {name: $languageName}) "
						+ "WITH * MERGE (ingredient)-[:IS]-(language) "
						+ "WITH * SET ingredient.checkedForNutrition = $checked "
						+ "WITH * RETURN ID(ingredient) AS id");
				
				result = writeSession.run(statement.withParameters(Values.parameters(
						"ingredientName", ingredient.getName(),
						"languageName", ingredient.getLanguage().getLangCode2(),
						"checked", ingredient.isCheckedForNutrition()
						)));
				
				id = result.list().get(0).get("id").asLong();
				assertTrue(id != -1);
				
				ingredient.setID(id);
				
				for(Entry<Nutrition, Amount> entry : ingredient.getNutrition().entrySet()) {
					statement = new Statement("MATCH (ingredient:Ingredient) "
							+ "WHERE ID(ingredient) = $id "
							+ "SET ingredient." + entry.getKey().name().toLowerCase() + " = '"
								+ entry.getValue().toString() + "'");					
					
					writeSession.run(statement.withParameters(Values.parameters("id", ingredient.getID())));
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			assertEquals("No errors", e.getLocalizedMessage());
		}
		
		IngredientMap ingredientMap = new IngredientMap();
		List<Node> ingredientNodes = new ArrayList<>();
		try(Session readSession = this.graph.openReadSession()) {
			for(Ingredient ingredient : ingredients) {
				statement = new Statement("MATCH (ingredient:Ingredient) "
						+ "WHERE ID(ingredient) = $id "
						+ "WITH * MATCH (ingredient)-[:IS]-(language:Language) "
						+ "RETURN ingredient, language");
				
				result = readSession.run(statement.withParameters(Values.parameters("id", ingredient.getID())));
				
				Record record = result.list().get(0);
				ingredientNodes.add(record.get(0).asNode());
				ingredientMap.put(record.get(0).asNode(), record.get(1).asNode());
			}
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertFalse(ingredientMap.getIngredientMap().isEmpty());
		
		//NULL test
		assertEquals(new HashSet<>(), 
				this.ingredientParser.parseIngredientsFromNodes(null));
		
		assertEquals(new HashSet<>(), this.ingredientParser.parseIngredientsFromNodes(null, Language.ENGLISH));
		
		//Check nutrition
		for(Node node : ingredientMap.getIngredientMap().keySet()) {
			Ingredient parsedIngredient = this.ingredientParser.parseIngredientFromNode(node, Language.ENGLISH);
			assertTrue(ingredients.contains(parsedIngredient));
			for(Ingredient storedIngredient : ingredients) {
				if(storedIngredient.equals(parsedIngredient)) {
					assertEquals(storedIngredient.isCheckedForNutrition(), 
							parsedIngredient.isCheckedForNutrition());
				}
			}
		}
		
		//Check correct working
		Set<Ingredient> parsedIngredients = this.ingredientParser.parseIngredientsFromNodes(ingredientMap);
		Set<Ingredient> parsedIngredients2 = this.ingredientParser.parseIngredientsFromNodes(ingredientNodes, Language.ENGLISH);
		assertTrue(ingredients.containsAll(parsedIngredients));
		assertTrue(parsedIngredients.containsAll(ingredients));
		
		assertTrue(ingredients.containsAll(parsedIngredients2));
		assertTrue(parsedIngredients2.containsAll(ingredients));
		
		//Delete ingredients
		try (Session writeSession = this.graph.openWriteSession()){
			for(Ingredient ingredient : ingredients) {
				statement = new Statement("MATCH(ingredient:Ingredient) "
						+ "WHERE ID(ingredient) = $id "
						+ "DETACH DELETE ingredient");
				
				writeSession.run(statement.withParameters(Values.parameters("id", ingredient.getID())));
			}
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void parseInvalidIngredientFromNodesTest() {
		IngredientMap ingredientMap = new IngredientMap();
		List<Node> ingredientNodes = new ArrayList<>();
		//Create non ingredient node
		User user = new User();
		String userName = "tmpingredientuser"; 
		user.setName(userName);
		user.setCurrentlySpokenLanguage(Language.ENGLISH);
		assertTrue(new UserValidator().isValid(user));
		Statement statement;
		StatementResult result;
		Node userNode = null;
		Node languageNode = null;
		try(Session writeSession = this.graph.openWriteSession()){
			statement = new Statement("MERGE (user:User {name: $userName}) "
					+ "WITH * MERGE (language:Language {name: $language}) "
					+ "WITH * MERGE (user)-[:SPEAKS]-(language) "
					+ "WITH * RETURN user, language");
			
			result = writeSession.run(statement.withParameters(Values.parameters(
					"userName", user.getName(),
					"language", user.getCurrentlySpokenLanguage().getLangCode2()
					)));
			
			
			Record record = result.list().get(0);
			userNode = record.get(0).asNode();
			languageNode = record.get(1).asNode();
			ingredientNodes.add(userNode);
			ingredientMap.put(userNode, languageNode);
		} catch (DatabaseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		assertFalse(ingredientMap.getIngredientMap().isEmpty());
		
		//Check with invalid label
		assertEquals(new HashSet<>(), this.ingredientParser.parseIngredientsFromNodes(ingredientMap));
		assertEquals(new HashSet<>(), this.ingredientParser.parseIngredientsFromNodes(ingredientNodes, Language.ENGLISH));

		//Delete user
		try(Session writeSession = this.graph.openWriteSession()){
			statement = new Statement("MATCH (user:User) "
					+ "WHERE user.name = $userName "
					+ "DETACH DELETE user");
			
			result = writeSession.run(statement.withParameters(Values.parameters(
					"userName", user.getName())));
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ingredientMap.put(null, languageNode);
		ingredientMap.put(userNode, null);
		assertEquals(new HashSet<>(), this.ingredientParser.parseIngredientsFromNodes(ingredientMap));
		assertEquals(null, this.ingredientParser.parseIngredientFromNode(userNode, languageNode));
		assertEquals(null, this.ingredientParser.parseIngredientFromNode(null, languageNode));
		assertEquals(null, this.ingredientParser.parseIngredientFromNode(userNode, (Node) null));
	}
}
