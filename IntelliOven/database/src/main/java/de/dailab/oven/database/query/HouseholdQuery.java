package de.dailab.oven.database.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Statement;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.types.Node;

import de.dailab.oven.database.configuration.Graph;
import de.dailab.oven.database.exceptions.InputException;
import de.dailab.oven.database.parse.HouseholdParser;
import de.dailab.oven.database.parse.IngredientDateAmountItemParser;
import de.dailab.oven.model.data_model.GroceryStockItem;
import de.dailab.oven.model.data_model.Household;
import de.dailab.oven.model.data_model.HouseholdLabel;
import de.dailab.oven.model.data_model.Ingredient;
import de.dailab.oven.model.data_model.IngredientDateAmountItem;
import de.dailab.oven.model.data_model.ShoppingListItem;
import de.dailab.oven.model.data_model.User;
import de.dailab.oven.model.database.RelationshipType;

public class HouseholdQuery extends AQuery{

	private static final String MATCH_HOUSEHOLD_ID = "MATCH(h:Household) WHERE ID(h) = ";

	private static final HouseholdParser PARSER = new HouseholdParser();
	
	private static final Logger LOGGER = Logger.getLogger(HouseholdQuery.class.getName());
	
	private static final IngredientDateAmountItemParser I_PARSER = new IngredientDateAmountItemParser();
	
	private final UserQuery userQuery;
	
	private final IngredientQuery ingredientQuery;
	/**
	 * Initialize empty to set Graph later on
	 */
	public HouseholdQuery() {this(null);}
	
	/**
	 * Initialize HouseHoldQuery with the graph to query
	 * @param graph	The graph to query
	 */
	public HouseholdQuery(@Nullable Graph graph) {
		super(graph);
		this.userQuery = (UserQuery) getQuery(UserQuery.class);
		this.ingredientQuery = (IngredientQuery) getQuery(IngredientQuery.class);
	}
	
	@Nullable
	public Set<Household> getHouseholds(){
		
		Statement statement = new Statement(
				"MATCH (n:Household) "
				+ " RETURN n");
		
		try(Session readSession = this.graph.openReadSession()) {
			StatementResult result = readSession.run(statement);
			
			if(result.hasNext()) {
				List<Node> nodes = new ArrayList<>();
				
				result.list().forEach(r -> nodes.add(r.get(0).asNode()));
				
				return PARSER.parseHouseholdsFromNodes(nodes);
			}
			
		} catch(Exception e) {
			logLostConnection(LOGGER);
			return null;
		}
		
		return new HashSet<>();
	}
	
	@Nullable
	public Household putHouseHold(@Nullable Household household) {
		
		if(household == null) return null;
		
		StringBuilder queryString = new StringBuilder();
		
		queryString.append("MERGE (h:Household {name:'");
		queryString.append(household.getName().toLowerCase());
		queryString.append("'}) RETURN ID(h)");
		
		Statement statement = new Statement(queryString.toString());
		
		try(Session writeSession = this.graph.openWriteSession()){
			
			StatementResult result = writeSession.run(statement);
			
			if(result.hasNext()) {
				household.setID(result.single().get(0).asLong());
				household.setName(household.getName().toLowerCase());
			}
			
		} catch (Exception e) {
			logLostConnection(LOGGER);
			return null;
		}
		
		return household;
	}
	
	@Nullable
	public User addUser(@Nullable User user, @Nullable Household household) throws InputException {
		
		if(user == null || household == null || household.getHouseholdLabel() == null) return null;
		
		HouseholdLabel newLabel = household.getHouseholdLabel();
		
		if(!newLabel.equals(user.getHousehold())) user.setHousehold(newLabel);
		
		return this.userQuery.putUser(user);
	}
	
	@Nullable
	public User removeUser(@Nullable User user, @Nullable Household household) throws InputException {
		return addUser(user, household);
	}
	
	@Nullable
	public Household getAllUsers(@Nullable Household household) {
		
		if(household == null || household.getID() == null) return null;
		
		StringBuilder queryString = new StringBuilder();
		
		queryString.append(MATCH_HOUSEHOLD_ID);
		queryString.append(household.getID());
		queryString.append(" WITH* MATCH(u:User)-[:BELONGS_TO]->(h) RETURN ID(u)");
		
		Statement statement = new Statement(queryString.toString());
		
		Set<Long> userIDs = new HashSet<>();
		
		try(Session readSession = this.graph.openWriteSession()){
			
			StatementResult result = readSession.run(statement);
			
			if(result.hasNext()) {
				result.list().forEach(r -> userIDs.add(r.get(0).asLong()));
			}
			
		} catch (Exception e) {
			logLostConnection(LOGGER);
			return null;
		}
		
		userIDs.remove(null);
		
		userIDs.forEach(id -> household.addUser(this.userQuery.getUser(id)));
		
		return household;
		
	}
	
	@Nullable
	public Household getShoppingList(@Nullable Household household) {
		
		List<IngredientDateAmountItem> list = getIngredientDateAmountItems(household, RelationshipType.TO_SHOP);
		List<ShoppingListItem> shoppingList = new ArrayList<>();

		
		if(list == null) return household;
		list.forEach(i -> {
			ShoppingListItem sI = new ShoppingListItem(i.getIngredient(), i.getQuantity(), i.getUnit(), i.getDate());
			sI.setID(i.getID());
			shoppingList.add(sI);
		});
		
		household.setShoppingList(shoppingList);

		return household;
	}
	
	@Nullable
	public ShoppingListItem addShoppingListItem(@Nullable Household household, @Nullable ShoppingListItem item) {
		return (ShoppingListItem) addIngredientDateAmountItem(household, item, RelationshipType.TO_SHOP);
	}
	
	@Nullable
	public Boolean removeShoppingListItem(@Nullable Household household, @Nullable ShoppingListItem item) {
		return removeIngredientDateAmountItem(household, item);
	}
	
	@Nullable 
	public Boolean updateShoppingListItem(@Nullable Household household, @Nullable ShoppingListItem item) {
		return updateIngredientDateAmountItem(household, item);
	}
	
	@Nullable
	public Household getGroceryStock(@Nullable Household household) {
		
		List<IngredientDateAmountItem> list = getIngredientDateAmountItems(household, RelationshipType.IN_STOCK);
		List<GroceryStockItem> stock = new ArrayList<>();
		
		if(list == null) return household;
		
		list.forEach(i -> {
			GroceryStockItem gI = new GroceryStockItem(i.getIngredient(), i.getQuantity(), i.getUnit(), i.getDate());
			gI.setID(i.getID());
			stock.add(gI);		
		});	
		
		household.setGroceryStock(stock);
		
		return household;
	}
	
	@Nullable
	private List<IngredientDateAmountItem> getIngredientDateAmountItems(@Nullable Household household, @Nonnull RelationshipType relationshipType) {
		
		if(household == null || household.getID() == null) return null;

		StringBuilder queryString = new StringBuilder();
		
		queryString.append(MATCH_HOUSEHOLD_ID);
		queryString.append(household.getID());
		queryString.append(" WITH* MATCH (language:Language)<-[:IS]-(ingredient:Ingredient)-[abstract:");
		queryString.append(relationshipType.toDatabaseLabel());
		queryString.append("]-(h) ");
		queryString.append("RETURN language, ingredient, collect(abstract) as abstractAmount");
		
		Statement statement = new Statement(queryString.toString());
		
		try(Session readSession = this.graph.openReadSession()){
			
			StatementResult result = readSession.run(statement);
			
			if(result.hasNext()) {
				return I_PARSER.parseItemsFromRecords(result.list());
			}
			
		} catch (Exception e) {
			logLostConnection(LOGGER);
			return null;
		}
		
		return new ArrayList<>();
	}
	
	
	@Nullable
	public GroceryStockItem addGroceryStockItem(@Nullable Household household, @Nullable GroceryStockItem item) {
		return (GroceryStockItem) addIngredientDateAmountItem(household, item, RelationshipType.IN_STOCK);
	}
	
	@Nullable
	public Boolean removeGroceryStockItem(@Nullable Household household, @Nullable GroceryStockItem item) {
		return removeIngredientDateAmountItem(household, item);
	}
	
	@Nullable 
	public Boolean updateGroceryStockItem(@Nullable Household household, @Nullable GroceryStockItem item) {
		return updateIngredientDateAmountItem(household, item);
	}
	
	@Nullable
	public IngredientDateAmountItem addIngredientDateAmountItem(@Nullable Household household, @Nullable IngredientDateAmountItem item, @Nullable RelationshipType relationshipType) {
		
		if(household == null || item == null) return null;
		
		Ingredient ingredient = item.getIngredient();
		
		if(item.getIngredient().getID() <= 0l)
			ingredient = this.ingredientQuery.putIngredient(ingredient);
		
		Map<String, Object> attributes = new HashMap<>();
		
		if(item.getDate() != null) attributes.put("date", item.getDate().toString());
		
		attributes.put("quantity", item.getQuantity());
		attributes.put("unit", item.getUnit().toString());
		
		Long id = this.ingredientQuery.addRelationshipWithAttributes(ingredient.getID(), household.getID(), relationshipType, attributes);
		
		item.setID(id);
		
		return item;
	}
	
	@Nullable
	public Boolean removeIngredientDateAmountItem(@Nullable Household household, @Nullable IngredientDateAmountItem item) {
		
		if(household == null || item == null || item.getID() == null) return false;

		StringBuilder queryString = new StringBuilder();
		queryString.append("MATCH ()-[n]-() WHERE ID(n) = ");
		queryString.append(item.getID());
		queryString.append(" DELETE n RETURN n");
		
		return runQueryWithSingleIdMatching(item.getID(), queryString.toString());
	}
	
	@Nullable 
	public Boolean updateIngredientDateAmountItem(@Nullable Household household, @Nullable IngredientDateAmountItem item) {
		if(household == null || item == null || item.getID() == null) return false;

		StringBuilder queryString = new StringBuilder();
		queryString.append("MATCH()-[n]-() WHERE ID(n) = ");
		queryString.append(item.getID());
		queryString.append(" SET n.date = '");
		if(item.getDate() != null)
			queryString.append(item.getDate().toString());
		queryString.append("' ");
		queryString.append(" SET n.quantity = ");
		queryString.append(item.getQuantity());
		queryString.append(" SET n.unit = '");
		queryString.append(item.getUnit().toString());
		queryString.append("' RETURN ID(n)");
		
		return runQueryWithSingleIdMatching(item.getID(), queryString.toString());
	}
	
	@Nullable
	private Boolean runQueryWithSingleIdMatching(long id, @Nonnull String queryString) {
		
		Statement statement = new Statement(queryString);
		
		try(Session writeSession = this.graph.openWriteSession()){
			
			StatementResult result = writeSession.run(statement);
			
			if(result.hasNext()) 
				return result.list().get(0).get(0).asLong() == id;				
					
			return false;
			
		} catch (Exception e) {
			logLostConnection(LOGGER);
			return null;
		}	
	}
}
