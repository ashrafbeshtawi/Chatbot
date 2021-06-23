package de.dailab.oven.database.validate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class IdValidatorTest {

	private static final IdValidator ID_VALIDATOR = new IdValidator();
	
	@Test
	public void isValidIdTest() {
		Long nullId = null;
		Long notYetSet = -1l;
		Long invalid = -10l;
		Long valid = 1l;
		
		assertTrue(ID_VALIDATOR.isValid(nullId));
		assertTrue(ID_VALIDATOR.isValid(notYetSet));
		assertFalse(ID_VALIDATOR.isValid(invalid));
		assertTrue(ID_VALIDATOR.isValid(valid));
	}
}
