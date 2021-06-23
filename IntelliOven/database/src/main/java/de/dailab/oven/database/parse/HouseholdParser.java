package de.dailab.oven.database.parse;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.neo4j.driver.v1.types.Node;

import de.dailab.oven.model.data_model.Household;
import de.dailab.oven.model.database.NodeLabel;

public class HouseholdParser extends ANeo4JParser{

	@Nonnull
	private static final String NAME_KEY = "name";
	@Nonnull
	private static final String HOUSEHOLD_LABEL = NodeLabel.HOUSEHOLD.toDatabaseLabel();
	
	@Nullable
	public Set<Household> parseHouseholdsFromNodes(@Nullable List<Node> householdNodes) {
		
		Set<Household> houseHolds = new HashSet<>();

		if(householdNodes == null)
			return houseHolds;

		final CountDownLatch latch = new CountDownLatch(householdNodes.size());
			
		for(Node householdNode : householdNodes) {
			try {
				houseHolds.add(POOL.submit(
						new AsynchHouseholdParser(householdNode, latch)).invoke());
				
			} catch(Exception e) {
				logParsingFailed(e);
				return null;
			}			
		}
		
		waitForLatch(latch);
		
		if(houseHolds.contains(null))
			houseHolds.remove(null);
		
		return houseHolds;
	}
	
	@Nullable
	public Household parseHouseholdFromNode(@Nullable Node householdNode) {
		
		final CountDownLatch latch = new CountDownLatch(1);
		
		Household household;
		
		if(householdNode == null) return null;
		
		try {
			household = POOL.submit(
					new AsynchHouseholdParser(householdNode, latch)).invoke();
			
		} catch(Exception e) {
			logParsingFailed(e);
			return null;
		}
		
		waitForLatch(latch);
		return household;
	}

	private class AsynchHouseholdParser implements Callable<Household> {
		
		@Nonnull
		private final CountDownLatch countDownLatch;
		@Nullable
		private final Node householdNode;
		

		public AsynchHouseholdParser (@Nullable Node householdNode,
				@Nonnull CountDownLatch countDownLatch) {
			
			this.countDownLatch = Objects.requireNonNull(countDownLatch, 
					"CountDownLatch must not be NULL");
			
			this.householdNode = householdNode;
		}

		/**
		 * Logs that a failure causes a NULL return
		 * @param message The message to add to the logging
		 */
		private void logNull(@Nonnull String message) {
			log.log(Level.INFO, "{0}. NULL is returned", Objects.requireNonNull(message));
		}

		@Override
		@Nullable
		public Household call() throws Exception {
			
			//Check if record is NULL
			if(this.householdNode == null || !this.householdNode.hasLabel(HOUSEHOLD_LABEL)) {
				this.countDownLatch.countDown();
				logNull("Node is not a household");
				return null;
			}
			
			Household household = new Household(this.householdNode.get(NAME_KEY).asString());
			
			//Set ID
			household.setID(this.householdNode.id());
			
			countDownLatch.countDown();
			return household;
		}
	}
}
