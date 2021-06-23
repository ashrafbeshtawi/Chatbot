package de.dailab.oven.database.query;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jsoup.internal.StringUtil;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Statement;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Values;
import org.neo4j.driver.v1.types.Node;

import de.dailab.oven.database.configuration.Graph;
import de.dailab.oven.model.database.RelationshipType;
import de.dailab.oven.database.parse.CategoryParser;
import de.dailab.oven.database.validate.CategoryValidator;
import de.dailab.oven.model.data_model.Category;
import de.dailab.oven.model.database.NodeLabel;

public class CategoryQuery extends AQuery{

	@Nonnull
	private static final Logger LOGGER = Logger.getLogger(CategoryQuery.class.getName());
	@Nonnull
	private static final CategoryParser PARSER = new CategoryParser();
	@Nonnull
	private static final NodeLabel CATEGORY_LABEL = NodeLabel.CATEGORY;
	@Nonnull
	private static final String CATEGORY_VARIABLE = CATEGORY_LABEL.toString();
	@Nonnull
	private static final CategoryValidator VALIDATOR = new CategoryValidator();
	@Nonnull
	private final IdQuery idQuery;

	/**
	 * Initialize empty to set Graph later on
	 */
	public CategoryQuery() {this(null);}
	
	/**
	 * Initialize CategoryQuery with the graph to query
	 * @param graph	The graph to query
	 */
	public CategoryQuery(@Nullable Graph graph) {
		super(graph);
		this.idQuery = (IdQuery) getQuery(IdQuery.class);
	}
	
	/**
	 * Get all categories connected to the specified node
	 * @param nodeID	The nodes ID
	 * @param nodeLabel	The nodes label
	 * @return	Empty Set in case nodeID and node label do not match or CategoryParser 
	 * 				returns it<br>
	 * 			NULL in case of lost database connection<br>
	 * 			Set with parsed categories otherwise	
	 */
	@Nullable
	public Set<Category> getConnectedCategories(@Nullable Long nodeID
			, @Nullable NodeLabel nodeLabel) {
		
		return getConnectedCategories(nodeID, nodeLabel, RelationshipType.UNDEF);
	}
	
	
	/**
	 * Get all categories connected to the specified node
	 * @param nodeID			The nodes ID
	 * @param nodeLabel			The nodes label
	 * @param relationshipType 	The relationship type to look for 
	 * @return	Empty Set in case nodeID and node label do not match or CategoryParser 
	 * 				returns it<br>
	 * 			NULL in case of lost database connection<br>
	 * 			Set with parsed categories otherwise	
	 */
	@Nullable
	public Set<Category> getConnectedCategories(@Nullable Long nodeID
			, @Nullable NodeLabel nodeLabel, @Nullable RelationshipType relationshipType) {
		
		String relationshipVariable;
		
		//Set correct variable
		if(relationshipType == null || relationshipType == RelationshipType.UNDEF) 
			relationshipVariable = "r";
		
		else 
			relationshipVariable = ":" + relationshipType.toDatabaseLabel();
		
		Boolean isValidId = this.idQuery.isNodeIdValid(nodeID, nodeLabel);
		
		if(Boolean.TRUE.equals(isValidId)) {
			
			Statement statement = new Statement(
					"MATCH (n)-[" + relationshipVariable + "]-(category:Category) "
					+ "WHERE ID(n) = $nodeID "
					+ "WITH * RETURN category");
			
			try(Session readSession = this.graph.openReadSession()) {
				StatementResult result = readSession.run(statement.withParameters(Values.parameters(
						"nodeID", nodeID)));
				
				if(result.hasNext()) {
					List<Node> nodes = new ArrayList<>();
					result.list().forEach(r -> nodes.add(r.get(0).asNode()));
					return PARSER.parseCategoriesFromNodes(nodes);					
				}
				
			} catch(Exception e) {
				logLostConnection(LOGGER);
				return null;
			}
			
		}
		
		return new HashSet<>();
	}
	
	/**
	 * Gets the category with the specified ID
	 * @param categoryID	The ID of the category
	 * @return 	NULL in case of lost database connection<br>
	 * 			Empty category in case ID does not match an Category<br>
	 * 			Parsed category otherwise
	 */
	@Nullable 
	public Category getCategory(@Nullable Long categoryID) {
		Boolean isCategory = this.idQuery.isNodeIdValid(categoryID, CATEGORY_LABEL);
		
		if(Boolean.TRUE.equals(isCategory)) {
			try(Session readSession = this.graph.openReadSession()) {
				Statement statement = new Statement(
						"MATCH (category:Category) "
						+ "WHERE ID(category) = $id "
						+ "RETURN category");
			
				StatementResult result = readSession.run(statement.withParameters(
						Values.parameters("id", categoryID)));
				
				Record record = result.single();
				
				return PARSER.parseCategoryFromNode(record.get(0).asNode());
				
			} catch(Exception e) {
				logLostConnection(LOGGER);
				return null;
			}
		}
		
		return new Category();
	}
	
	/**
	 * Returns all with the category connected nodes with the given node label
	 * @param category		The category the nodes shall be connected with
	 * @param nodeLabel		The node label the nodes shall have
	 * @return				NULL in case of lost database connection<br>
	 * 						The list with the connected nodes otherwise
	 */
	@Nullable
	public List<Node> getConnectedNodes(@Nullable Category category, 
			@Nullable NodeLabel nodeLabel) {
		
		List<Node> nodes = new ArrayList<>();
		
		if(category == null || category.getID() == null
				|| nodeLabel == null  || nodeLabel == NodeLabel.UNDEF)
			return nodes;
			
		try(Session readSession = this.graph.openReadSession()) {
			Statement statement = new Statement(
					"MATCH (category:Category) WHERE ID(category) = $id "
					+ "WITH * MATCH (category)-[]-(n:"+ nodeLabel.toDatabaseLabel() + ") "
					+ "WITH * RETURN n");
			
			StatementResult result = readSession.run(statement.withParameters(
					Values.parameters("id", category.getID())));
			
			if(result.hasNext())
				result.list().forEach(record -> nodes.add(record.get(0).asNode()));
			
		} catch (Exception e) {
			logLostConnection(LOGGER);
			return null;
		}
		
		return nodes;
	}
	
	/**
	 * Returns all with the categories connected nodes with the given node label
	 * @param ingredient	The categories the nodes shall be connected with
	 * @param nodeLabel		The node label the nodes shall have
	 * @return				NULL in case of lost database connection<br>
	 * 						The list with the connected nodes otherwise
	 */
	@Nullable
	public List<Node> getConnectedNodes(@Nullable List<Category> categories, 
			@Nullable NodeLabel nodeLabel) {
		
		Set<Node> nodes = new HashSet<>();
		
		Set<Long> ids = new HashSet<>();
		
		if(categories == null || categories.isEmpty()) return new ArrayList<>();
		
		while(categories.contains(null)) {categories.remove(null);}
		
		getCategoryIds(categories, ids);
		
		ids.remove(null);
		
		String label;
		
		if(nodeLabel == null || nodeLabel == NodeLabel.UNDEF)
			label = "";
		else
			label = ":" + nodeLabel.toDatabaseLabel();
		
		try(Session readSession = this.graph.openReadSession()){
			
			StringBuilder queryString = new StringBuilder();
			queryString.append("MATCH (c:Category) WHERE ID(c) IN [");
			queryString.append(StringUtil.join(ids, ","));
			queryString.append("] ");
			queryString.append("WITH * MATCH (c)-[]-(n"+ label + ") ");
			queryString.append("WITH * RETURN n");
						
			Statement statement = new Statement(queryString.toString());
			
			StatementResult result = readSession.run(statement);
			
			if(result.hasNext())
				result.list().forEach(record -> nodes.add(record.get(0).asNode()));

		} catch (Exception e) {
			logLostConnection(LOGGER);
			return null;
		}
		
		if(nodes.contains(null))
			return null;	

			
		return nodes.stream().collect(Collectors.toList());
	}

	private void getCategoryIds(List<Category> categories, Set<Long> ids) {
		for(Category c : categories) {
			if(c.isIdFromDatabase())
				ids.add(c.getID());
			else {
				c = getCategory(c.getName());
				if(c != null)
					ids.add(c.getID());					
			}
		}
	}
	
	/**
	 * Tries to merge the category into the database
	 * @param category		The category to put
	 * @return				NULL in case of lost database connection or category is NULL<br>
	 * 						Unchanged category in case ingredient is already in database
	 * 							or category is invalid based on {@link CategoryValidator}<br>
	 * 						Category with (new) ID from database otherwise
	 */
	@Nullable
	public Category putCategory(@Nullable Category category) {
		
		if(category == null || !VALIDATOR.isValid(category) 
				|| category.isObjectFromDatabase())
			
			return category;
		
		try(Session writeSession = this.graph.openWriteSession()) {
			
			StringBuilder queryString = new StringBuilder();
			
			queryString.append("MERGE (category:Category {name : $name}) ");	
			queryString.append("RETURN ID(category)");
			
			Statement statement = new Statement(queryString.toString());
			
			StatementResult result = writeSession.run(statement.withParameters(Values.parameters(
					"name", category.getName()
					)));
			
			category.setID(result.single().get(0).asLong());
			category.setAllDataFromDatabase();
			
		} catch (Exception e) {
			logLostConnection(LOGGER);
			return null;
		}
		
		return category;
	}
	
	
	@Nullable
	public Category getCategory(@Nullable String name) {
		
		if(name == null || name.isEmpty()) return null;
		
		StringBuilder queryString = new StringBuilder();
		
		queryString.append("MATCH(c:Category) WHERE c.name = '");
		queryString.append(name);
		queryString.append("' RETURN c");
		
		try(Session readSession = this.graph.openReadSession()){
			
			Statement statement = new Statement(queryString.toString());
			
			StatementResult result = readSession.run(statement);
			
			if(result.hasNext())
				return PARSER.parseCategoryFromNode(result.list().get(0).get(0).asNode());
			
		} catch (Exception e) {
			logLostConnection(LOGGER);
			return null;
		}
		
		return null;
	}
	
	
	/**
	 * Retrieves all strongly connected node IDs. Meaning all passed back node IDs are connected
	 * with each passed category
	 * @param categories	The categories to query
	 * @param nodeLabel		The node label of interest ({@code null} or {@link NodeLabel}.UNDEF if not specified
	 * @return				The matching IDs<br>Empty set in case none exist<br>{@code Null} in case of lost database connection
	 */
	@Nonnull
	public Set<Long> getStrongConnectedIds(@Nullable List<Category> categories, 
			@Nullable NodeLabel nodeLabel){
		
		
		Set<Long> ids = new HashSet<>();		
		Set<Category> categoriesWithIds = new HashSet<>();
		List<Set<Long>> groupedIds = new ArrayList<>();
		
		
		if(categories == null) return ids;
		
		for(Category c : categories) {
			if(c.isIdFromDatabase())
				categoriesWithIds.add(c);
			else
				categoriesWithIds.add(getCategory(c.getName()));
		}
		
		categoriesWithIds.forEach(c -> groupedIds.add(getConnectedIds(c, nodeLabel)));
		
		groupedIds.remove(null);
		
		if(!groupedIds.isEmpty()) {
			for(Set<Long> set : groupedIds) {
				if(set.isEmpty()) continue;
				if(ids.isEmpty()) ids.addAll(set);
				else ids.retainAll(set);
			}
		}
				
		return ids;
	}
	
	@Nullable
	private Set<Long> getConnectedIds(@Nonnull Category category, @Nullable NodeLabel nodeLabel){
		
		Set<Long> ids = new HashSet<>();
		
		List<Node> nodes = getConnectedNodes(category, nodeLabel);
		
		if(nodes == null) return null;
		
		nodes.forEach(n -> ids.add(n.id()));
		
		return ids;
	}
	
	
	/**
	 * Tries to merge the categories into the database
	 * @param categories	The categories to put
	 * @return				NULL in case of lost database connection<br>
	 * 						Within List:<br>
	 * 						Unchanged category in case category is already in database
	 * 							or category is invalid based on {@link CategoryValidator}<br>
	 * 						Category with (new) ID from database otherwise
	 */
	@Nullable
	public List<Category> putCategories(@Nullable List<Category> categories) {
		
		Set<Category> putCategories = new HashSet<>();
		
		if(categories != null)
			categories.forEach(ingredient -> putCategories.add(putCategory(ingredient)));
		
		if(putCategories.contains(null))
			return null;
		
		return putCategories.stream().collect(Collectors.toList());
	}
	
	
	/**
	 * Adds a relationship between the category and the target node<br>
	 * Direction:  category->target node
	 * @param categoryID 	The categories ID
	 * @param nodeID		The target node ID
	 * @param relationship	The relationship type to set
	 * @param attributes	The attributes to set for the relationship
	 * @return 	NULL in case of lost database connection<br>
	 * 			True if adding relationship has been successful or any ID 
	 * 				or the relationship is NULL<br>
	 * 			False otherwise
	 */
	@Nullable
	public Boolean addRelationshipWithAttributes(@Nullable Long categoryID, 
			@Nullable Long nodeID, @Nullable RelationshipType relationship, 
			@Nullable Map<String, Object> attributes) {
		
		if(categoryID == null || nodeID == null || relationship == null)
			return true;
		
		if(attributes == null)
			attributes = new HashMap<>();
		
		Set<NodeLabel> nodeLabels = EnumSet.allOf(NodeLabel.class);
		nodeLabels.remove(NodeLabel.UNDEF);
		
		if(Boolean.TRUE.equals(this.idQuery.isNodeIdValid(categoryID, CATEGORY_LABEL))
				&& Boolean.TRUE.equals(this.idQuery.isOneNodeLabelValid(nodeID, nodeLabels))) {
			
			try(Session writeSession = this.graph.openWriteSession()) {
				
				StringBuilder queryString = new StringBuilder();
				
				queryString.append("MATCH (category:Category) WHERE ID(category) = $catID ");
				queryString.append("WITH * MATCH (n) WHERE ID(n) = $nodeID ");
				queryString.append("WITH * MERGE (category)-[r:" + relationship.toDatabaseLabel() + "]->(n) ");
				
				attributes.entrySet().forEach(e -> queryString.append(
						"SET r." + e.getKey() + " = $" + e.getKey() + " "
						));
			
				queryString.append("RETURN ID(r)");
				
				//Add IDs to attributes
				attributes.put("catID", categoryID);
				attributes.put("nodeID", nodeID);
				
				Statement statement = new Statement(queryString.toString());
				
				StatementResult result = writeSession.run(statement.withParameters(attributes));
				
				Long tmpID = null;
				
				if(result.hasNext())
					tmpID = result.single().get(0).asLong();
				
				return tmpID != null;
				
			} catch (Exception e) {
				logLostConnection(LOGGER);
				return null;
			}
		}
			
		return false;
	}
	
	/**
	 * Adds a relationship between the category and the target node<br>
	 * Direction:  category->target node
	 * @param categoryID 	The categories ID
	 * @param nodeID		The target node ID
	 * @param relationship	The relationship type to set
	 * @return 	NULL in case of lost database connection<br>
	 * 			True if adding relationship has been successful or any ID 
	 * 				or the relationship is NULL<br>
	 * 			False otherwise
	 */
	@Nullable
	public Boolean addRelationship(@Nullable Long categoryID, 
			@Nullable Long nodeID, @Nullable RelationshipType relationship) {
		
		return addRelationshipWithAttributes(categoryID, nodeID, relationship, null);
	}
	
	/**
	 * Adds a relationship between the categories and the target node<br>
	 * Direction:  category->target node
	 * @param categoryIDs 	The categories IDs
	 * @param nodeID		The target node ID
	 * @param relationship	The relationship type to set
	 * @return 	NULL in case of lost database connection<br>
	 * 			True if adding all relationships has been successful in comparison to addRealtionship wirh<br>
	 * 			False otherwise
	 */
	@Nullable
	public Boolean addRelationship(@Nullable List<Long> categoryIDs, @Nullable Long nodeID,
			@Nullable RelationshipType relationship) {
		
		if(categoryIDs == null)
			return true;
		
		Set<Boolean> booleans = new HashSet<>();
		
		categoryIDs.forEach(i -> booleans.add(addRelationship(i, nodeID, relationship)));
		
		if(booleans.contains(null))
			return null;
		
		return !booleans.contains(Boolean.FALSE);
	}
}
