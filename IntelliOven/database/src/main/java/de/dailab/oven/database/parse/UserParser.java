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

import de.dailab.oven.model.data_model.User;
import de.dailab.oven.model.database.NodeLabel;

public class UserParser extends ANeo4JParser{

	@Nonnull
	private static final String NAME_KEY = "name";
	@Nonnull
	private static final String USER_LABEL = NodeLabel.USER.toDatabaseLabel();
	
	@Nullable
	public Set<User> parseUsersFromNodes(@Nullable List<Node> userNodes) {
		
		Set<User> users = new HashSet<>();

		if(userNodes == null)
			return users;

		final CountDownLatch latch = new CountDownLatch(userNodes.size());
			
		for(Node userNode : userNodes) {
			try {
				users.add(POOL.submit(
						new AsynchUserParser(userNode, latch)).invoke());
				
			} catch(Exception e) {
				logParsingFailed(e);
				return null;
			}			
		}
		
		waitForLatch(latch);
		
		if(users.contains(null))
			users.remove(null);
		
		return users;
	}
	
	@Nullable
	public User parseUserFromNode(@Nullable Node userNode) {
		
		final CountDownLatch latch = new CountDownLatch(1);
		
		User user;
		
		if(userNode == null) return null;
		
		try {
			user = POOL.submit(
					new AsynchUserParser(userNode, latch)).invoke();
			
		} catch(Exception e) {
			logParsingFailed(e);
			return null;
		}
		
		waitForLatch(latch);
		return user;
	}

	private class AsynchUserParser implements Callable<User> {
		
		@Nonnull
		private final CountDownLatch countDownLatch;
		@Nullable
		private final Node userNode;
		

		public AsynchUserParser (@Nullable Node userNode,
				@Nonnull CountDownLatch countDownLatch) {
			
			this.countDownLatch = Objects.requireNonNull(countDownLatch, 
					"CountDownLatch must not be NULL");
			
			this.userNode = userNode;
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
		public User call() throws Exception {
			
			//Check if record is NULL
			if(this.userNode == null || !this.userNode.hasLabel(USER_LABEL)) {
				this.countDownLatch.countDown();
				logNull("Node is not an user");
				return null;
			}
			
			User user = new User();
			user.setName(this.userNode.get(NAME_KEY).asString());
			
			//Set ID
			user.setId(this.userNode.id());
			
			countDownLatch.countDown();
			return user;
		}
	}
}
