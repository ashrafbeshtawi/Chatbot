package de.dailab.oven.database.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.EnumMap;
import java.util.HashMap;
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
import de.dailab.oven.model.data_model.Amount;
import de.dailab.oven.model.data_model.Ingredient;
import de.dailab.oven.model.data_model.Nutrition;
import de.dailab.oven.model.database.NodeLabel;
import zone.bot.vici.Language;

public class NutritionQueryTest extends AbstractDatabaseTest {
	
	private Graph graph;
	private IngredientQuery ingredientQuery;
	private NutritionQuery nutritionQuery;
	private static final Map<Nutrition, Amount> BULK_NUTRITION = new EnumMap<>(Nutrition.class);
	
	private Map<Nutrition, Amount> testMap = new EnumMap<>(Nutrition.class);
	
	@Override
	public void initialize()
            throws DatabaseException, ConfigurationException {
		this.graph = this.getGraph();
		this.ingredientQuery = new IngredientQuery(this.graph);
		this.nutritionQuery = new NutritionQuery(this.graph);
		
		float standard = 5.0f;
		
		this.testMap.put(Nutrition.CARBOHYDRATE, 
				new Amount(standard, Nutrition.CARBOHYDRATE.getStandartUnit()));
		
		this.testMap.put(Nutrition.ENERGY, 
				new Amount(standard, Nutrition.ENERGY.getStandartUnit()));
		
		this.testMap.put(Nutrition.FAT, 
				new Amount(standard, Nutrition.FAT.getStandartUnit()));
		
		this.testMap.put(Nutrition.FIBER, 
				new Amount(standard, Nutrition.FIBER.getStandartUnit()));
		
		this.testMap.put(Nutrition.PROTEIN, 
				new Amount(standard, Nutrition.PROTEIN.getStandartUnit()));
		
		this.testMap.put(Nutrition.SATFAT, 
				new Amount(standard, Nutrition.SATFAT.getStandartUnit()));
		
		this.testMap.put(Nutrition.SUGAR, 
				new Amount(standard, Nutrition.SUGAR.getStandartUnit()));
		
		this.testMap.put(Nutrition.WATER, 
				new Amount(standard, Nutrition.WATER.getStandartUnit()));
		
	}

	@After
	public void close() {
		if(this.ingredientQuery != null) {
			this.ingredientQuery.close();
			this.nutritionQuery.close();
		}
	}
	
	@Test
	public void resetGraphTest() {
		assertTrue(this.nutritionQuery.getGraph() != null);
		this.nutritionQuery.setGraph(null);
		assertEquals(null, this.nutritionQuery.getGraph());
		this.nutritionQuery.setGraph(this.getGraph());
		assertEquals(this.getGraph(), this.nutritionQuery.getGraph());
	}
	
	@Test
	public void nullTest() {
		NutritionQuery nullQuery = new NutritionQuery(null);
		assertEquals(null, nullQuery.getGraph());
		
		assertTrue(nullQuery.getNutrition(null).isEmpty());
		assertTrue(nullQuery.getNutrition(1l).isEmpty());
		
		assertTrue(nullQuery.setNutrition(null, null, null));
		assertTrue(nullQuery.setNutrition(this.testMap, NodeLabel.AUTHOR, null));
		assertTrue(nullQuery.setNutrition(this.testMap, null, 1l));
		assertTrue(nullQuery.setNutrition(null, NodeLabel.AUTHOR, 1l));
		
		assertTrue(nullQuery.getSetNutritionString(null, null).isEmpty());
		assertTrue(nullQuery.getSetNutritionString(this.testMap, null).isEmpty());
		assertTrue(nullQuery.getSetNutritionString(this.testMap, "").isEmpty());
		assertTrue(nullQuery.getSetNutritionString(null, "test").isEmpty());
		assertTrue(nullQuery.getSetNutritionString(BULK_NUTRITION, "test").isEmpty());
	}

	@Test
	public void getSetNutritionStringTest() {
		String expectedResult = 
				"SET test.water = '5.0 gram' "
				+ "SET test.energy = '5.0 kcal' "
				+ "SET test.protein = '5.0 gram' "
				+ "SET test.carbohydrate = '5.0 gram' "
				+ "SET test.fiber = '5.0 gram' "
				+ "SET test.sugar = '5.0 gram' "
				+ "SET test.fat = '5.0 gram' "
				+ "SET test.satfat = '5.0 gram' ";
		
		assertEquals(expectedResult, this.nutritionQuery.getSetNutritionString(this.testMap, "test"));
		
		Map<Nutrition, Amount> testNullMap = new HashMap<>();
		testNullMap.putAll(this.testMap);
		testNullMap.put(null, null);
		
		String gottenString = this.nutritionQuery.getSetNutritionString(testNullMap, "test");

		assertTrue(!gottenString.isEmpty());
	}
	
	@Test
	public void getNutritionTest() {
		//Create valid ingredients
		float standard = 5.0f;
		Set<Ingredient> ingredients = new HashSet<>();
		
		Ingredient validIngredient1 = new Ingredient("tmpnutritioningredient 1", Language.ENGLISH); 
		validIngredient1.setNutrition(this.testMap);
		validIngredient1.setCheckedForNutrition(true);

		ingredients.add(validIngredient1);
		
		Ingredient validIngredient2 = new Ingredient("tmpnutritioningredient 2", Language.ENGLISH); 
		validIngredient2.addNutrition(Nutrition.CARBOHYDRATE, 
				new Amount(standard, Nutrition.CARBOHYDRATE.getStandartUnit()));
		validIngredient2.setCheckedForNutrition(true);
		
		ingredients.add(validIngredient2);

		Ingredient validIngredient3 = new Ingredient("tmpnutritioningredient 3", Language.ENGLISH); 
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
				assertNotEquals(-1l, id);
				
				ingredient.setID(id);
				
				for(Entry<Nutrition, Amount> entry : ingredient.getNutrition().entrySet()) {
					statement = new Statement("MATCH (ingredient:Ingredient) "
							+ "WHERE ID(ingredient) = $id "
							+ "SET ingredient." + entry.getKey().toVariable() + " = '"
								+ entry.getValue().toString() + "'");					
					
					writeSession.run(statement.withParameters(Values.parameters("id", ingredient.getID())));
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			assertEquals("No errors", e.getLocalizedMessage());
		}
		
		ingredients.forEach(i -> assertEquals(i.getNutrition(), this.nutritionQuery.getNutrition(i.getID())));
		
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
	public void setNutritionTest() {
		//Create valid ingredients
		float standard = 5.0f;
		Set<Ingredient> ingredients = new HashSet<>();
		
		Ingredient validIngredient1 = new Ingredient("tmpnutritioningredient 1", Language.ENGLISH); 
		validIngredient1.setNutrition(this.testMap);
		validIngredient1.setCheckedForNutrition(true);

		ingredients.add(validIngredient1);
		
		Ingredient validIngredient2 = new Ingredient("tmpnutritioningredient 2", Language.ENGLISH); 
		validIngredient2.addNutrition(Nutrition.CARBOHYDRATE, 
				new Amount(standard, Nutrition.CARBOHYDRATE.getStandartUnit()));
		validIngredient2.setCheckedForNutrition(true);
		
		ingredients.add(validIngredient2);

		Ingredient validIngredient3 = new Ingredient("tmpnutritioningredient 3", Language.ENGLISH); 
		validIngredient3.setCheckedForNutrition(false);
		
		ingredients.add(validIngredient3);
		
		assertFalse(this.nutritionQuery.setNutrition(this.testMap, NodeLabel.AUTHOR, 1l));
		assertFalse(this.nutritionQuery.setNutrition(this.testMap, NodeLabel.INGREDIENT, 1l));
		
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
				assertNotEquals(-1l, id);
				
				ingredient.setID(id);
				
			}
		} catch(Exception e) {
			e.printStackTrace();
			assertEquals("No errors", e.getLocalizedMessage());
		}
		
		ingredients.forEach(i -> assertTrue(this.nutritionQuery.setNutrition(i.getNutrition(), NodeLabel.INGREDIENT, i.getID())));
		
		ingredients.forEach(i -> assertEquals(i.getNutrition(), this.nutritionQuery.getNutrition(i.getID())));
		
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
}
