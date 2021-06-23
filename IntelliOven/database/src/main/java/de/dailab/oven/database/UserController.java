package de.dailab.oven.database;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.dailab.oven.database.configuration.Graph;
import de.dailab.oven.database.exceptions.DatabaseException;
import de.dailab.oven.database.exceptions.InputException;
import de.dailab.oven.database.query.UserQuery;
import de.dailab.oven.model.data_model.Category;
import de.dailab.oven.model.data_model.ICategory;
import de.dailab.oven.model.data_model.Ingredient;
import de.dailab.oven.model.data_model.User;

public class UserController {
	
	private static final String USER_MUST_NOT_BE_NULL_KEY = "User must not be NULL.";
	@Nullable
	private Graph graph;
	@Nonnull
	private final UserQuery userQuery;
	
	/**
	 * Opens user controller for a specific graph
	 * @param graph The graph instance which should be used
	 * @throws DatabaseException In case connection could not be established
	 */
	public UserController(@Nullable final Graph graph) throws DatabaseException {
		this.graph = graph;
		if(graph != null) {
			this.userQuery = new UserQuery(this.graph);			
		}
		else {
			throw new DatabaseException("Graph has not been initialized");
		}
	}
	
	/**
	 * Adds user to database
	 * @param user 			User to add
	 * @throws Exception	In case database could not run required query
	 */
	public void addUser(@Nullable final User user) throws Exception {
		if(this.graph != null && user != null) {
			this.userQuery.putUser(Objects.requireNonNull(user, "User must not be NULL"));			
		}
	}

	/**
	 * Returns the user matching the ID<br>
	 * @param userID				Users ID if the user is already in database
	 * @return						The user matching the ID
	 * @throws InputException		In case input did not match requirements
	 * @throws InterruptedException	In case user parsing thread has been interrupted
	 */
	@Nullable
	public User getUserById(final long userID) throws InputException, InterruptedException {
		if(this.graph != null && userID != -1l) 
			return this.userQuery.getUser(userID);	
		
		return null;
	}
	
	/**
	 * Returns the user matching the parameters
	 * @param userName				Users name. Pass NULL or empty if it is unknown
	 * @return						The user matching the userName
	 * @throws InputException		In case input did not match requirements
	 * @throws InterruptedException	In case user parsing thread has been interrupted
	 */
	@Nullable
	public User getUserByUserName(@Nullable final String userName) throws InputException, InterruptedException {
		if(this.graph != null && userName != null && !userName.isEmpty())
		    return this.userQuery.getUser(userName);
		
		return null;
	}
	
	/**
	 * @return List of all users stored in the database 
	 * @throws InputException		In case input did not match requirements
	 * @throws InterruptedException	In case user parsing thread has been interrupted
	 */
	public List<User> getAllUsers() throws InputException, InterruptedException {
		if(this.graph != null) 
			return this.userQuery.getAllUsers();			
		
		return new ArrayList<>();
	}

	/**
	 * Add a category to the user preferences
	 * @param category		Category to add
	 * @param user			User to add
	 * @throws Exception	In case database could not run required query
	 */
	public void addPreferences(@Nullable final ICategory category, @Nullable final User user) throws Exception {
		if(this.graph != null && category != null && user != null) {
			Objects.requireNonNull(user, USER_MUST_NOT_BE_NULL_KEY).addPreferredCategory(new Category(category.getID()));
			this.userQuery.putUser(user);
		}
	}

	/**
	 * Add a category to the user preferences
	 * @param incompatibleIngredient		ingredient the user dislikes or is allergic to
	 * @param user			User to add
	 * @throws Exception	In case database could not run required query
	 */
	public void addIncompatibleIngredient(@Nullable final String incompatibleIngredient, @Nullable final User user) throws Exception {
		if(this.graph != null && incompatibleIngredient != null && user != null) {
			user.addIncompatibleIngredient(new Ingredient(incompatibleIngredient, user.getSpokenLanguages().iterator().next()));
			this.userQuery.putUser(user);
		}
	}
	
	/**
	 * Updates the whole user in database referring to the new set / added parameters within the given user class
	 * @param user 			The user which shall be updated
	 * @throws Exception	In case database could not run required query
	 */
	public void updateUser(@Nullable final User user) throws Exception {
		if(this.graph != null && user != null) 
			this.userQuery.putUser(Objects.requireNonNull(user, USER_MUST_NOT_BE_NULL_KEY));			
		
	}
	
	/**
	 * Delete given user from database
	 * @param user User to delete
	 */
	public void deleteUser(@Nullable final User user) {
		if(this.graph != null && user != null) {
			this.userQuery.deleteUser(Objects.requireNonNull(user, USER_MUST_NOT_BE_NULL_KEY));			
		}
	}

	@Nullable
	public Graph getGraph() {
		return this.graph;
	}
	
	public void setGraph(@Nullable final Graph graph) {
		this.graph = graph;
	}

	public User addAndGetUser(final User user) throws Exception {
		if(this.graph != null && user != null) {
			User newUser = this.userQuery.putUser(Objects.requireNonNull(user, "User must not be NULL"));
			if(newUser == null) {
				return this.userQuery.getUser(user.getName());
			}
			return newUser;			
		}
		else {
			return null;			
		}
	}
}
