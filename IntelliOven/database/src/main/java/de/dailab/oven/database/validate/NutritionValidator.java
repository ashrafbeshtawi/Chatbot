package de.dailab.oven.database.validate;

import java.util.AbstractMap;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.dailab.oven.model.data_model.Amount;
import de.dailab.oven.model.data_model.Nutrition;

/**
 * Class for validating nutrition
 * @author Tristan Schroer
 * @since 14th October, 2019
 * @version 2.0.0
 */
public class NutritionValidator extends AValidator{

	@Nonnull
	private static final Logger LOGGER = Logger.getLogger(NutritionValidator.class.getName());
	@Nonnull
	private static final String NUTRITION_KEY = "Nutrition";
	
	/**
	 * Checks if nutrition is valid
	 * @param nutrition	The nutrition to test
	 * @return			True if nutrition is valid<br>False otherwise
	 */
	public boolean isValid(@Nullable Nutrition nutrition) { 
		//Check if nutrition is NULL
		if(nutrition == null) {
			logNull(LOGGER, NUTRITION_KEY);
			return false;
		}
		
		return true;
	}

	/**
	 * Checks if nutrition and its amount are valid
	 * @param nutrition	The nutrition to test
	 * @return			True if nutrition is valid<br>False otherwise
	 */
	public boolean isValid(@Nullable Entry<Nutrition, Amount> nutrition) {
		
		//Check if nutrition map is NULL
		if(nutrition == null) {
			logNull(LOGGER, NUTRITION_KEY + " entry");
			return false;
		}

		Nutrition singleNutrition = nutrition.getKey();
		
		//Check if nutrition is valid
		if(!isValid(nutrition.getKey()))
			return false;
		
		//Check if amount is valid
		if(!getValidator(AmountValidator.class).isValid(nutrition.getValue())) {
			logInvalid(LOGGER, "Amount", singleNutrition.name(), "current nutrition", 
					NUTRITION_KEY);
			
			return false;
		}
		
		return true;
	}
	
	/**
	 * Checks if all nutrition and their amount are valid
	 * @param nutrition	The nutrition to test
	 * @return			True if nutrition is valid<br>False otherwise
	 */
	public boolean isValid(@Nullable Map<Nutrition, Amount> nutrition) {
		//Check if nutrition map is NULL
		if(nutrition == null) {
			logNull(LOGGER, NUTRITION_KEY + " map");
			return false;
		}
		
		//Check each nutrition entry
		for(Entry<Nutrition, Amount> entry : nutrition.entrySet()) {
			if(!isValid(entry))
				return false;
		}
		
		return true;
	}

	/**
	 * @param nutritionObject	{@link Nutrition} or {@link Map} of {@link Nutrition} and 
	 * 							{@link Amount} or simple {@link Entry} of such a {@link Map} to test
	 * @return 					<tt>True</tt> if nutritionObject is valid<br><tt>False</tt> otherwise
	 */
	@Override
	public <T> boolean isValid(T nutritionObject) {
		if(nutritionObject == null) {
			logNull(LOGGER, NUTRITION_KEY);
			return false;
		}
		
		if(isCorrectObject(nutritionObject, EnumMap.class, LOGGER)) {
			Map<?, ?> map = (Map<?, ?>) nutritionObject;
			
			if(map.isEmpty()) return true;
			
			Entry<?, ?> entry = map.entrySet().iterator().next();
			
			if(isCorrectObject(entry.getKey(), Nutrition.class, LOGGER)
					&& isCorrectObject(entry.getValue(), Amount.class, LOGGER)) {
				Map<Nutrition, Amount> nutritionMap = new EnumMap<>(Nutrition.class);
				map.entrySet().forEach(c -> nutritionMap.put((Nutrition) c.getKey(),
						(Amount) c.getValue()));
				
				return isValid(nutritionMap);
			}
		}
		
		if(isCorrectObject(nutritionObject, Entry.class, LOGGER)) {
			Entry<?, ?> entry = (Entry<?, ?>) nutritionObject;
			
			if(isCorrectObject(entry.getKey(), Nutrition.class, LOGGER)
					&& isCorrectObject(entry.getValue(), Amount.class, LOGGER)) {
				
				Entry<Nutrition, Amount> e = new AbstractMap.SimpleEntry<>(
						(Nutrition) entry.getKey(), (Amount) entry.getValue());
				
				return isValid(e);
			}
		}
		
		if(isCorrectObject(nutritionObject, Nutrition.class, LOGGER)) 
			return isValid((Nutrition) nutritionObject);
		
		return false;
	}
}