package de.dailab.oven.database.parse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
import de.dailab.oven.model.data_model.Amount;
import de.dailab.oven.model.data_model.Ingredient;
import de.dailab.oven.model.data_model.Nutrition;
import zone.bot.vici.Language;

public class NutritionParserTest extends AbstractDatabaseTest {
	
	@Nullable
	private Graph graph;
	@Nonnull
	private NutritionParser nutritionParser = new NutritionParser();
	
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
	public void parseValidNutritionFromNodeTest() {
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
							+ "SET ingredient." + entry.getKey().toVariable() + " = '"
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
		} catch (DatabaseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		assertFalse(validIngredientNodes.isEmpty());
		//NULL test
		assertEquals(new EnumMap<>(Nutrition.class), 
				this.nutritionParser.parseNutritionFromNode(null));
		
		//Check nutrition
		int match = 0;
		for(Node node : validIngredientNodes) {
			Map<Nutrition, Amount> parsedNutrition = this.nutritionParser.parseNutritionFromNode(node);
			for(Ingredient storedIngredient : ingredients) {
				if(storedIngredient.getNutrition().equals(parsedNutrition)) {
					match += 1;
				}
			}
		}
		assertEquals(3, match);
		
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
