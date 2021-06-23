package de.dailab.oven.database.validate;

import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.dailab.oven.model.data_model.Ingredient;
import de.dailab.oven.database.validate.model.NameRequest;
import de.dailab.oven.model.data_model.Category;
import de.dailab.oven.model.data_model.User;
import zone.bot.vici.Language;

/**
 * Class to verify users
 * @author Tristan Schroer
 * @version 2.0.0
 */
public class UserValidator extends AValidator{

	@Nonnull
	private static final Logger LOGGER = Logger.getLogger(UserValidator.class.getName());
	@Nonnull
	private static final String USER_KEY = "User";
	@Nonnull
	private static final String CATEGORIES = "Categories";
	@Nonnull
	private static final String USER_NAME = "User name";
	@Nonnull
	private static final String DEFAULT_USER = "current user";
	
	/**
	 * Checks if all user attributes are valid
	 * @param userObject	The {@link User} to test
	 * @return				True if user is valid<br>False otherwise
	 */
	@Override
	public <T> boolean isValid(@Nullable T userObject) {
		
		//Check if user is NULL
		if(userObject == null) {
			logNull(LOGGER, USER_KEY);
			return false;
		}
		
		if(!isCorrectObject(userObject, User.class, LOGGER)) return false;
		
		User user = (User) userObject;
		
		String userName = user.getName();
		
		//Check user name
		if(!isUserNameValid(user.getName())) {
				
			logInvalid(LOGGER, USER_NAME, userName, DEFAULT_USER, USER_KEY);
			return false;
		}
		
		//Check user ID
		if(!isUserIdValid(user.getId())) {
			
			logInvalid(LOGGER, "UserID", userName, DEFAULT_USER, USER_KEY);
			return false;
		}
		
		//Check preferred categories
		else if(!isPreferredCategoriesValid(user.getPreferredCategories())) {
				logInvalid(LOGGER, "Preferred " + CATEGORIES, userName, DEFAULT_USER, USER_KEY);
				return false;
		}
		
		//Check ingredients
		else if(!areIngredientsValid(user)) return false;

		//Check if spoken languages are valid
		else if(!isSpokenLanguagesValid(user.getSpokenLanguages())) {
			
			logInvalid(LOGGER, "Spoken languages", userName, DEFAULT_USER, USER_KEY);
			return false;
		}
		
		//Check recipe ratings
		else if(!isRecipeRatingsValid(user.getRecipeRatings())) {
				
			logInvalid(LOGGER, "Recipe ratings", userName, DEFAULT_USER, USER_KEY);
			return false;
		}
		
		return true;
	}
	
	
	/**
	 * Checks if all ingredients stored in user instance are valid
	 * @param user 	The user to test
	 * @return		<tt>True</tt> if all ingredients are valid<br><tt>False</tt> otherwise
	 */
	private boolean areIngredientsValid(@Nonnull User user) {
		
		String userName = user.getName();
		
		//Check incompatible ingredients
		if(!isIncompatibleIngredientsValid(user.getIncompatibleIngredients())) {
				
			logInvalid(LOGGER, "Incompatible ingrediets", userName, DEFAULT_USER, USER_KEY);
			return false;
		}
		
		//Check liked ingredients
		else if(!isLikedIngredientsValid(user.getLikesIngredients())) {
			
			logInvalid(LOGGER, "Liked ingrediets", userName, DEFAULT_USER, USER_KEY);
			return false;
		}
		
		return true;
	}
	
	
	/**
	 * Checks if the user name is NULL and valid
	 * @param userName	The user name to test
	 * @return			True if user name is valid<br>False otherwise
	 */
	public boolean isUserNameValid(@Nullable String userName) {
		return getValidator(NameValidator.class).isValid(new NameRequest(USER_NAME, userName, 3));
	}
	
	/**
	 * Checks if ID is not NULL and valid
	 * @param id 	The user ID
	 * @return		True if ID is valid<br>False otherwise
	 */
	public boolean isUserIdValid(@Nullable Long id) {
		return getValidator(IdValidator.class).isValid(id);
	}
	
	/**
	 * Checks if categories is not NULL and each category has at least two characters
	 * @param name	The preferred categories
	 * @return		True if all categories are valid<br>False otherwise
	 */
	public boolean isPreferredCategoriesValid(Set<Category> categories) {
		return getValidator(CategoryValidator.class).isValid(categories);
	}
	
	/**
	 * Checks if incompatible ingredients are valid
	 * @param incompatibleIngredients 	Ingredients to test
	 * @return							True if ingredients are valid<br>False otherwise
	 */
	public boolean isIncompatibleIngredientsValid(Set<Ingredient> incompatibleIngredients) {
		return getValidator(IngredientValidator.class).isValid(incompatibleIngredients);
	}
	
	/**
	 * Checks if liked ingredients are valid
	 * @param likedIngredients 	Ingredients to test
	 * @return					True if ingredients are valid<br>False otherwise
	 */
	public boolean isLikedIngredientsValid(Set<Ingredient> likedIngredients) {
		return getValidator(IngredientValidator.class).isValid(likedIngredients);
	}
	
	/**
	 * Checks if all languages are valid
	 * @param languages	The languages to test
	 * @return			True if all languages are valid<br>False otherwise
	 */
	public boolean isSpokenLanguagesValid(Set<Language> languages) {
		return getValidator(LanguageValidator.class).isValid(languages);
	}
	
	/**
	 * Checks if user ratings are not NULL and valid
	 * @param recipeRatings	The ratings to test
	 * @return				True if ratings are valid<br>False otherwise
	 */
	public boolean isRecipeRatingsValid(@Nullable Map<Long, Integer> recipeRatings) {
		return getValidator(RatingValidator.class).isValid(recipeRatings);
	}
}
