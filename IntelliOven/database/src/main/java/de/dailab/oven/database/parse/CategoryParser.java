package de.dailab.oven.database.parse;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.neo4j.driver.v1.types.Node;

import de.dailab.oven.model.data_model.Category;
import de.dailab.oven.model.database.NodeLabel;

/**
 * Class for parsing categories from database nodes
 * @author Tristan Schroer
 * @version 2.0.0
 */
public class CategoryParser extends ANeo4JParser{

	@Nonnull
	private static final Logger LOGGER = Logger.getLogger(CategoryParser.class.getName());
	@Nonnull
	private static final String NAME_KEY = "name";
	@Nonnull
	private static final String CATEGORY_LABEL = NodeLabel.CATEGORY.toDatabaseLabel();
	
	/**
	 * Tries to parse categories from the given nodes
	 * @param categoryNodes	The nodes to parse
	 * @return				Empty set in case categoryNodes is empty or NULL<br>
	 * 						NULL in case of parsing failure (Thread failed)<br>
	 * 						Parsed categories otherwise
	 */
	@Nullable
	public Set<Category> parseCategoriesFromNodes(@Nullable List<Node> categoryNodes) {
		
		Set<Category> categories = new HashSet<>();

		if(categoryNodes == null) return categories;

		final CountDownLatch latch = new CountDownLatch(categoryNodes.size());
		
		categories = new HashSet<>();
			
		for(Node categoryNode : categoryNodes) {
			try {
				categories.add(POOL.submit(
						new AsynchCategoryParser(categoryNode, latch)).invoke());
				
			} catch(Exception e) {
				logParsingFailed(e);
				return null;
			}			
		}
		
		waitForLatch(latch);
		
		categories.remove(null);
		
		return categories;
	}
	
	/**
	 * Tries to parse category from the given node
	 * @param categoryNode	The node to parse
	 * @return				NULL in case of parsing failure<br>
	 * 						Empty category in case passed node is NULL<br>
	 * 						The parsed category otherwise
	 */
	@Nullable
	public Category parseCategoryFromNode(@Nullable Node categoryNode) {
		
		final CountDownLatch latch = new CountDownLatch(1);
		
		Category category = new Category();
		
		if(categoryNode == null) return category;
		
		try {
			category = POOL.submit(
					new AsynchCategoryParser(categoryNode, latch)).invoke();
			
		} catch(Exception e) {
			logParsingFailed(e);
			return null;
		}
		
		waitForLatch(latch);
		return category;
	}
	
	/**
	 * Class for parsing a category from database node asynchronously 
	 * @author Tristan Schroer
	 * @version 2.0.0
	 */
	private class AsynchCategoryParser implements Callable<Category> {
		
		@Nonnull
		private final CountDownLatch countDownLatch;
		@Nullable
		private final Node categoryNode;
		
		/**
		 * Initialize AsynchIngredientParser with the node to parse and a CountDownLatch 
		 * @param ingredientNode	The node to parse
		 * @param countDownLatch	CountDownLatch to count down
		 */
		public AsynchCategoryParser (@Nullable Node categoryNode, 
				@Nonnull CountDownLatch countDownLatch) {
			
			this.countDownLatch = Objects.requireNonNull(countDownLatch, 
					"CountDownLatch must not be NULL");
			
			this.categoryNode = categoryNode;
		}

		/**
		 * Logs that a failure causes a NULL return
		 * @param message The message to add to the logging
		 */
		private void logNull(@Nonnull String message) {
			LOGGER.log(Level.INFO, "{0}. NULL is returned", Objects.requireNonNull(message));
		}
		
		/**
		 * Tries to parse the ingredient from the node. Returns NULL in case of critical failure
		 */
		@Override
		@Nullable
		public Category call() throws Exception {
			
			//Check if record is NULL
			if(this.categoryNode == null || !this.categoryNode.hasLabel(CATEGORY_LABEL)) {
				this.countDownLatch.countDown();
				logNull("Node is not a category");
				return null;
			}
			
			Category category = new Category();
			
			//Set ID
			category.setID(this.categoryNode.id());
			
			//Set name
			category.setName(this.categoryNode.get(NAME_KEY).asString());					
			
			countDownLatch.countDown();
			return category;
		}
	}
}
