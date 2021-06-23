package de.dailab.oven.database.validate;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Class for verifying ratings
 * @author Tristan Schroer
 * @version 2.0.0
 */
public class RatingValidator extends AValidator{

	@Nonnull
	private static final Logger LOGGER = Logger.getLogger(RatingValidator.class.getName());
	@Nonnull
	private static final String RATING_KEY = "Rating";
	
	private static final int MAX_RATING = 10;
	private static final int MIN_RATING = -10;
	
	/**
	 * Checks if the rating is within the allowed range -10 to 10
	 * @param rating	The rating to test
	 * @return			True if rating is valid<br>False otherwise
	 */
	public boolean isValid(@Nullable Integer rating) {
		if(rating == null) {
			logNull(LOGGER, RATING_KEY);
			return false;
		}
		
		else if(rating <= MAX_RATING && rating >= MIN_RATING)
			return true;
		
		LOGGER.log(Level.INFO, "Rating {0} is out of range from {1} to {2} hence invalid",
				new Object[] {rating, MIN_RATING, MAX_RATING});
		
		return false;
	}
	
	/**
	 * Checks if the rating is not NULL and the rating as well as the mapped ID is valid
	 * @param rating	The rating to test
	 * @return			True if rating is valid<br>False otherwise
	 */
	public boolean isValid(@Nullable Entry<Long, Integer> rating) {
		//Check NULL
		if(rating == null) {
			logNull(LOGGER, RATING_KEY);
			return false;
		}
		
		//Check rating
		else if(!isValid(rating.getValue()) || rating.getKey() == null || rating.getKey() < 0l)
			return false;
		
		//Check ID 
		return getValidator(IdValidator.class).isValid(rating.getKey());
	}
	
	/**
	 * Checks if ratings is not NULL and each rating is valid as well as the mapped ID
	 * @param ratings	The ratings to test
	 * @return			True if ratings are valid<br>False otherwise
	 */
	public boolean isValid(@Nullable Map<Long, Integer> ratings) {
		//Check NULL
		if(ratings == null) {
			LOGGER.log(Level.INFO, "Ratings must not be NULL");
			return false;
		}
		
		//Check each rating
		for(Entry<Long, Integer> rating : ratings.entrySet()) {
			if(!isValid(rating))
				return false;
		}
		
		return true;
	}

	/**
	 * @param nutritionObject	{@link Integer} rating or {@link Map} of {@link Long} and 
	 * 							{@link Integer} or simple {@link Entry} of such a {@link Map} to test
	 * @return 					<tt>True</tt> if ratingObject is valid<br><tt>False</tt> otherwise
	 */
	@Override
	public <T> boolean isValid(T ratingObject) {
		
		if(ratingObject == null) {
			logNull(LOGGER, RATING_KEY);
			return false;
		}
		
		if(isCorrectObject(ratingObject, HashMap.class, LOGGER)) {
			Map<?, ?> map = (Map<?, ?>) ratingObject;
			
			if(map.isEmpty()) return true;
			
			Entry<?, ?> entry = map.entrySet().iterator().next();
			
			if(isCorrectObject(entry.getKey(), Long.class, LOGGER)
					&& isCorrectObject(entry.getValue(), Integer.class, LOGGER)) {
				Map<Long, Integer> ratingMap = new HashMap<>();
				
				map.entrySet().forEach(c -> ratingMap.put((Long) c.getKey(),
						(Integer) c.getValue()));
				
				return isValid(ratingMap);
			}
		}
		
		if(isCorrectObject(ratingObject, Entry.class, LOGGER)) {
			Entry<?, ?> entry = (Entry<?, ?>) ratingObject;
			
			if(isCorrectObject(entry.getKey(), Long.class, LOGGER)
					&& isCorrectObject(entry.getValue(), Integer.class, LOGGER)) {
				
				Entry<Long, Integer> e = new AbstractMap.SimpleEntry<>(
						(Long) entry.getKey(), (Integer) entry.getValue());
				
				return isValid(e);
			}
		}
		
		if(isCorrectObject(ratingObject, Integer.class, LOGGER)) 
			return isValid((Integer) ratingObject);
		
		return false;
	}
}