package de.dailab.oven.database.parse;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.neo4j.driver.v1.types.Node;

import de.dailab.oven.model.data_model.Ingredient;
import de.dailab.oven.model.database.NodeLabel;
import zone.bot.vici.Language;

/**
 * Class for parsing ingredients from database nodes
 * @author Tristan Schroer
 * @version 2.0.0
 */
public class IngredientParser extends ANeo4JParser{

	@Nonnull
	private static final LanguageParser LANGUAGE_PARSER = new LanguageParser();
	@Nonnull
	private static final String NAME_KEY = "name";
	@Nonnull
	private static final String INGREDIENT_LABEL = NodeLabel.INGREDIENT.toDatabaseLabel();
	@Nonnull
	private static final String CHECKED_KEY = "checkedForNutrition";
	
	/**
	 * Tries to parse ingredients from the given nodes
	 * @param ingredientMap		The nodes to parse with their matching language
	 * @return					The parsed ingredients<br>NULL in case of parsing failure<br>
	 * 							Empty set in case ingredientNodes is empty or NULL
	 */
	@Nullable
	public Set<Ingredient> parseIngredientsFromNodes(
			@Nullable de.dailab.oven.database.model.IngredientMap ingredientMap) {
		
		Set<Ingredient> ingredients = new HashSet<>();

		if(ingredientMap == null)
			return ingredients;

		Map<Node, Node> ingredientsM = ingredientMap.getIngredientMap();
		
		final CountDownLatch latch = new CountDownLatch(ingredientsM.size());
		
		ingredients = new HashSet<>();
			
		for(Entry<Node,Node> ingredientEntry : ingredientsM.entrySet()) {
			Language language = LANGUAGE_PARSER.parseLanguageFromNode(ingredientEntry.getValue());
			
			try {
				ingredients.add(POOL.submit(
						new AsynchIngredientParser(ingredientEntry.getKey(), language, latch)).invoke());
				
			} catch(Exception e) {
				logParsingFailed(e);
				return null;
			}			
		}
		
		waitForLatch(latch);
		
		if(ingredients.contains(null))
			ingredients.remove(null);
		
		return ingredients;
	}
	
	/**
	 * Tries to parse ingredients from the given nodes
	 * @param ingredientNodes	The nodes to parse
	 * @param language			The language to set for the ingredients
	 * @return					The parsed ingredients<br>NULL in case of parsing failure<br>
	 * 							Empty set in case ingredientNodes is empty or NULL
	 */
	@Nullable
	public Set<Ingredient> parseIngredientsFromNodes(
			@Nullable List<Node> ingredientNodes, @Nullable Language language) {
		
		Set<Ingredient> ingredients = new HashSet<>();

		if(ingredientNodes == null)
			return ingredients;

		final CountDownLatch latch = new CountDownLatch(ingredientNodes.size());
		
		ingredients = new HashSet<>();
			
		for(Node ingredientNode : ingredientNodes) {
			try {
				ingredients.add(POOL.submit(
						new AsynchIngredientParser(ingredientNode, language, latch)).invoke());
				
			} catch(Exception e) {
				logParsingFailed(e);
				return null;
			}			
		}
		
		waitForLatch(latch);
		
		if(ingredients.contains(null))
			ingredients.remove(null);
		
		return ingredients;
	}
	
	/**
	 * Tries to parse an ingredient from the given node
	 * @param ingredientNode	The node to parse
	 * @param language			The language to set for the ingredient
	 * @return					The parsed ingredient<br>
	 * 							Empty ingredient in case passed ingredient is NULL<br>
	 * 							NULL in case of parsing failure
	 */
	@Nullable
	public Ingredient parseIngredientFromNode(@Nullable Node ingredientNode,
			@Nullable Language language) {
		
		final CountDownLatch latch = new CountDownLatch(1);
		
		Ingredient ingredient;
		
		if(ingredientNode == null) return null;
		
		try {
			ingredient = POOL.submit(
					new AsynchIngredientParser(ingredientNode, language, latch)).invoke();
			
		} catch(Exception e) {
			logParsingFailed(e);
			return null;
		}
		
		waitForLatch(latch);
		return ingredient;
	}
	
	/**
	 * Tries to parse an ingredient from the given node
	 * @param ingredientNode	The ingredient node to parse
	 * @param languageNode		The language node for the ingredient
	 * @return					The parsed ingredient<br>
	 * 							Empty ingredient in case passed ingredient is NULL<br>
	 * 							NULL in case of parsing failure
	 */
	@Nullable
	public Ingredient parseIngredientFromNode(@Nullable Node ingredientNode,
			@Nullable Node languageNode) {
		
		Language language = LANGUAGE_PARSER.parseLanguageFromNode(languageNode);
		
		return parseIngredientFromNode(ingredientNode, language);
	}
	
	/**
	 * Class for parsing an ingredient from database node asynchronously 
	 * @author Tristan Schroer
	 * @version 2.0.0
	 */
	private class AsynchIngredientParser implements Callable<Ingredient> {
		
		@Nonnull
		private final CountDownLatch countDownLatch;
		@Nullable
		private final Node ingredientNode;
		@Nullable
		private Language language;
		
		/**
		 * Initialize AsynchIngredientParser with the node to parse and a CountDownLatch 
		 * @param ingredientNode	The node to parse
		 * @param countDownLatch	CountDownLatch to count down
		 */
		public AsynchIngredientParser (@Nullable Node ingredientNode, @Nullable Language language,
				@Nonnull CountDownLatch countDownLatch) {
			
			this.countDownLatch = Objects.requireNonNull(countDownLatch, 
					"CountDownLatch must not be NULL");
			
			this.ingredientNode = ingredientNode;
			if(language == null)
				this.language = Language.UNDEF;
			
			else
				this.language = language;
		}

		/**
		 * Logs that a failure causes a NULL return
		 * @param message The message to add to the logging
		 */
		private void logNull(@Nonnull String message) {
			log.log(Level.INFO, "{0}. NULL is returned", Objects.requireNonNull(message));
		}
		
		/**
		 * Tries to parse the ingredient from the node. Returns NULL in case of critical failure
		 */
		@Override
		@Nullable
		public Ingredient call() throws Exception {
			
			//Check if record is NULL
			if(this.ingredientNode == null || !this.ingredientNode.hasLabel(INGREDIENT_LABEL)) {
				this.countDownLatch.countDown();
				logNull("Node is not an ingredient");
				return null;
			}
			
			Ingredient ingredient = new Ingredient(this.ingredientNode.get(NAME_KEY).asString(), language);
			
			//Set ID
			ingredient.setID(this.ingredientNode.id());
			
			//Set checkedForNutrition
			try {
				ingredient.setCheckedForNutrition(this.ingredientNode
						.get(CHECKED_KEY).asBoolean());
								
			} catch(Exception e) {
				ingredient.setCheckedForNutrition(false);					
			}
			
			//Set nutrition
			ingredient.setNutrition(new NutritionParser().parseNutritionFromNode(ingredientNode));
			
			countDownLatch.countDown();
			return ingredient;
		}
	}
}
