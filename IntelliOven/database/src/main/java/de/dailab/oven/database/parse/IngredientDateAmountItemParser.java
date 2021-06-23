package de.dailab.oven.database.parse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.types.Relationship;

import de.dailab.oven.model.data_model.Amount;
import de.dailab.oven.model.data_model.Ingredient;
import de.dailab.oven.model.data_model.IngredientDateAmountItem;
import de.dailab.oven.model.data_model.Unit;

/**
 * Class for parsing ingredients from database nodes
 * @author Tristan Schroer
 * @version 2.0.0
 */
public class IngredientDateAmountItemParser extends ANeo4JParser{

	@Nonnull
	private static final String LANGUAGE_KEY = "language";
	@Nonnull
	private static final String INGREDIENT_KEY = "ingredient";
	@Nonnull
	private static final String ABSTRACT_AMOUNT_KEY = "abstractAmount";
	@Nonnull
	private static final Logger LOGGER = Logger.getLogger(IngredientDateAmountItemParser.class.getName());
	
	@Nullable
	public List<IngredientDateAmountItem> parseItemFromRecord(@Nullable Record record) {
		
		if(record == null || !record.containsKey(INGREDIENT_KEY) || !record.containsKey(LANGUAGE_KEY) 
				|| !record.containsKey(ABSTRACT_AMOUNT_KEY))
			return null;
		

		final CountDownLatch latch = new CountDownLatch(1);
		
		List<IngredientDateAmountItem> items = new ArrayList<>();
		
		try {
			items.addAll(POOL.submit(new AsynchIngredientDateAmountItemParser(record, latch)).invoke());
			
		} catch(Exception e) {
			logParsingFailed(e);
			return null;
		}			
	
		waitForLatch(latch);
		
		return items;
	}
	
	@Nullable
	public List<IngredientDateAmountItem> parseItemsFromRecords(
			@Nullable List<Record> records) {
		
		List<IngredientDateAmountItem> items = new ArrayList<>();

		if(records == null) return null;

		final CountDownLatch latch = new CountDownLatch(items.size());
			
		for(Record record : records) {
			try {
				items.addAll(POOL.submit(
						new AsynchIngredientDateAmountItemParser(record, latch)).invoke());
				
			} catch(Exception e) {
				logParsingFailed(e);
				return null;
			}			
		}
		
		waitForLatch(latch);
		
		if(items.contains(null)) items.remove(null);
		
		return items;
	}
	
	private class AsynchIngredientDateAmountItemParser implements Callable<List<IngredientDateAmountItem>> {
		
		@Nonnull
		private final CountDownLatch countDownLatch;
		@Nullable
		private final Record record;
		
		public AsynchIngredientDateAmountItemParser (@Nullable Record record, 
				@Nonnull CountDownLatch countDownLatch) {
			
			this.countDownLatch = Objects.requireNonNull(countDownLatch, 
					"CountDownLatch must not be NULL");
			
			this.record = record;
		}

		/**
		 * Logs that a failure causes a NULL return
		 * @param message The message to add to the logging
		 */
		private void logNull(@Nonnull String message) {
			LOGGER.log(Level.INFO, "{0}. NULL is returned", Objects.requireNonNull(message));
		}
		

		@Override
		@Nullable
		public List<IngredientDateAmountItem> call() throws Exception {
			
			//Check if record is NULL
			if(this.record == null) {
				this.countDownLatch.countDown();
				logNull("Record is null");
				return null;
			}
			
			
			Ingredient ingredient = null;
			
			try {
				ingredient = new IngredientParser().parseIngredientFromNode(this.record.get(INGREDIENT_KEY).asNode(), this.record.get(LANGUAGE_KEY).asNode());				
			} catch (Exception e) {
				LOGGER.log(Level.INFO, e.getLocalizedMessage(), e.getCause());
			}
			
			if(ingredient == null) {
				this.countDownLatch.countDown();
				logNull("Ingredient is not parsable");
				return null;
			}
			
			List<Object> abstractAmountList = null;
			
			try {
				abstractAmountList = record.get(ABSTRACT_AMOUNT_KEY).asList();
				
			} catch(Exception e) {
				LOGGER.log(Level.INFO, e.getLocalizedMessage(), e.getCause());
			}
			
			if(abstractAmountList == null) {
				this.countDownLatch.countDown();
				logNull("Relationships are not parsable");
				return null;
			}
			
			List<IngredientDateAmountItem> list = new ArrayList<>();
			
			
			Relationship rel;
			LocalDateTime date;
			IngredientDateAmountItem item;
			for(int i = 0; i < abstractAmountList.size(); i++) {
				try {
					rel = this.record.get(ABSTRACT_AMOUNT_KEY).get(i).asRelationship();
					
					if(rel.containsKey("date"))
						date = LocalDateTime.parse(rel.get("date").asString());						
					else
						date = null;
					final Unit unit = Unit.UNDEF.getUnitFromAbbreviation(rel.get("unit").asString());
					item = new IngredientDateAmountItem(ingredient, rel.get("quantity").asFloat(), unit, date);
					item.setID(rel.id());
					list.add(item);
				} catch (Exception e) {
					logNull("Relationship is not parsable");
					return null;
				}
			}
					
			countDownLatch.countDown();
			return list;
		}
	}
}