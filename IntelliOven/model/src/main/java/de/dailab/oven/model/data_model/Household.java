package de.dailab.oven.model.data_model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.dailab.oven.model.database.ADatabaseNode;
import de.dailab.oven.model.database.AttributeTriple;
import de.dailab.oven.model.database.NodeLabel;

public class Household extends ADatabaseNode{

	@Nonnull
	private static final Logger LOGGER = Logger.getLogger(Household.class.getName());
	@Nonnull
	private static final NodeLabel HOUSE_HOLD_LABEL = NodeLabel.HOUSEHOLD;
	@Nonnull
	private static final Set<User> USERS = new HashSet<>();
	@Nonnull
	private static final List<GroceryStockItem> STOCK = new ArrayList<>();
	@Nonnull
	private static final List<ShoppingListItem> SHOPPING_LIST = new ArrayList<>();
	@Nullable
	private final HouseholdLabel householdLabel;
	
	private boolean usersFromDatabase = false;
	private boolean stockFromDatabase = false;
	private boolean shoppingListFromDatabase = false;

	public Household() {this(null);}
	
	public Household(@Nullable String name) {this(name, null);}
	
	
	public Household(@Nullable String name, @Nullable Long id) {
		super(name, id, HOUSE_HOLD_LABEL);
		try {
			addAttributeTriple(new AttributeTriple("users", this.usersFromDatabase, 
					this.getClass().getMethod("getUsers")));
			
			addAttributeTriple(new AttributeTriple("groceryStock", this.stockFromDatabase, 
					this.getClass().getMethod("getGroceryStock")));
			
			addAttributeTriple(new AttributeTriple("shoppingList", this.shoppingListFromDatabase, 
					this.getClass().getMethod("getShoppingList")));
			
		} catch (NoSuchMethodException | SecurityException e) {
			LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e.getCause());
		}
		
		this.householdLabel = HouseholdLabel.GUEST.getLabel(name);
	}
	
	public Set<User> getUsers() {return Collections.unmodifiableSet(USERS);}
	
	public boolean addUser(@Nullable User user) {
		if(user != null && !USERS.contains(user)) {
			this.usersFromDatabase = false;
			return USERS.add(user);
		}
		return false;
	}
	
	public boolean updateUser(@Nullable User user) {
		if(user == null || !removeUser(user)) return false;
		return addUser(user);
	}
	
	public void setUsers(@Nullable Set<User> users) {
		if(USERS.equals(users)) return;
		
		USERS.clear();
		if(users != null) USERS.addAll(users);
		
		this.usersFromDatabase = false;
	}
	
	public boolean removeUser(@Nullable User user) {
		if(USERS.contains(user)) {
			this.usersFromDatabase = false;
			return USERS.remove(user);
		}
		return false;
	}
	
	public List<GroceryStockItem> getGroceryStock() {return Collections.unmodifiableList(STOCK);}
	
	public boolean addGroceryStockItem(@Nullable GroceryStockItem item) {
		if(item != null && !STOCK.contains(item)) {
			this.stockFromDatabase = false;
			return STOCK.add(item);
		}
		return false;
	}
	
	public boolean updateGroceryStockItem(@Nullable GroceryStockItem item) {
		if(item == null || !removeGroceryStockItem(item)) return false;
		return addGroceryStockItem(item);
	}
	
	public void setGroceryStock(@Nullable List<GroceryStockItem> stock) {
		if(STOCK.equals(stock)) return;
		
		STOCK.clear();
		if(stock != null) STOCK.addAll(stock);
		
		this.stockFromDatabase = false;
	}
	
	public boolean removeGroceryStockItem(@Nullable GroceryStockItem item) {
		if(STOCK.contains(item)) {
			this.stockFromDatabase = false;
			return STOCK.remove(item);
		}
		return false;
	}
	
	public List<ShoppingListItem> getShoppingList() {return Collections.unmodifiableList(SHOPPING_LIST);}
	
	public boolean addShoppingListItem(@Nullable ShoppingListItem item) {
		if(item != null && !SHOPPING_LIST.contains(item)) {
			this.shoppingListFromDatabase = false;
			return SHOPPING_LIST.add(item);
		}
		return false;
	}
	
	public boolean updateShoppingListItem(@Nullable ShoppingListItem item) {
		if(item == null || !removeShoppingListItem(item)) return false;
		return addShoppingListItem(item);
	}
	
	public void setShoppingList(@Nullable List<ShoppingListItem> list) {
		if(SHOPPING_LIST.equals(list)) return;
		
		SHOPPING_LIST.clear();
		
		if(list != null) SHOPPING_LIST.addAll(list);
		
		this.shoppingListFromDatabase = false;
	}
	
	public boolean removeShoppingListItem(@Nullable ShoppingListItem item) {
		if(SHOPPING_LIST.contains(item)) {
			this.shoppingListFromDatabase = false;
			return SHOPPING_LIST.remove(item);
		}//
		return false;
	}
	
	public HouseholdLabel getHouseholdLabel() {
		return this.householdLabel;
	}
	
	@Override
	public boolean equals(Object object) {
		
		if(object == null) return false;
		
		if(this.getClass() != object.getClass()) return false;
		
		final Household otherHouseHold = (Household) object;
				
		String otherName = otherHouseHold.getName();
		
		return (this.getName() == null) ? (otherName != null) : this.getName().contentEquals(otherName);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.getName());
	}
}
