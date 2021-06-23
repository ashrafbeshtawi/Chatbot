package de.dailab.oven.database.validate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import de.dailab.oven.model.data_model.Amount;
import de.dailab.oven.model.data_model.Unit;

public class AmountValidatorTest {

	private static final AmountValidator AMOUNT_VALIDATOR = new AmountValidator();
	
	@Test
	public void isValidAmountTest() {
		Amount invalidQuantity = new Amount(-5.0f, Unit.CENTIMETER);
		
		Amount invalidUnit = new Amount();
		invalidUnit.setQuantity(2.0f);
		String nullString = null;
		invalidUnit.setUnit(nullString);
		
		Amount validAmount = new Amount(5.0f, Unit.UNDEF);
		
		assertFalse(AMOUNT_VALIDATOR.isValid(null));
		assertFalse(AMOUNT_VALIDATOR.isValid(invalidQuantity));
		assertTrue(AMOUNT_VALIDATOR.isValid(invalidUnit));
		assertTrue(AMOUNT_VALIDATOR.isValid(validAmount));
	}

}