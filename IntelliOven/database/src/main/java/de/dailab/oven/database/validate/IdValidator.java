package de.dailab.oven.database.validate;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Class for validating an ID within the system.<br>
 * Does <strong>not</strong> test against a database
 * @author Tristan Schroer
 * @since 14th October, 2019
 * @version 2.0.0
 */
public class IdValidator extends AValidator{

	@Nonnull
	private static final Logger LOGGER = Logger.getLogger(IdValidator.class.getName());
	@Nonnull
	private static final String ID_KEY = "ID";
	
	/**
	 * Checks if the given ID is not NULL and valid
	 * @param id		The ID to test
	 * @return			True if ID is valid<br>False otherwise
	 */
	public boolean isValid(@Nullable Long id) {
		
		//TODO: Change to >0l
		if(id == null || id >= -1l) return true;
		
		else {			
			logInvalid(LOGGER, ID_KEY, id.toString(), "null", ID_KEY);		
			return false;
		}
	}

	/**
	 * @param id	A Long displaying an ID
	 * @return 		<tt>True</tt> if id is valid<br><tt>False</tt> otherwise
	 */
	@Override
	public <T> boolean isValid(T id) {
		
		if(id == null) return true;
		
		if(isCorrectObject(id, Long.class, LOGGER)) return isValid((Long) id);
		
		return false;
	}
}