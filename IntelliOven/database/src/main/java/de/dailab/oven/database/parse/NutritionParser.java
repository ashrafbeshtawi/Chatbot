package de.dailab.oven.database.parse;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.neo4j.driver.v1.types.Node;

import de.dailab.oven.model.data_model.Amount;
import de.dailab.oven.model.data_model.Nutrition;

/**
 * Class for parsing nutrition from database nodes
 * @author Tristan Schroer
 * @version 2.0.0
 */
public class NutritionParser extends ANeo4JParser{

	@Nonnull
	private static final ForkJoinPool POOL = ForkJoinPool.commonPool();	

	/**
	 * Tries to parse nutrition from the given node
	 * @param node		The node to parse
	 * @return			The parsed nutrition<br>NULL in case of parsing failure<br>
	 * 					Empty map in case node is NULL or has no nutrition
	 */
	@Nullable
	public Map<Nutrition, Amount> parseNutritionFromNode(Node node) {

		Map<Nutrition, Amount> nutritionMap = new EnumMap<>(Nutrition.class);
		
		if(node == null)
			return nutritionMap;

		final CountDownLatch latch = new CountDownLatch(1);
		
		try {
			nutritionMap.putAll(POOL.submit(new AsynchNutritionParser(node, latch)).invoke());			
		} catch(Exception e) {
			logParsingFailed(e);
			return null;
		}			
		
		waitForLatch(latch);
		
		return nutritionMap;
	}
	
	/**
	 * Class for parsing an nutrition from database node asynchronously 
	 * @author Tristan Schroer
	 * @version 2.0.0
	 */
	private class AsynchNutritionParser implements Callable<Map<Nutrition,Amount>> {
		
		@Nonnull
		private final CountDownLatch countDownLatch;
		@Nullable
		private final Node node;
		
		/**
		 * Initialize AsynchNutritionParser with the node to parse and a CountDownLatch 
		 * @param ingredientNode	The node to parse
		 * @param countDownLatch	CountDownLatch to count down
		 */
		public AsynchNutritionParser (@Nonnull Node node, 
				@Nonnull CountDownLatch countDownLatch) {
			
			this.countDownLatch = Objects.requireNonNull(countDownLatch, 
					"CountDownLatch must not be NULL");
			
			this.node = Objects.requireNonNull(node);
		}

		/**
		 * Tries to parse the nutrition from the node. Returns NULL in case of critical failure
		 */
		@Override
		@Nonnull
		public Map<Nutrition, Amount> call() throws Exception {
			
			Map<Nutrition, Amount> nutritionMap = new EnumMap<>(Nutrition.class);
			
			//Set nutrition
			String nutritionKey;
			String amountString;
			Amount amount;
			for(Nutrition nutrition : Nutrition.values()) {
				nutritionKey = nutrition.name().toLowerCase();
				if(this.node.containsKey(nutritionKey)) {
					try {
						amountString = this.node.get(nutritionKey).asString();
						amount = new Amount(Float.parseFloat(amountString.split(" ")[0]), 
								amountString.split(" ")[1]);
						
						nutritionMap.put(nutrition, amount);						
					} catch(Exception e) {
						log.log(Level.INFO, "Could not parse nutrition {0}", nutrition.name());
					}					
				}
			}
			
			countDownLatch.countDown();
			return nutritionMap;
		}
	}
}
