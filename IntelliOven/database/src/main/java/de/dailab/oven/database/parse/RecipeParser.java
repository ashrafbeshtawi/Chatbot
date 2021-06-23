package de.dailab.oven.database.parse;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.dailab.oven.model.data_model.*;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecipeParser implements Callable<Recipe>{

	private static final String RECIPE_KEY = "recipe";
	private static final String DURATION_KEY = "durations";
	private static final String INGREDIENT_KEY = "ingredients";
	private static final String RATING_KEY = "ratings";
	@Nullable
	private final Record record;
	private final CountDownLatch countDownLatch;
	private static final Logger LOGGER = LoggerFactory.getLogger(RecipeParser.class);
	
	public RecipeParser(@Nullable final Record record, @Nonnull final CountDownLatch countDownLatch) {
		this.countDownLatch = Objects.requireNonNull(countDownLatch, "CountDownLatch must not be NULL");
		this.record = record;
	}
	
	@Override
	public Recipe call() throws Exception {
		final Recipe recipe = new Recipe();
		if(this.record == null) {
			this.countDownLatch.countDown();
			return null;
		}
		else {
			Node recipeNode = null;
			Map<String, Object> recipeMap = null;		
			try {
				recipeNode = this.record.get(RECIPE_KEY).asNode();
				recipeMap = recipeNode.asMap();					
			} catch (final Exception e) {
				this.countDownLatch.countDown();
				return null;
			}
			
			if(recipeMap != null && recipeNode != null) {
				
				//Set name
				recipe.setName(recipeMap.get("name").toString());
				
				
				//Set total duration
				Duration duration = Duration.ZERO;
				final long isoDuration;
				try {
					isoDuration = this.record.get(RECIPE_KEY).get("duration").asIsoDuration().seconds();
					duration = duration.plusSeconds(isoDuration);
				} catch (final Exception e1) {
					try {
						duration = Duration.parse(recipeMap.get("duration").toString());
						recipe.setDuration(duration);
						recipe.addDurationToListOfDurations("total", duration);
					} catch (final Exception e2) {
						LOGGER.info(e1.getLocalizedMessage(), e1.getCause());
						LOGGER.info(e2.getLocalizedMessage(), e2.getCause());
					}
				}
				
				recipe.setDuration(duration);
				recipe.addDurationToListOfDurations("total", duration);
				//Add different durations to recipe
				if(this.record.containsKey(DURATION_KEY)) {
					final List<Object> durations = this.record.get(DURATION_KEY).asList();
					final int numberOfDurations = durations.size();
					
					for(int i = 0; i < numberOfDurations; i++) {
						try {
							if(!this.record.get(DURATION_KEY).get(i).get(0).asString().toLowerCase().contentEquals("null")) {
								recipe.addDurationToListOfDurations(this.record.get(DURATION_KEY).get(i).get(0).get("name").asString(), Duration.parse(this.record.get(DURATION_KEY).get(i).get(1).get("time").toString()));
							}
						} catch (final Exception e) {
							try {
								if(!this.record.get(DURATION_KEY).get(i).get(0).asNode().asMap().get("name").toString().toLowerCase().contentEquals("null")) {
									recipe.addDurationToListOfDurations(this.record.get(DURATION_KEY).get(i).get(0).asNode().asMap().get("name").toString(), Duration.parse(this.record.get(DURATION_KEY).get(i).get(1).asRelationship().asMap().get("time").toString()));
								}
							} catch (final Exception e2) {
								LOGGER.info(e2.getLocalizedMessage(), e2.getCause());
							}
						}
						
					}
				}
				
				//Set ID
				recipe.setId(this.record.get(RECIPE_KEY).asNode().id());
				
				//Set Instructions

				try {
					final List<Object> instructionObjects = recipeNode.get("instructions").asList();
					final List<String> instructions = new ArrayList<>();
					for(final Object inst : instructionObjects) {
						instructions.add(inst.toString());
					}
					recipe.setInstructions(instructions);					
				} catch (final Exception e) {LOGGER.info(e.getLocalizedMessage(), e.getCause());}
				
				//Set originalServings
				//Direct casting to integer won't work
				final long servings = (long) recipeMap.get("originalServings");
				recipe.setOriginalServings((int) servings);
				
				//Set URL
				recipe.setUrl(recipeMap.get("url").toString());
				
				//Set image path
				if(recipeMap.containsKey("imagePath")) {
					recipe.setImagePath(recipeMap.get("imagePath").toString());					
				}
				
				//Add categories
				if(this.record.containsKey("categories")) {
					try {
						final List<Object> categories = this.record.get("categories").asList();
						for(final Object category : categories) {
							recipe.addCategory(new CategoryParser().parseCategoryFromNode((Node) category));						
						}						
					} catch (final Exception e) {LOGGER.info(e.getLocalizedMessage(), e.getCause());}
				}
				//Set author
				recipe.setAuthor(this.record.get("author.name").asString());
				
				//Set language
				recipe.setLanguage(this.record.get("language.name").asString());
				
				//Set oven settings
				if(recipeMap.containsKey("ovenSettings")) {
					try {
						final List<Object> setObj = recipeNode.get("ovenSettings").asList();
						
						for(int i = 0; i < setObj.size(); i ++) {
							recipe.addOvenSetting(i, setObj.get(i).toString());
						}						
					} catch (final Exception e) {LOGGER.info(e.getLocalizedMessage(), e.getCause());}
				}
				
				//Set food-label
				final String foodlabel = this.record.get("foodlabel.name").asString();
				recipe.setFoodLabel(FoodLabel.valueOf(foodlabel.toUpperCase()));
				
				//Set ingredients with amount
				try {
					
					final List<Object> ingredientsWithAmount = this.record.get(INGREDIENT_KEY).asList();
					final int numberOfIngredientItems = ingredientsWithAmount.size();
					
					final List<String> addedIngredients = new ArrayList<>();
					IngredientParser iP = new IngredientParser();
					
					for(int i = 0; i < numberOfIngredientItems; i++) {
						//Prepare ingredient
						
						final Ingredient ingredient = iP.parseIngredientFromNode(this.record.get(INGREDIENT_KEY).get(i).get(0).asNode(), recipe.getLanguage());
						
						if(ingredient == null || addedIngredients.contains(ingredient.getName())) continue;
						
						addedIngredients.add(ingredient.getName());
						
						//Set amount of ingredient
						final Relationship amountRelationship = this.record.get(INGREDIENT_KEY).get(i).get(1).asRelationship();
						if(amountRelationship.containsKey("attributes")) {
							List<Object> attr = amountRelationship.get("attribtues").asList();
							attr.forEach(a -> ingredient.addAttribute(a.toString()));
						}
						final IngredientWithAmount ingred = new IngredientWithAmount(ingredient, amountRelationship.get("amount").asFloat(), Unit.valueOf(amountRelationship.get("unit").asString()));
						ingredient.setCheckedForNutrition(true);
						recipe.addIngredientToListOfIngredients(ingred);
					}				
				} catch(final Exception e) {LOGGER.info(e.getLocalizedMessage(), e.getCause());}
				
				//Set userRatings		
				if(this.record.containsKey(RATING_KEY)) {
					try {
						final List<Object> ratings = this.record.get(RATING_KEY).asList();
						final int numberOfRatings = ratings.size();
						
						for(int i = 0; i < numberOfRatings; i++) {
							recipe.addUserRating(this.record.get(RATING_KEY).get(i).get(0).asLong(), this.record.get(RATING_KEY).get(i).get(1).asInt());
						}											
					} catch(final Exception e) {
						LOGGER.info(e.getLocalizedMessage(), e.getCause());
					}
				}
			}
						
		}
		this.countDownLatch.countDown();
		return recipe;
	}

}
