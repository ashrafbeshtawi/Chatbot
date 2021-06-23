package de.dailab.oven.database.validate;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.dailab.oven.model.data_model.Amount;

/**
 * Class to verify amounts
 * @author Tristan Schroer
 * @version 2.0.0
 */
public class AmountValidator extends AValidator{

	@Nonnull
	private static final Logger LOGGER = Logger.getLogger(AmountValidator.class.getName());
	@Nonnull
	private static final String AMOUNT_KEY = "Amount";
	
	/**
	 * Checks if amount is valid
	 * @param amount	The amount to test
	 * @return			True if amount is valid<br>False otherwise
	 */
	public boolean isValid(@Nullable Amount amount) {
		//Check if amount is NULL
		if(amount == null) {
			logNull(LOGGER, AMOUNT_KEY);
			return false;
		}
		
		//Check if quantity is valid
		else if(amount.getQuantity() < - 0.001f) {
			logInvalid(LOGGER, "Quantitiy " + amount.getQuantity(), "", "", AMOUNT_KEY);
			return false;
		}
		
		return true;
	}

	/**
	 * Checks if amount is valid
	 * @param amount	The amount to test
	 * @return			True if amount is valid<br>False otherwise
	 */
	@Override
	public <T> boolean isValid(T amount) {
		if(amount == null) {
			logNull(LOGGER, Amount.class.getSimpleName());
			return false;
		}
		
		if(!isCorrectObject(amount, Amount.class, LOGGER)) return false;
		
		Amount a = (Amount) amount;
		
		return isValid(a);
	}
}