package de.dailab.oven.database.query;

import java.util.List;
import de.dailab.oven.database.exceptions.InputException;
import de.dailab.oven.model.data_model.filters.RecipeFilter;
import de.dailab.oven.model.data_model.Ingredient;
import de.dailab.oven.model.data_model.Recipe;
import de.dailab.oven.model.data_model.User;

public interface IQuery {
	
	
	/**
	 * Uploads a single recipe into the database
	 * @param recipe recipe
	 * @throws InputException in case of invalid inputs for every field of the recipe object
	 * @throws Exception in case of connection to database is lost  
	 */
	void putSingleRecipe(Recipe recipe) throws Exception;
	
	/**
	 * Uploads multiple recipes into the database
	 * @param recipes recipes
	 * @throws InputException in case of invalid inputs for every field of each recipe object
	 * @throws Exception in case of connection to database is lost 
	 */
	void putMultipleRecipes(List<Recipe> recipes) throws Exception;
	
	/**
	 * Use this function with the recipe filter for easier access and querying
	 * @param recipeFilter recipe filter
	 * @return List of Recipes in the database with the given filter
	 * @throws Exception in case of connection to database is lost
	 */
	List<Recipe> getRecipe(RecipeFilter recipeFilter) throws Exception;
	
	/**
	 * This function can be used to check if there are several duplicates of the given recipe in the database 
	 * @param recipe Recipe to look for
	 * @return Recipe if found or Null if not
	 * @throws Exception in case of connection to database is lost 
	 */
	Recipe checkForRecipeDuplicates(Recipe recipe) throws Exception;
	
	/**
	 * Create a whole new User with every attribute
	 * @param user recipe filter
	 * @throws Exception in case of connection to database is lost
	 */
	void putUser(User user) throws Exception;
	
	/**
	 * Returns a list of users. Parameters are optional
	 * @param userName name of user
	 * @param userId user identifier
	 * @return list that should contain no more than one entry
     * @throws Exception in case of connection to database is lost
	 */
	List<User> getUser(String userName, long userId) throws Exception;
	
	
	/**
	 * Adds relationship "replaceable by" between to ingredients
	 * @param ingredientToReplace ingredients to be replaced
	 * @param newIngredient ingredient replacement
	 * @throws Exception in case of connection to database is lost
	 */
	void addReplaceableRelationship(Ingredient ingredientToReplace, Ingredient newIngredient) throws Exception;
}
