package de.dailab.oven.database.validate;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.dailab.oven.database.validate.model.NameRequest;

/**
 * Class for validating names based on different criteria
 * @author Tristan Schroer
 * @since 14th October, 2019
 * @version 2.0.0
 */
public class NameValidator extends AValidator{

	@Nonnull
	private static final Logger LOGGER = Logger.getLogger(NameValidator.class.getName());
	
	/**
	 * Checks if the given name is not NULL and valid
	 * @param name		The name to test
	 * @param variable	The variable which defines the name (Author, Category, ...)
	 * @param minLength	The minimum length for the given name
	 * @return			True if name is valid<br>False otherwise
	 */
	public boolean isValid(@Nullable String name, @Nullable String variable, 
			@Nullable Integer minLength) {
		
		//Set variable if it is not set
		if(variable == null || variable.isEmpty())
			variable = "Unkown";
		
		//Check if name is NULL
		if(name == null) {
			LOGGER.log(Level.INFO, "{0} must not be NULL", variable);
			return false;
		}
		
		//Check if length is negative
		if(minLength == null || minLength < 0) {
			LOGGER.log(Level.INFO, "The minimum length for {0} is negative or NULL. "
					+ "Name is assumed to be valid", variable);
			
			return true;
		}
		
		//Check if name has appropriate length
		if(name.length() < minLength) {
			LOGGER.log(Level.INFO, "Name for {0} is too short.", variable);
			return false;
		}
		
		return true;
	}

	/**
	 * Send an "isValid"-Request encapsulated within a NameRequest-Object
	 * @param nameRequestObject The {@link NameRequest} with specified requests
	 * @return 					<tt>True</tt> if name is valid<br><tt>False</tt> otherwise 
	 */
	@Override
	public <T> boolean isValid(@Nullable T nameRequestObject) {
		
		if(nameRequestObject == null) {
			logNull(LOGGER, NameRequest.class.getSimpleName());
			return false;
		}
		
		if(!isCorrectObject(nameRequestObject, NameRequest.class, LOGGER)) return false;
		
		NameRequest nR = (NameRequest) nameRequestObject;
		
		return isValid(nR.getValue(), nR.getKey(), nR.getMaxLength());
	}
}