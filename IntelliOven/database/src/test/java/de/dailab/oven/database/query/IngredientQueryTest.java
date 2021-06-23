package de.dailab.oven.database.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.After;
import org.junit.Test;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Statement;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Values;

import de.dailab.oven.database.AbstractDatabaseTest;
import de.dailab.oven.database.configuration.Graph;
import de.dailab.oven.database.exceptions.ConfigurationException;
import de.dailab.oven.database.exceptions.DatabaseException;
import de.dailab.oven.model.database.RelationshipType;
import de.dailab.oven.database.validate.UserValidator;
import de.dailab.oven.model.data_model.Amount;
import de.dailab.oven.model.data_model.Ingredient;
import de.dailab.oven.model.data_model.Nutrition;
import de.dailab.oven.model.data_model.User;
import de.dailab.oven.model.database.NodeLabel;
import zone.bot.vici.Language;

public class IngredientQueryTest extends AbstractDatabaseTest {
	
	private Graph graph;
	private IngredientQuery ingredientQuery;
	private UserQuery userQuery;
	private static final UserValidator VALIDATOR = new UserValidator();
	private static final Map<Nutrition, Amount> BULK_NUTRITION = new EnumMap<>(Nutrition.class);
	
	@Override
	public void initialize()
            throws DatabaseException, ConfigurationException {
		this.graph = this.getGraph();
		this.ingredientQuery = new IngredientQuery(this.graph);
		this.userQuery = new UserQuery(this.graph);
	}

	@After
	public void close() {
		if(this.ingredientQuery != null) {
			this.ingredientQuery.close();			
		}
	}
	
	@Test
	public void getConnectedIngredientsTest() {
		//NULL test and invalid input test
		assertEquals(new HashSet<>(), this.ingredientQuery.getConnectedIngredients(null, null));
		assertEquals(new HashSet<>(), this.ingredientQuery.getConnectedIngredients(Long.MAX_VALUE, null));
		assertEquals(new HashSet<>(), this.ingredientQuery.getConnectedIngredients(Long.MAX_VALUE, NodeLabel.AUTHOR));
		
		//Valid test
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
		
		//Create valid user
		User user = new User();
		String userName = "tmpingredientquerysuser11"; 
		user.setName(userName);
		user.setCurrentlySpokenLanguage(Language.ENGLISH);
		user.addIncompatibleIngredient(validIngredient1);
		user.addLikedIngredient(validIngredient2);
		user.addIncompatibleIngredient(validIngredient3);
		assertTrue(VALIDATOR.isValid(user));
		Set<Ingredient> gottenIngredients = new HashSet<>();
		try {
			user = this.userQuery.putUser(user);
			assertTrue(user.getId() != -1);
			gottenIngredients = 
					this.ingredientQuery.getConnectedIngredients(user.getId(), NodeLabel.USER);
			assertTrue(ingredients.containsAll(gottenIngredients));
			assertTrue(gottenIngredients.containsAll(ingredients));
			this.userQuery.deleteUser(user);
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		
		//Delete ingredients
		Statement statement;
		try (Session writeSession = this.graph.openWriteSession()){
			for(Ingredient ingredient : gottenIngredients) {
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
	public void getConnectedIngredientsRelationShipTest() {
		//NULL test and invalid input test
		assertEquals(new HashSet<>(), this.ingredientQuery.getConnectedIngredients(null, null, null));
		assertEquals(new HashSet<>(), this.ingredientQuery.getConnectedIngredients(Long.MAX_VALUE, null, null));
		assertEquals(new HashSet<>(), this.ingredientQuery.getConnectedIngredients(Long.MAX_VALUE, NodeLabel.AUTHOR, null));
		
		//Valid test
		//Create valid ingredients
		float standard = 5.0f;
		Set<Ingredient> ingredients = new HashSet<>();
		Set<Ingredient> incompatible = new HashSet<>();
		
		Ingredient validIngredient1 = new Ingredient("test ingredient 6", Language.ENGLISH); 

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
		
		Ingredient validIngredient2 = new Ingredient("test ingredient 7", Language.ENGLISH); 

		validIngredient2.addNutrition(Nutrition.CARBOHYDRATE, 
				new Amount(standard, Nutrition.CARBOHYDRATE.getStandartUnit()));
		
		validIngredient2.setCheckedForNutrition(true);
		
		ingredients.add(validIngredient2);

		Ingredient validIngredient3 = new Ingredient("test ingredient 8", Language.ENGLISH); 

		validIngredient3.setCheckedForNutrition(false);
		
		ingredients.add(validIngredient3);
		
		//Create valid user
		User user = new User();
		String userName = "tmpingredientquerysuser7"; 
		user.setName(userName);
		user.setCurrentlySpokenLanguage(Language.ENGLISH);
		user.addIncompatibleIngredient(validIngredient1);
		incompatible.add(validIngredient1);
		user.addLikedIngredient(validIngredient2);
		user.addIncompatibleIngredient(validIngredient3);
		incompatible.add(validIngredient3);
		assertTrue(VALIDATOR.isValid(user));
		Set<Ingredient> gottenIngredients = new HashSet<>();
		Set<Ingredient> gottenIncompatibles = new HashSet<>();
		try {
			user = this.userQuery.putUser(user);
			assertTrue(user.getId() != -1);
			gottenIngredients = 
					this.ingredientQuery.getConnectedIngredients(user.getId(), NodeLabel.USER);
			gottenIncompatibles = 
					this.ingredientQuery.getConnectedIngredients(user.getId(), NodeLabel.USER, 
							RelationshipType.IS_INCOMPATIBLE_WITH);
			
			assertTrue(ingredients.containsAll(gottenIngredients));
			
			assertTrue(gottenIngredients.containsAll(ingredients));
			
			assertTrue(incompatible.containsAll(gottenIncompatibles));
			assertTrue(gottenIncompatibles.containsAll(incompatible));
			this.userQuery.deleteUser(user);	
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		
		//Delete ingredients
		Statement statement;
		try (Session writeSession = this.graph.openWriteSession()){
			for(Ingredient ingredient : gottenIngredients) {
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
	public void resetGraphTest() {
		assertTrue(this.ingredientQuery.getGraph() != null);
		this.ingredientQuery.setGraph(null);
		assertEquals(null, this.ingredientQuery.getGraph());
		this.ingredientQuery.setGraph(this.graph);
		assertEquals(this.graph, this.ingredientQuery.getGraph());
	}
	
	@Test
	public void getIngredientTest() {
		//NULL test and invalid input test
		Long n = null;
		assertEquals(null, this.ingredientQuery.getIngredient(n));
		assertEquals(null, this.ingredientQuery.getIngredient(Long.MIN_VALUE));
		assertEquals(null, this.ingredientQuery.getIngredient(Long.MAX_VALUE));
		assertEquals(null, this.ingredientQuery.getIngredient(-1l));
		
		//Create user for invalid input
		User user = new User();
		String userName = "tmpingredientquerysuser10"; 
		user.setName(userName);
		user.setCurrentlySpokenLanguage(Language.ENGLISH);
		try {
			user = this.userQuery.putUser(user);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		assertTrue(user.getId() != -1);
		
		assertEquals(null, this.ingredientQuery.getIngredient(user.getId()));
		
		//Valid test
		//Create valid ingredient
		Ingredient ingredient = new Ingredient("test ingredient 20", Language.ENGLISH); 
		Statement statement;
		StatementResult result;
		Long id = null;
		try(Session writeSession = this.graph.openWriteSession()){
			statement = new Statement("MERGE (ingredient:Ingredient {name: $name}) "
					+ "WITH * MERGE (language:Language {name: $languageName}) "
					+ "WITH * MERGE (ingredient)-[:IS]-(language) "
					+ "WITH * RETURN ID(ingredient)");
			
			result = writeSession.run(statement.withParameters(Values.parameters(
					"name", ingredient.getName(),
					"languageName", ingredient.getLanguage().getLangCode2()
					)));
			
			id = result.list().get(0).get(0).asLong();
		} catch (DatabaseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		assertTrue(id != null);
		assertEquals(ingredient, this.ingredientQuery.getIngredient(id));
		
		
		//Delete user
		this.userQuery.deleteUser(user);	
		
		//Delete ingredients
		try (Session writeSession = this.graph.openWriteSession()){
				statement = new Statement("MATCH(ingredient:Ingredient) "
						+ "WHERE ID(ingredient) = $id "
						+ "DETACH DELETE ingredient");
				writeSession.run(statement.withParameters(Values.parameters("id", id)));
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	@Test
	public void getNutritionTest() {
		//NULL test and invalid input test
		assertEquals(BULK_NUTRITION, this.ingredientQuery.getNutrition(null));
		assertEquals(BULK_NUTRITION, this.ingredientQuery.getNutrition(Long.MIN_VALUE));
		assertEquals(BULK_NUTRITION, this.ingredientQuery.getNutrition(Long.MAX_VALUE));
		assertEquals(BULK_NUTRITION, this.ingredientQuery.getNutrition(-1l));
		
		//Create user for invalid input
		User user = new User();
		String userName = "tmpingredientquerysuser2"; 
		user.setName(userName);
		user.setCurrentlySpokenLanguage(Language.ENGLISH);
		try {
			user = this.userQuery.putUser(user);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		assertTrue(user.getId() != -1);
		
		assertEquals(BULK_NUTRITION, this.ingredientQuery.getNutrition(user.getId()));
		
		//Valid test
		//Create valid ingredient
		Ingredient ingredient = new Ingredient("test ingredient 10", Language.ENGLISH); 
		Statement statement;
		StatementResult result;
		Long id = null;
		try(Session writeSession = this.graph.openWriteSession()){
			statement = new Statement("MERGE (ingredient:Ingredient {name: $name}) "
					+ "WITH * MERGE (language:Language {name: $languageName}) "
					+ "WITH * MERGE (ingredient)-[:IS]-(language) "
					+ "WITH * RETURN ID(ingredient)");
			
			result = writeSession.run(statement.withParameters(Values.parameters(
					"name", ingredient.getName(),
					"languageName", ingredient.getLanguage().getLangCode2()
					)));
			
			id = result.list().get(0).get(0).asLong();
		} catch (DatabaseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		assertTrue(id != null);
		
		assertEquals(BULK_NUTRITION, this.ingredientQuery.getNutrition(id));
		
		
		float standard = 5.0f;
		ingredient.addNutrition(Nutrition.CARBOHYDRATE, 
				new Amount(standard, Nutrition.CARBOHYDRATE.getStandartUnit()));
		
		ingredient.addNutrition(Nutrition.ENERGY, 
				new Amount(standard, Nutrition.ENERGY.getStandartUnit()));
		
		try(Session writeSession = this.graph.openWriteSession()) {
			for(Entry<Nutrition, Amount> entry : ingredient.getNutrition().entrySet()) {
				statement = new Statement("MATCH (ingredient:Ingredient) WHERE ID(ingredient) = $id "
						+ "SET ingredient." + entry.getKey().name().toLowerCase() + " = $amount");
				
				writeSession.run(statement.withParameters(Values.parameters(
						"id", id,
						"amount", entry.getValue().toString()
						)));
			}
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
 		assertEquals(ingredient.getNutrition(), this.ingredientQuery.getNutrition(id));
 		
		//Delete user
		this.userQuery.deleteUser(user);	
		
		//Delete ingredient
		try (Session writeSession = this.graph.openWriteSession()){
			statement = new Statement("MATCH(ingredient:Ingredient) "
								+ "WHERE ID(ingredient) = $id "
								+ "DETACH DELETE ingredient");
			writeSession.run(statement.withParameters(Values.parameters("id", id)));			
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
