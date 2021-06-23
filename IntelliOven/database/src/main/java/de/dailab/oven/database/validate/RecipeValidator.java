package de.dailab.oven.database.validate;

import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.dailab.oven.model.data_model.*;
import org.apache.commons.validator.routines.UrlValidator;

import de.dailab.oven.database.validate.model.NameRequest;
import zone.bot.vici.Language;

/**
 * Class for validating recipes
 * @author Tristan Schroer
 * @version 2.0.0
 */
public class RecipeValidator extends AValidator{

	@Nonnull
	private static final Logger LOGGER = Logger.getLogger(RecipeValidator.class.getName());
	@Nonnull
	private static final UrlValidator URL_VALIDATOR = new UrlValidator();
	
	@Nonnull
	private static final String AUTHOR = "Author";
	@Nonnull
	private static final String CATEGORIES = "Categories";
	@Nonnull
	private static final String DURATION = "Duration";
	@Nonnull
	private static final String FOOD_LABEL = "Food label";
	@Nonnull
	private static final String IMAGE_PATH = "Image path";
	@Nonnull
	private static final String INSTRUCTIONS = "Instructions";
	@Nonnull
	private static final String ORIGINAL_SERVINGS = "Original Servings";
	@Nonnull
	private static final String RECIPE_KEY = "Recipe";
	@Nonnull
	private static final String RECIPE_CURRENT = "current " + RECIPE_KEY;
	@Nonnull
	private static final String RECIPE_NAME = RECIPE_KEY + " name";
	
	/**
	 * Checks if all attributes of the given recipe are valid in order 
	 * to store them in the database
	 * @param recipe	The recipe to test
	 * @return			True if recipe is valid<br>False otherwise
	 */
	public boolean isValid(@Nullable Recipe recipe) {
		
		//Check if recipe is NULL
		if(recipe == null) {
			logNull(LOGGER, RECIPE_KEY);
			return false;
		}
		
		return (firstRecipePartValid(recipe) && secondRecipePartValid(recipe));
	}
	
	/**
	 * Checks author, language, name, durations, food label and categories of the recipe (SonarFix)
	 * @param recipe	The recipe to test
	 * @return			True if all attributes are valid<br>False otherwise
	 */
	private boolean firstRecipePartValid(@Nonnull Recipe recipe) {
		String recipeName = recipe.getName();
		
		//Check for valid author
		if(!isAuthorValid(recipe.getAuthor())) {
			logInvalid(LOGGER, AUTHOR, recipeName, RECIPE_CURRENT, RECIPE_KEY);
			return false;
		}
		
		//Check for valid language
		else if(!isLanguageValid(recipe.getLanguage())) {

			logInvalid(LOGGER, "Language", recipeName, RECIPE_CURRENT, RECIPE_KEY);
			return false;
		}
		
		//Check for valid recipe name
		else if(!isRecipeNameValid(recipe.getName())) {
				
			logInvalid(LOGGER, RECIPE_NAME, recipeName, RECIPE_CURRENT, RECIPE_KEY);
			return false;
		}
		
		//Check for valid categories
		else if(!isCategoriesValid(recipe.getCategories())) {
			
			logInvalid(LOGGER, CATEGORIES, recipeName, RECIPE_CURRENT, RECIPE_KEY);
			return false;
		}
		
		//Check for valid duration
		if(!isDurationValid(recipe.totalDuration())) {
				
			logInvalid(LOGGER, DURATION, recipeName, RECIPE_CURRENT, RECIPE_KEY);
			return false;
		}
		
		//Check for valid durations
		else if(!isDurationsValid(recipe.getDurations())) {
			
			logInvalid(LOGGER, "Durations", recipeName, RECIPE_CURRENT, RECIPE_KEY);
			return false;
		}
		
		//Check if food label is valid
		else if(!isFoodLabelValid(recipe.getFoodLabel())) {
			
			logInvalid(LOGGER, FOOD_LABEL, recipeName, RECIPE_CURRENT, RECIPE_KEY);
			return false;
		}
		
		return true;
	}
	
	/**
	 * Checks ID, imagePath, ingredients, instructions, original servings, URL and ratings 
	 * of the recipe (SonarFix)
	 * @param recipe	The recipe to test
	 * @return			True if all attributes are valid<br>False otherwise
	 */
	private boolean secondRecipePartValid(@Nonnull Recipe recipe) {
		
		String recipeName = recipe.getName();
		
		//Check if ID is valid
		if(!isIdValid(recipe.getId())) {

			logInvalid(LOGGER, "ID", recipeName, RECIPE_CURRENT, RECIPE_KEY);
			return false;
		}
		
		//Checking image files is not necessary
		//Check if image path exists
		else if(!isImagePathValid(recipe.getImagePath())) {

			logInvalid(LOGGER, IMAGE_PATH, recipeName, RECIPE_CURRENT, RECIPE_KEY);
			return false;
		}
		
		//Check if ingredients are valid
		else if(!isIngredientsValid(recipe.getIngredients())) {
			
			logInvalid(LOGGER, "Ingredients", recipeName, RECIPE_CURRENT, RECIPE_KEY);
			return false;
		}

		//Check instructions
		else if(!isInstructionsValid(recipe.getInstructions())) {
	
			logInvalid(LOGGER, INSTRUCTIONS, recipeName, RECIPE_CURRENT, RECIPE_KEY);
			return false;
		}
		
		//Check original servings
		else if(!isOriginalServingsValid(recipe.getOriginalServings())) {
			
			logInvalid(LOGGER, ORIGINAL_SERVINGS, recipeName, RECIPE_CURRENT, RECIPE_KEY);
			return false;
		}
		
		//Check URL
		else if(!isUrlValid(recipe.getUrl())) {
		
			logInvalid(LOGGER, "URL", recipeName, RECIPE_CURRENT, RECIPE_KEY);
			return false;
		}
		
		//Check user ratings
		else if(!isUserRatingsValid(recipe.getUserRatings())) {
			
			logInvalid(LOGGER, "User ratings", recipeName, RECIPE_CURRENT, RECIPE_KEY);
			return false;
		}
		
		return true;
	}
	/**
	 * Checks if author is not NULL and has at least three characters
	 * @param author	The recipes author
	 * @return			True if author is valid<br>False otherwise
	 */
	public boolean isAuthorValid(@Nullable String author) {
		return getValidator(NameValidator.class).isValid(new NameRequest(AUTHOR, author, 2));
	}
	
	/**
	 * Checks if language is not NULL and language is not Language.UNDEF
	 * @param language 	The recipes language
	 * @return			True if language is valid<br>False otherwise
	 */
	public boolean isLanguageValid(@Nullable Language language) {
		return getValidator(LanguageValidator.class).isValid(language);			
	}
	
	/**
	 * Checks if name is not NULL and has at least three characters
	 * @param name	The recipes name
	 * @return		True if name is valid<br>False otherwise
	 */
	public boolean isRecipeNameValid(@Nullable String name) {
		return getValidator(NameValidator.class).isValid(new NameRequest(RECIPE_NAME, name, 2));
	}

	public boolean isCategoriesValid(Set<Category> categories) {
		return getValidator(CategoryValidator.class).isValid(categories);
	}
	
	/**
	 * Checks if the given duration is not NULL and not negative
	 * @param duration 	The duration to check
	 * @return			True if duration is valid<br>False otherwise
	 */
	public boolean isDurationValid(@Nullable Duration duration) {
		if(duration == null) {
			logNull(LOGGER, DURATION);
			return false;
		}
		else if(duration.isNegative()) {
			LOGGER.log(Level.INFO, "{0} is invalid since it is negative", duration);
			return false;
		}
		return true;
	}
	
	/**
	 * Checks if all durations and their names are valid
	 * @param durations	The durations to test
	 * @return			True if all durations are valid<br>False otherwise
	 */
	public boolean isDurationsValid(@Nullable Map<String, Duration> durations) {
		if(durations == null) {
			logNull(LOGGER, "Durations");
			return false;
		}
		
		for(Entry<String,Duration> duration : durations.entrySet()) {
			if(!isDurationValid(duration.getValue())
					|| !getValidator(NameValidator.class).isValid(
							new NameRequest(DURATION, duration.getKey(), 2)))
				
				return false;
		}
		
		return true;
	}
	
	/**
	 * Checks if food label is NULL
	 * @param foodLabel	The food label to test
	 * @return	True if food label is not NULL<br>False otherwise
	 */
	public boolean isFoodLabelValid(@Nullable FoodLabel foodLabel) {
		if(foodLabel == null) {
			logNull(LOGGER, FOOD_LABEL);
			return false;
		}
		return true;
	}
	
	/**
	 * Checks if ID is not NULL and valid
	 * @param id 	The recipe ID
	 * @return		True if ID is valid<br>False otherwise
	 */
	public boolean isIdValid(@Nullable Long id) {
		return getValidator(IdValidator.class).isValid(id);
	}
	
	/**
	 * Checks if image path is not NULL and valid if it has been set
	 * @param imagePath	The image path to test
	 * @return			True if path is valid<br>False otherwise
	 */
	public boolean isImagePathValid(@Nullable String imagePath) {
		//Check NULL
		if(imagePath == null) {
			logNull(LOGGER, IMAGE_PATH);
			return false;
		}
		
		//Check if it is not set
		if(imagePath.isEmpty())
			return true;
		
		//Check if file exists
		if(!new File(imagePath).exists()) {
			LOGGER.log(Level.INFO, "Image file for path {0} does not exist", imagePath);
			return false;
		}
		
		return true;
	}
	
	/**
	 * Checks if ingredients and their amounts are valid
	 * @param ingredients 	The ingredient map to test
	 * @return 				True if ingredients are valid<br>False otherwise
	 */
	public boolean isIngredientsValid(@Nullable List<IngredientWithAmount> ingredients) {
		if(ingredients == null || ingredients.isEmpty()) {
			logInvalid(LOGGER, "Ingredients", "", "current" + RECIPE_KEY, RECIPE_KEY);
			return false;
		}
		
		return getValidator(IngredientValidator.class).isValid(ingredients);
	}
	
	/**
	 * Checks if instructions are not NULL and not empty
	 * @param instructions	The instructions to test
	 * @return				True if instructions are valid<br>False otherwise
	 */
	public boolean isInstructionsValid(@Nullable List<String> instructions) {
		//Check if instructions is NULL
		if(instructions == null) {
			logNull(LOGGER, INSTRUCTIONS);
			return false;
		}
		
		//Check if instructions is empty
		if(instructions.isEmpty()) {
			LOGGER.log(Level.INFO, "Instructions must not be empty");
			return false;
		}
		
		return true;
	}
	
	/**
	 * Checks if original servings is a valid number
	 * @param originalServings	The original servings to test
	 * @return					True if original servings is valid<br>False otherwise
	 */
	public boolean isOriginalServingsValid(@Nullable Integer originalServings) {
		//Check if original servings is NULL
		if(originalServings == null) {
			logNull(LOGGER, ORIGINAL_SERVINGS);
			return false;
		}
		//Check if original servings is equal or lower zero
		else if (originalServings <= 0){
			LOGGER.log(Level.INFO, "Original Servings can not be {0}", originalServings);
			return false;
		}
		
		return true;
	}
	
	/**
	 * Checks if URL has been set, is NULL and valid
	 * @param url	The URL to test
	 * @return		True if URL is valid<br>False otherwise
	 */
	public boolean isUrlValid(@Nullable String url) {
		//Check if URL is NULL
		if(url == null) {
			logNull(LOGGER, "URL");
			return false;
		}
		
		//Check if it has not been set
		if(url.isEmpty())
			return true;
		
		//Validate otherwise
		return URL_VALIDATOR.isValid(url);
	}
	
	/**
	 * Checks if user ratings are not NULL and valid
	 * @param userRatings	The ratings to test
	 * @return				True if ratings are valid<br>False otherwise
	 */
	public boolean isUserRatingsValid(@Nullable Map<Long, Integer> userRatings) {
		return getValidator(RatingValidator.class).isValid(userRatings);
	}

	@Override
	public <T> boolean isValid(T recipeObject) {
		
		if(isCorrectObject(recipeObject, Recipe.class, LOGGER)) 
			return isValid((Recipe) recipeObject);
		
		return false;
	}
}
