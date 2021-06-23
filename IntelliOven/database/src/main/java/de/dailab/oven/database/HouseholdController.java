package de.dailab.oven.database;

import de.dailab.oven.database.configuration.Graph;
import de.dailab.oven.database.exceptions.DatabaseException;
import de.dailab.oven.database.exceptions.InputException;
import de.dailab.oven.database.query.HouseholdQuery;
import de.dailab.oven.database.query.UserQuery;
import de.dailab.oven.model.data_model.GroceryStockItem;
import de.dailab.oven.model.data_model.Household;
import de.dailab.oven.model.data_model.HouseholdLabel;
import de.dailab.oven.model.data_model.ShoppingListItem;
import de.dailab.oven.model.data_model.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for controlling households (GUEST and LOCAL)
 * @author 	Tristan Schroer
 * @version 1.0.0
 */
public class HouseholdController {

	@Nonnull
	private static final Logger LOGGER = Logger.getLogger(HouseholdController.class.getName());
	@Nonnull
	private final HouseholdQuery householdQuery;
	@Nonnull
	private final UserQuery userQuery;
	@Nullable
	private Graph graph;
	@Nonnull
	private static final Set<Household> HOUSEHOLDS = new HashSet<>();
	
	/**
	 * Initialize the controller with the graph to query
	 * @param graph					The graph to query
	 * @throws DatabaseException	In case graph is {@code null}
	 */
	public HouseholdController(@Nullable final Graph graph) throws DatabaseException {
		this.graph = graph;
		
		if(graph != null) {
			this.householdQuery = new HouseholdQuery(graph);
			this.userQuery = new UserQuery(graph);
		}
		else {
			throw new DatabaseException("Graph has not been initialized");
		}
	}
	
	/**
	 * @return 	The set of households (typically <i>HOUSEHOLD</i> and <i>GUEST</i>)<br>
	 * 			Empty set in case of no database connection
	 */
	@Nonnull
	public synchronized Set<Household> getHouseholds(){
		if(HOUSEHOLDS.isEmpty()) {
			
			Set<Household> tmp = this.householdQuery.getHouseholds();
			
			if(tmp == null) return new HashSet<>();
			
			tmp.forEach(h -> {if(h != null) {HOUSEHOLDS.add(h);}});
			
			if(tmp.size() < 2) {
				Household tmpH = addHouseHold(HouseholdLabel.GUEST);
				if(tmpH != null) HOUSEHOLDS.add(tmpH);
				tmpH = addHouseHold(HouseholdLabel.HOUSEHOLD);
				if(tmpH != null) HOUSEHOLDS.add(tmpH);
			}
		}
		
		return Collections.unmodifiableSet(HOUSEHOLDS);
	}
	
	/**
	 * @return The default household <i>(not GUEST)</i>
	 */
	public synchronized Household getHousehold() {
		return getHousehold(HouseholdLabel.HOUSEHOLD);
	}
	
	/**
	 * Returns the whole household with the specified label
	 * @param householdLabel	The {@link HouseholdLabel} of interest
	 * @return					The {@link Household} matching the specified label<br>
	 * 							{@code Null} in case {@link Household} with label of interest does
	 * 							not exist
	 */
	public synchronized Household getHousehold(@Nullable HouseholdLabel householdLabel) {
		
		if(householdLabel == null) return null;
		
		for(Household h : getHouseholds()) {
			if(h.getName().contentEquals(householdLabel.toString().toLowerCase())){
				return h;
			}
		}
			
		return null;
	}
	
	/**
	 * Adds the given user to the default {@link Household} and removes it from the 
	 * <i>GUEST-</i>{@link Household}
	 * @param user 	The {@link User} to add
	 * @return		The user with correct {@link HouseholdLabel}<br>
	 * 				{@code Null} in case passed {@link User} is {@code null}
	 */
	@Nullable
	public synchronized User addUser(@Nullable User user) {
		
		if(user == null) return null;
		
		user.setHousehold(HouseholdLabel.HOUSEHOLD);

		Household guest = getHousehold(HouseholdLabel.GUEST);
		Household household = getHousehold(HouseholdLabel.HOUSEHOLD);
		
		guest.removeUser(user);
		household.addUser(user);
		
		try {
			this.householdQuery.removeUser(user, guest);
		} catch (InputException e1) {
			LOGGER.log(Level.INFO, "User {0} could not be removed from the Household {1}",
					new Object[] {user.getName(), guest.getName()});
		}
		
		try {
			user = this.householdQuery.addUser(user, household);
		} catch (InputException e) {
			LOGGER.log(Level.INFO, "User {0} could not be added to the Household {1}",
					new Object[] {user.getName(), household.getName()});
		}
		
		return user;
	}
	
	/**
	 * Get a user from the database by name
	 * @param userName	The user name to query
	 * @return			The found {@link User}<br>
	 * 					{@code Null} in case of any failure
	 */
	@Nullable
	public User getUser(@Nullable String userName) {
		User user = this.userQuery.getUser(userName);
		if(user != null)	
			getHousehold(user.getHousehold()).addUser(user);
		
		return user;
	}
	
	/**
	 * Get a user from the database by ID
	 * @param userID	The user ID to query
	 * @return			The found {@link User}<br>
	 * 					{@code Null} in case of any failure
	 */
	public User getUser(@Nullable Long userID) {
		User user = this.userQuery.getUser(userID);
		if(user != null)	
			getHousehold(user.getHousehold()).addUser(user);
		
		return user;
	}
	
	/**
	 * Removes the {@link User} from the default-{@link Household} and adds it
	 * to the <i>GUEST-</i>{@link Household}
	 * @param user	The {@link User} to remove
	 * @return		The {@link User} with correct {@link HouseholdLabel}<br>
	 * 				{@code Null} in case passed {@link User} is {@code null}
	 */
	@Nullable
	public synchronized User removeUser(@Nullable User user) {
		
		if(user == null) return null;
		
		user.setHousehold(HouseholdLabel.GUEST);
		
		Household guest = getHousehold(HouseholdLabel.GUEST);
		Household household = getHousehold(HouseholdLabel.HOUSEHOLD);
		
		household.removeUser(user);
		guest.addUser(user);
		
		try {
			this.householdQuery.removeUser(user, household);
		} catch (InputException e1) {
			LOGGER.log(Level.INFO, "User {0} could not be removed from the Household {1}",
					new Object[] {user.getName(), guest.getName()});
		}
		try {
			user = this.householdQuery.addUser(user, guest);
		} catch (InputException e) {
			LOGGER.log(Level.INFO, "User {0} could not be added to the Household {1}",
					new Object[] {user.getName(), guest.getName()});
		}
		
		return user;
	}
	
	/**
	 * Returns a set of {@link User}s who belong to the given {@link HouseholdLabel}
	 * @param householdLabel	The {@link HouseholdLabel} to query
	 * @return					The set of all {@link User}s belonging to the {@link Household}<br>
	 * 							Empty set in case no {@link User} matches or database failure 
	 */
	@Nonnull
	public synchronized Set<User> getAllUsers(@Nullable HouseholdLabel householdLabel){
		return this.householdQuery.getAllUsers(getHousehold(householdLabel)).getUsers();
	}
	
	/**
	 * @return 	The list of {@link ShoppingListItem}s for the default {@link Household}<br>
	 * 			Empty set in case no items exists or database failure
	 */
	@Nonnull
	public synchronized List<ShoppingListItem> getShoppingList(){
		Household household = this.householdQuery.getShoppingList(getHousehold());
		return household.getShoppingList();
	}
	
	/**
	 * Add a {@link ShoppingListItem} to the shopping list for the default {@link Household}
	 * @param shoppingListItem	The item to add
	 * @return					The updated shopping list
	 */
	@Nonnull
	public synchronized List<ShoppingListItem> addShoppingListItem(@Nullable ShoppingListItem shoppingListItem) {
		List<ShoppingListItem> toAdd = new ArrayList<>();
		toAdd.add(shoppingListItem);
		return addShoppingListItems(toAdd);	
	}

	/**
	 * Add {@link ShoppingListItem}s to the shopping list for the default {@link Household}
	 * @param shoppingListItem	The items to add
	 * @return					The updated shopping list
	 */
	@Nonnull
	public synchronized List<ShoppingListItem> addShoppingListItems(@Nullable List<ShoppingListItem> shoppingListItems) {
		Household household = getHousehold();
		
		if(shoppingListItems != null)
			shoppingListItems.forEach(i -> {
				i = this.householdQuery.addShoppingListItem(household, i);
				household.addShoppingListItem(i);
			});		
		
		return household.getShoppingList();
	}
	
	/**
	 * Update a {@link ShoppingListItem} from the shopping list for the default {@link Household}
	 * @param shoppingListItem	The item to update
	 * @return					The updated shopping list
	 */
	@Nonnull
	public synchronized List<ShoppingListItem> updateShoppingListItem(@Nullable ShoppingListItem shoppingListItem) {
		Household household = getHousehold();
		household.updateShoppingListItem(shoppingListItem);
		this.householdQuery.updateShoppingListItem(household, shoppingListItem);
		return household.getShoppingList();
	}

	/**
	 * Remove a {@link ShoppingListItem} from the shopping list for the default {@link Household}
	 * @param shoppingListItem	The item to remove
	 * @return					The updated shopping list
	 */
	@Nonnull
	public synchronized List<ShoppingListItem> removeShoppingListItem(@Nullable ShoppingListItem shoppingListItem) {
		Household household = getHousehold();
		this.householdQuery.removeShoppingListItem(household, shoppingListItem);
		household.removeShoppingListItem(shoppingListItem);
		return household.getShoppingList();
	}
	
	/**
	 * @return 	The list of {@link GroceryStockItem}s for the default {@link Household}<br>
	 * 			Empty set in case no items exists or database failure
	 */
	@Nonnull
	public synchronized List<GroceryStockItem> getGroceryStock(){
		Household household = this.householdQuery.getGroceryStock(getHousehold());
		return household.getGroceryStock();
	}
	
	/**
	 * Add a {@link GroceryStockItem} to the grocery stock for the default {@link Household}
	 * @param shoppingListItem	The item to add
	 * @return					The updated grocery stock
	 */
	@Nonnull
	public synchronized List<GroceryStockItem>  addGroceryStockItem(@Nullable GroceryStockItem groceryStockItem) {
		List<GroceryStockItem> toAdd = new ArrayList<>();
		toAdd.add(groceryStockItem);
		return addGroceryStockItems(toAdd);
	}

	/**
	 * Add {@link GroceryStockItem}s to the grocery stock for the default {@link Household}
	 * @param shoppingListItem	The items to add
	 * @return					The updated grocery stock
	 */
	@Nonnull
	public synchronized List<GroceryStockItem> addGroceryStockItems(@Nullable List<GroceryStockItem> groceryStockItems) {
		Household household = getHousehold();
		
		if(groceryStockItems != null)
			groceryStockItems.forEach(i -> {
				i = this.householdQuery.addGroceryStockItem(household, i);	
				household.addGroceryStockItem(i);
			});		
		
		return household.getGroceryStock();
	}
	
	/**
	 * Update a {@link GroceryStockItem} from the grocery stock for the default {@link Household}
	 * @param shoppingListItem	The item to update
	 * @return					The updated grocery stock
	 */
	@Nonnull
	public synchronized List<GroceryStockItem> updateGroceryStockItem(@Nullable GroceryStockItem groceryStockItem) {
		Household household = getHousehold();
		household.updateGroceryStockItem(groceryStockItem);
		this.householdQuery.updateGroceryStockItem(getHousehold(), groceryStockItem);
		return household.getGroceryStock();
	}
	
	/**
	 * Remove a {@link GroceryStockItem} from the grocery stock for the default {@link Household}
	 * @param shoppingListItem	The item to remove
	 * @return					The updated grocery stock
	 */
	@Nonnull
	public synchronized List<GroceryStockItem> removeGroceryStockItem(@Nullable GroceryStockItem groceryStockItem) {
		Household household = getHousehold();
		household.removeGroceryStockItem(groceryStockItem);
		this.householdQuery.removeGroceryStockItem(getHousehold(), groceryStockItem);
		return household.getGroceryStock();
	}
	
	/**
	 * Adds a {@link Household} to the set of {@link Household}s
	 * @param householdLabel 	The {@link HouseholdLabel} which identifies the {@link Household}
	 * @return					The added {@link Household}<br>
	 * 							{@code Null} in case adding failed
	 */
	@Nullable
	private Household addHouseHold(@Nonnull HouseholdLabel householdLabel) {
		Household tmpH = new Household(householdLabel.toString());
		tmpH = this.householdQuery.putHouseHold(tmpH);
		return tmpH;
	}
	
}