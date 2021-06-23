package de.dailab.oven.database.validate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Test;

public class RatingValidatorTest {

	private static final RatingValidator RATING_VALIDATOR = new RatingValidator();
	
	@Test
	public void isValidRatingTest() {
		Integer nullRating = null;
		assertFalse(RATING_VALIDATOR.isValid(nullRating));
		assertFalse(RATING_VALIDATOR.isValid(-11));
		assertFalse(RATING_VALIDATOR.isValid(11));
		assertTrue(RATING_VALIDATOR.isValid(0));
	}
	
	@Test
	public void isValidRatingEntryTest() {
		Long nullId = null;
		Long invalidId = -10l;
		Long validId = 10l;
		
		Integer nullRating = null;
		Integer invalidRating = -11;
		Integer validRating = 5;
		
		
		Entry<Long, Integer> nullEntry = null;
		assertFalse(RATING_VALIDATOR.isValid(nullEntry));
		
		Map<Long, Integer> nullMap = new HashMap<>();
		nullMap.put(nullId, nullRating);
		nullMap.put(validId, nullRating);
		for(Entry<Long, Integer> entry : nullMap.entrySet()) {
			assertFalse(RATING_VALIDATOR.isValid(entry));
		}
		nullMap.put(nullId, validRating);
		for(Entry<Long, Integer> entry : nullMap.entrySet()) {
			assertFalse(RATING_VALIDATOR.isValid(entry));
		}
		
		Map<Long, Integer> invalidMap = new HashMap<>();
		invalidMap.put(validId, invalidRating);
		invalidMap.put(invalidId, validRating);
		for(Entry<Long, Integer> entry : invalidMap.entrySet()) {
			assertFalse(RATING_VALIDATOR.isValid(entry));
		}
		
		Map<Long, Integer> validMap = new HashMap<>();
		validMap.put(validId, validRating);
		for(Entry<Long, Integer> entry : validMap.entrySet()) {
			assertTrue(RATING_VALIDATOR.isValid(entry));
		}
	}
	
	@Test
	public void isValidMapTest() {
		Map<Long, Integer> nullMap1 = null;
		assertFalse(RATING_VALIDATOR.isValid(nullMap1));
		
		Long nullId = null;
		Long invalidId = -10l;
		Long validId = 10l;
		
		Integer nullRating = null;
		Integer invalidRating = -11;
		Integer validRating = 5;
		
		Map<Long, Integer> nullMap = new HashMap<>();
		nullMap.put(nullId, nullRating);
		nullMap.put(validId, nullRating);
		assertFalse(RATING_VALIDATOR.isValid(nullMap));
		
		Map<Long, Integer> invalidMap = new HashMap<>();
		invalidMap.put(validId, invalidRating);
		invalidMap.put(invalidId, validRating);
		assertFalse(RATING_VALIDATOR.isValid(invalidMap));
		
		Map<Long, Integer> validMap = new HashMap<>();
		validMap.put(validId, validRating);
		assertTrue(RATING_VALIDATOR.isValid(validMap));
	}
}