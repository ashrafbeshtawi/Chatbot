package de.dailab.oven.database.validate;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.dailab.oven.database.validate.model.NameRequest;
import de.dailab.oven.model.data_model.Category;

/**
 * Class to verify categories
 * @author Tristan Schroer
 * @version 2.0.0
 */
public class CategoryValidator extends AValidator{

	@Nonnull
	private static final Logger LOGGER = Logger.getLogger(CategoryValidator.class.getName());
	@Nonnull
	private static final String CATEGORY_KEY = "Category";
	
	/**
	 * Checks if category is not NULL and has proper length
	 * @param category	Category to test
	 * @return			True if category is valid<br>False otherwise
	 */
	public boolean isValid(@Nullable Category category) {
		//Check if category is NULL
		if(category == null || category.isEmpty()) {
			logNull(LOGGER, CATEGORY_KEY + "must not be empty and ");
			return false;
		}
		
		return getValidator(NameValidator.class).isValid(new NameRequest(CATEGORY_KEY, category.getName(), 2));
	}
	
	/**
	 * Checks if categories is not NULL and each category is valid
	 * @param categories	Categories to test
	 * @return				True if categories are valid<br>False otherwise
	 */
	public boolean isValid(@Nullable Set<Category> categories) {
		//Check if categories is NULL
		if(categories == null) {
			logNull(LOGGER, CATEGORY_KEY + "s");
			return false;
		}
		
		//Check each category
		for(Category category : categories) {
			if(!isValid(category))
				return false;
		}
		
		return true;
	}

	/**
	 * @param categoryObject	{@link Category} or Set of {@link Category} to test
	 * @return 					<tt>True</tt> if categoryObject is valid<br><tt>False</tt> otherwise
	 */
	@Override
	public <T> boolean isValid(T categoryObject) {
		if(categoryObject == null) {
			logNull(LOGGER, Category.class.getSimpleName());
			return false;
		}
		
		if(isCorrectObject(categoryObject, HashSet.class, LOGGER)) {
			Set<?> set = (Set<?>) categoryObject;
			if(set.isEmpty()) return true;
			if(isCorrectObject(set.iterator().next(), Category.class, LOGGER)) {
				Set<Category> categories = new HashSet<>();
				set.forEach(c -> categories.add((Category) c));
				return isValid(categories);
			}
		}
		
		if(isCorrectObject(categoryObject, Category.class, LOGGER)) 
			return isValid((Category) categoryObject);
		
		return false;
	}
}