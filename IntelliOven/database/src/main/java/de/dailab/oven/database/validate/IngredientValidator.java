package de.dailab.oven.database.validate;

import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.dailab.oven.database.validate.model.NameRequest;
import de.dailab.oven.model.data_model.Amount;
import de.dailab.oven.model.data_model.Ingredient;
import de.dailab.oven.model.data_model.IngredientWithAmount;

/**
 * Class to verify ingredients
 * @author Tristan Schroer
 * @version 2.0.0
 */
public class IngredientValidator extends AValidator{

	@Nonnull
	private static final Logger LOGGER = Logger.getLogger(IngredientValidator.class.getName());
	@Nonnull
	private static final String INGREDIENT_KEY = "Ingredient";
	@Nonnull
	private static final String INGREDIENTS_KEY = INGREDIENT_KEY + "s";
	@Nonnull
	private static final String INGREDIENT_CURRENT = "current ingredient";
	/**
	 * Checks if ingredient is NULL and valid
	 * @param ingredient	Ingredient to test
	 * @return				True if ingredient is valid<br>False otherwise
	 */
	public boolean isValid(@Nullable Ingredient ingredient) {
		//Check if ingredient is NULL
		if(ingredient == null) {
			logNull(LOGGER, INGREDIENT_KEY);
			return false;
		}
		
		//Check if name and language is valid
		if(!getValidator(NameValidator.class).isValid(new NameRequest(INGREDIENT_KEY, ingredient.getName(), 2))) {
			logInvalid(LOGGER, "Name", INGREDIENT_CURRENT, INGREDIENT_CURRENT, INGREDIENT_KEY);
			return false;
		}
		
		if(!getValidator(LanguageValidator.class).isValid(ingredient.getLanguage())) {
			logInvalid(LOGGER, "Language", ingredient.getName(), INGREDIENT_CURRENT, INGREDIENT_KEY);
			return false;
		}
			
		
		if(!getValidator(IdValidator.class).isValid(ingredient.getID())) {
			logInvalid(LOGGER, "ID", ingredient.getName(), INGREDIENT_CURRENT, INGREDIENT_KEY);
			return false;
		}
		
		if(!getValidator(NutritionValidator.class).isValid(ingredient.getNutrition())) {
			logInvalid(LOGGER, "Nutrition", ingredient.getName(), INGREDIENT_CURRENT, INGREDIENT_KEY);
			return false;
		}
		
		//Check if nutrition is valid 
		return true;
	}
	
	/**
	 * Checks if ingredients are NULL and each is valid
	 * @param ingredients	The ingredients to test
	 * @return				True if all ingredients are valid<br>False otherwise
	 */
	public boolean isValid(@Nullable Set<Ingredient> ingredients) {
	
		//Check if ingredients are NULL
		if(ingredients == null) {
			logNull(LOGGER, INGREDIENTS_KEY);
			return false;
		}
		
		//Check single ingredient
		for(Ingredient ingredient : ingredients) {
			if(!isValid(ingredient))
				return false;
		}
		
		return true;
	}
	
	/**
	 * Checks if ingredient and its amount are valid
	 * @param ingredient	Ingredient to test
	 * @return				True if ingredient is valid<br>False otherwise
	 */
	public boolean isValid(@Nullable IngredientWithAmount ingredient) {
		//Check if ingredient is NULL
		if(ingredient == null) {
			logNull(LOGGER, INGREDIENT_KEY);
			return false;
		}
		
		//Check ingredient
		if(!isValid(ingredient.getIngredient()))
			return false;
		
		//Check amount
		return getValidator(AmountValidator.class).isValid(new Amount(ingredient.getQuantity(), ingredient.getUnit()));
	}
	
	/**
	 * Checks of all ingredients with their amounts are valid
	 * @param ingredients	The ingredients to test
	 * @return				True if all ingredients are valid<br>False if not
	 */
	public boolean isValid(@Nullable List<IngredientWithAmount> ingredients) {
		//Check if ingredients is NULL
		if(ingredients == null) {
			logNull(LOGGER, INGREDIENTS_KEY);
			return false;
		}
		
		//Check each ingredient
		for(IngredientWithAmount entry : ingredients) {
			if(!isValid(entry))
				return false;
		}
		
		return true;
	}

	/**
	 * @param <T>				The type
	 * @param ingredientObject	{@link Ingredient} or Set of {@link Ingredient} to test
	 * @return 					<tt>True</tt> if ingredientObject is valid<br><tt>False</tt> otherwise
	 */
	@Override
	public <T> boolean isValid(T ingredientObject) {

		if(ingredientObject == null) {

			logNull(LOGGER, Ingredient.class.getSimpleName());
			return false;
		}
		
		if(isCorrectObject(ingredientObject, HashSet.class, LOGGER)) {
			Set<?> set = (Set<?>) ingredientObject;
			
			if(set.isEmpty()) return true;
			
			if(isCorrectObject(set.iterator().next(), Ingredient.class, LOGGER)) {
				Set<Ingredient> ingredients = new HashSet<>();
				set.forEach(c -> ingredients.add((Ingredient) c));
				return isValid(ingredients);
			}
		}
		
		if(ingredientObject instanceof List) {
			List<?> list = (List<?>) ingredientObject;
			
			if(list.isEmpty()) return true;
			
			Object entry = list.get(0);
			
			if(isCorrectObject(entry, IngredientWithAmount.class, LOGGER)) {
				List<IngredientWithAmount> ingredientList = new ArrayList<>(list.size());
				list.forEach(i -> ingredientList.add((IngredientWithAmount) i));
				return isValid(ingredientList);
			}
		}
		
		if(isValidEntry(ingredientObject)) return true;
		
		if(isCorrectObject(ingredientObject, Ingredient.class, LOGGER)) 
			return isValid((Ingredient) ingredientObject);
		
		return false;
	}
	
	/**
	 * Checks if object is a valid ingredient entry (SonarFix)
	 * @param <T>				The type
	 * @param ingredientObject	The object to test
	 * @return					<tt>True</tt> if ingredientObject is valid<br><tt>False</tt> otherwise
	 */
	private <T> boolean isValidEntry(T ingredientObject) {
		
		if(isCorrectObject(ingredientObject, Entry.class, LOGGER)) {
			Entry<?, ?> entry = (Entry<?, ?>) ingredientObject;
			
			if(isCorrectObject(entry.getKey(), Ingredient.class, LOGGER)
					&& isCorrectObject(entry.getValue(), Amount.class, LOGGER)) {
				
				Entry<Ingredient, Amount> e = new AbstractMap.SimpleEntry<>(
						(Ingredient) entry.getKey(), (Amount) entry.getValue());
				
				return isValid(e);
			}
		}
		
		return false;
	}
}
