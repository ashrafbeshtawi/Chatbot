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
import de.dailab.oven.database.model.IngredientMap;
import de.dailab.oven.model.database.RelationshipType;
import zone.bot.vici.Language;
import de.dailab.oven.database.parse.IngredientParser;
import de.dailab.oven.database.validate.IngredientValidator;
import de.dailab.oven.model.data_model.Amount;
import de.dailab.oven.model.data_model.Ingredient;
import de.dailab.oven.model.data_model.Nutrition;
import de.dailab.oven.model.database.NodeLabel;

public class IngredientQuery extends AQuery {
	
	@Nonnull
	private static final Logger LOGGER = Logger.getLogger(IngredientQuery.class.getName());
	@Nonnull
	private static final IngredientParser PARSER = new IngredientParser();
	@Nonnull
	private static final NodeLabel INGREDIENT_LABEL = NodeLabel.INGREDIENT;
	@Nonnull
	private static final String INGREDIENT_VARIABLE = INGREDIENT_LABEL.toString();
	@Nonnull
	private static final IngredientValidator VALIDATOR = new IngredientValidator();
	@Nonnull
	private final IdQuery idQuery;
	@Nonnull
	private final NutritionQuery nutritionQuery;
	
	/**
	 * Initialize empty to set Graph later on
	 */
	public IngredientQuery() {this(null);}
	
	/**
	 * Initialize IngredientQuery with the graph to query
	 * @param graph	The graph to query
	 */
	public IngredientQuery(@Nullable Graph graph) {
		super(graph);
		this.idQuery = (IdQuery) getQuery(IdQuery.class);
		this.nutritionQuery = (NutritionQuery) getQuery(NutritionQuery.class);
	}
	
	/**
	 * Get all ingredients connected to the specified node
	 * @param nodeID	The nodes ID
	 * @param nodeLabel	The nodes label
	 * @return	Empty Set in case nodeID and node label do not match or IngredientParser 
	 * 				returns it<br>
	 * 			NULL in case of lost database connection<br>
	 * 			Set with parsed ingredients otherwise	
	 */
	@Nullable
	public Set<Ingredient> getConnectedIngredients(@Nullable Long nodeID
			, @Nullable NodeLabel nodeLabel) {
		
		return getConnectedIngredients(nodeID, nodeLabel, RelationshipType.UNDEF);
	}
	
	
	/**
	 * Get all ingredients connected to the specified node
	 * @param nodeID			The nodes ID
	 * @param nodeLabel			The nodes label
	 * @param relationshipType 	The relationship type to look for 
	 * @return	Empty Set in case nodeID and node label do not match or IngredientParser 
	 * 				returns it<br>
	 * 			NULL in case of lost database connection<br>
	 * 			Set with parsed ingredients otherwise	
	 */
	@Nullable
	public Set<Ingredient> getConnectedIngredients(@Nullable Long nodeID
			, @Nullable NodeLabel nodeLabel, @Nullable RelationshipType relationshipType) {
		
		String relationshipVariable;
		
		//Set correct variable
		if(relationshipType == null || relationshipType == RelationshipType.UNDEF) 
			relationshipVariable = "r";
		
		else 
			relationshipVariable = ":" + relationshipType.toDatabaseLabel();
			
		Statement statement = new Statement(
				"MATCH (n)-[" + relationshipVariable + "]-(ingredient:Ingredient) "
				+ "WHERE ID(n) = $nodeID "
				+ "WITH * MATCH (ingredient)-[:IS]->(language:Language) "
				+ "WITH * RETURN ingredient, language");
		
		try(Session readSession = this.graph.openReadSession()) {
			StatementResult result = readSession.run(statement.withParameters(Values.parameters(
					"nodeID", nodeID)));
			
			if(result.hasNext()) {
				IngredientMap ingredientMap = new IngredientMap();
				
				ingredientMap.putAll(result.list());
				return PARSER.parseIngredientsFromNodes(ingredientMap);
			}
			
		} catch(Exception e) {
			logLostConnection(LOGGER);
			return null;
		}
		
		return new HashSet<>();
	}
	
	/**
	 * Gets the ingredient with the specified ID
	 * @param ingredientID	The ID of the ingredient
	 * @return 	NULL in case of lost database connection or ID does not match an Ingredient<br>
	 * 			Parsed ingredient otherwise
	 */
	@Nullable 
	public Ingredient getIngredient(@Nullable Long ingredientID) {
		Boolean isIngredient = this.idQuery.isNodeIdValid(ingredientID, INGREDIENT_LABEL);
		
		if(Boolean.TRUE.equals(isIngredient)) {
			try(Session readSession = this.graph.openReadSession()) {
				Statement statement = new Statement(
						"MATCH (ingredient:Ingredient) "
						+ "WHERE ID(ingredient) = $id "
						+ "WITH * MATCH(ingredient)-[:IS]->(language:Language) "
						+ "RETURN ingredient, language");
			
				StatementResult result = readSession.run(statement.withParameters(
						Values.parameters("id", ingredientID)));
				
				Record record = result.next();
				
				return PARSER.parseIngredientFromNode(
						record.get(0).asNode(), record.get(1).asNode());
				
			} catch(Exception e) {
				logLostConnection(LOGGER);
			}
		}
		
		return null;
	}
	
	/**
	 * Gets the nutrition for the ingredient
	 * @param ingredientID	The specified ID for the ingredient
	 * @return	NULL in case of lost database connection
	 * 			Empty map in case of any failure or nutrition do not exist<br>
	 * 			Retrieved Map of nutrition otherwise
	 */
	@Nullable
	public Map<Nutrition, Amount> getNutrition(@Nullable Long ingredientID) {
		return this.nutritionQuery.getNutrition(ingredientID);
	}
	
	/**
	 * Tries to set the given nutrition for the ingredient
	 * @param nutrition	The nutrition to set
	 * @param ingredientID	The ingredients ID
	 * @return				NULL in case of lost database connection<br>
	 * 						False if nutrition has not been set<br>
	 * 						True if any parameter is NULL or nutrition has been set
	 */
	@Nullable
	public Boolean setNutrition(@Nullable Map<Nutrition, Amount> nutrition, 
			@Nullable Long ingredientID) {
		
		return this.nutritionQuery.setNutrition(nutrition, INGREDIENT_LABEL, ingredientID);
	}
	
	/**
	 * Returns all with the ingredient connected nodes with the given node label
	 * @param ingredient	The ingredient the nodes shall be connected with
	 * @param nodeLabel		The node label the nodes shall have
	 * @return				NULL in case of lost database connection<br>
	 * 						The list with the connected nodes otherwise
	 */
	@Nullable
	public List<Node> getConnectedNodes(@Nullable Ingredient ingredient, 
			@Nullable NodeLabel nodeLabel) {
		
		List<Node> nodes = new ArrayList<>();
		
		if(ingredient == null || ingredient.getID() < 0
				|| nodeLabel == null  || nodeLabel == NodeLabel.UNDEF)
			return nodes;
			
		try(Session readSession = this.graph.openReadSession()) {
			Statement statement = new Statement(
					"MATCH (ingredient:Ingredient) WHERE ID(ingredient) = $id "
					+ "WITH * MATCH (ingredient)-[]-(n:"+ nodeLabel.toDatabaseLabel() + ") "
					+ "WITH * RETURN n");
			
			StatementResult result = readSession.run(statement.withParameters(
					Values.parameters("id", ingredient.getID())));
			
			if(result.hasNext())
				result.list().forEach(record -> nodes.add(record.get(0).asNode()));
			
		} catch (Exception e) {
			logLostConnection(LOGGER);
			return null;
		}
		
		return nodes;
	}
	
	/**
	 * Returns all with the ingredients connected nodes with the given node label.
	 * @param ingredient	The ingredients the nodes shall be connected with
	 * @param nodeLabel		The node label the nodes shall have
	 * @return				NULL in case of lost database connection<br>
	 * 						The list with the connected nodes otherwise
	 */
	@Nullable
	public List<Node> getConnectedNodes(@Nullable List<Ingredient> ingredients, 
			@Nullable NodeLabel nodeLabel) {
		
		Set<Node> nodes = new HashSet<>();
		
		Set<Long> ids = new HashSet<>();
		
		if(ingredients == null || ingredients.isEmpty()) return new ArrayList<>();
		
		while(ingredients.contains(null)) {ingredients.remove(null);}
		
		for(Ingredient i : ingredients) {
			getIngredient(i).forEach(nI -> ids.add(nI.getID()));
		}
		
		String label;
		
		if(nodeLabel == null || nodeLabel == NodeLabel.UNDEF)
			label = "";
		else
			label = ":" + nodeLabel.toDatabaseLabel();
		
		try(Session readSession = this.graph.openReadSession()){
			
			StringBuilder queryString = new StringBuilder();
			queryString.append("MATCH (ingredient:Ingredient) WHERE ID(ingredient) IN [");
			queryString.append(StringUtil.join(ids, ","));
			queryString.append("] ");
			queryString.append("WITH * MATCH (ingredient)-[]-(n"+ label + ") ");
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
	
	/**
	 * Retrieves all strongly connected node IDs. The passed list of ingredients is the base in
	 * such way, that each ingredient will be looked up by its name for other similar ingredients
	 * which are treated as groups so that every strong connected node has at least one match in 
	 * each group
	 * @param ingredients	The ingredients to query
	 * @param nodeLabel		The node label of interest ({@code null} or {@link NodeLabel}.UNDEF if not specified
	 * @return				The matching IDs<br>Empty set in case none exist<br>{@code Null} in case of lost database connection
	 */
	@Nonnull
	public Set<Long> getStrongConnectedIds(@Nullable List<Ingredient> ingredients, 
			@Nullable NodeLabel nodeLabel){
		
		Set<Long> ids = new HashSet<>();
		
		List<Set<Ingredient>> groupedIngredients = new ArrayList<>();
		List<Set<Long>> groupedIds = new ArrayList<>();
		
		if(ingredients == null || ingredients.isEmpty()) return new HashSet<>();
		
		for(Ingredient i : ingredients) {
			groupedIngredients.add(getIngredient(i));
		}
		
		groupedIngredients.remove(null);
		
		groupedIngredients.forEach(g -> groupedIds.add(getConnectedIds(g.stream().collect(Collectors.toList()), nodeLabel)));
		
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
	
	/**
	 * Retrieves all node IDs which are connected to any ingredient within the passed list. 
	 * @param ingredients	The ingredients to query
	 * @param nodeLabel		The node label of interest ({@code null} or {@link NodeLabel}.UNDEF if not specified
	 * @return				The matching IDs<br>Empty set in case none exist<br>{@code Null} in case of lost database connection
	 */
	@Nullable
	private Set<Long> getConnectedIds(@Nonnull List<Ingredient> ingredients, @Nullable NodeLabel nodeLabel){
			
		Set<Long> ids = new HashSet<>();
		
		Set<Long> ingIDs = new HashSet<>();
		
		ingredients.forEach(i -> ingIDs.add(i.getID()));
		
		if(ingredients.isEmpty()) return new HashSet<>();
		
		String label;
		
		if(nodeLabel == null || nodeLabel == NodeLabel.UNDEF)
			label = "";
		else
			label = ":" + nodeLabel.toDatabaseLabel();
		
		try(Session readSession = this.graph.openReadSession()){
			
			StringBuilder queryString = new StringBuilder();
			queryString.append("MATCH (ingredient:Ingredient) WHERE ID(ingredient) IN [");
			queryString.append(StringUtil.join(ingIDs, ","));
			queryString.append("] ");
			queryString.append("WITH *, collect(ingredient) AS ings MATCH (i)-[]-(n"+ label + ") WHERE i in ings ");
			queryString.append("WITH * RETURN ID(n)");
						
			Statement statement = new Statement(queryString.toString());
					
			StatementResult result = readSession.run(statement);
			
			if(result.hasNext())
				result.list().forEach(record -> ids.add(record.get(0).asLong()));

		} catch (Exception e) {
			logLostConnection(LOGGER);
			return null;
		}
		
		return ids;
	}	

	/**
	 * Tries to merge the ingredient into the database
	 * @param ingredient	The ingredient to put
	 * @return				NULL in case of lost database connection or ingredient is NULL<br>
	 * 						Unchanged ingredient in case ingredient is already in database
	 * 							or ingredient is invalid based on {@link IngredientValidator}<br>
	 * 						Ingredient with (new) ID from database otherwise
	 */
	@Nullable
	public Ingredient putIngredient(@Nullable Ingredient ingredient) {
		
		if(ingredient == null || !VALIDATOR.isValid(ingredient))
			
			return ingredient;
				
		try(Session writeSession = this.graph.openWriteSession()) {
			
			StringBuilder queryString = new StringBuilder();
			
			queryString.append("MERGE (ingredient:Ingredient {name : $name}) ");
			queryString.append("SET ingredient.checkedForNutrition = $checked ");
			queryString.append(this.nutritionQuery.getSetNutritionString(
					ingredient.getNutrition(), "ingredient"));
			
			queryString.append("WITH * MERGE (language:Language {name : $language}) ");
			queryString.append("WITH * MERGE (ingredient)-[:IS]-(language) ");
			queryString.append("RETURN ID(ingredient)");
			
			Statement statement = new Statement(queryString.toString());
			
			StatementResult result = writeSession.run(statement.withParameters(Values.parameters(
					"name", ingredient.getName(),
					"checked", ingredient.isCheckedForNutrition(),
					"language", ingredient.getLanguage().getLangCode2()
					)));
			
			ingredient.setID(result.next().get(0).asLong());
			
		} catch (Exception e) {
			logLostConnection(LOGGER);
			return null;
		}
		
		return ingredient;
	}
	
	/**
	 * Tries to merge the ingredients into the database
	 * @param ingredient	The ingredients to put
	 * @return				NULL in case of lost database connection or ingredient is NULL<br>
	 * 						Within List:<br>
	 * 						Unchanged ingredient in case ingredient is already in database
	 * 							or ingredient is invalid based on {@link IngredientValidator}<br>
	 * 						Ingredient with (new) ID from database otherwise
	 */
	@Nullable
	public List<Ingredient> putIngredients(@Nullable List<Ingredient> ingredients) {
		
		Set<Ingredient> putIngredients = new HashSet<>();
		
		if(ingredients != null)
			ingredients.forEach(ingredient -> putIngredients.add(putIngredient(ingredient)));
		
		if(putIngredients.contains(null))
			return null;
		
		return putIngredients.stream().collect(Collectors.toList());
	}
	
	

	@Nullable
	public Long addRelationshipWithAttributes(@Nullable Long ingredientID, 
			@Nullable Long nodeID, @Nullable RelationshipType relationship, 
			@Nullable Map<String, Object> attributes) {
		
		if(ingredientID == null || nodeID == null || relationship == null)
			return null;
		
		if(attributes == null)
			attributes = new HashMap<>();
		
		Set<NodeLabel> nodeLabels = EnumSet.allOf(NodeLabel.class);
		nodeLabels.remove(NodeLabel.UNDEF);
		
		if(Boolean.TRUE.equals(this.idQuery.isNodeIdValid(ingredientID, INGREDIENT_LABEL))
				&& Boolean.TRUE.equals(this.idQuery.isOneNodeLabelValid(nodeID, nodeLabels))) {
			
			try(Session writeSession = this.graph.openWriteSession()) {
				
				StringBuilder queryString = new StringBuilder();
				
				queryString.append("MATCH (ingredient:Ingredient) WHERE ID(ingredient) = $ingID ");
				queryString.append("WITH * MATCH (n) WHERE ID(n) = $nodeID ");
				queryString.append("WITH * MERGE (ingredient)-[r:" + relationship.toDatabaseLabel() + "]->(n) ");
				
				attributes.entrySet().forEach(e -> queryString.append(
						"SET r." + e.getKey() + " = $" + e.getKey() + " "
						));
			
				queryString.append("RETURN ID(r)");
				
				//Add IDs to attributes
				attributes.put("ingID", ingredientID);
				attributes.put("nodeID", nodeID);
				
				Statement statement = new Statement(queryString.toString());
				
				StatementResult result = writeSession.run(statement.withParameters(attributes));
				
				Long tmpID = null;
				
				if(result.hasNext())
					tmpID = result.next().get(0).asLong();
				
				return tmpID;
				
			} catch (Exception e) {
				logLostConnection(LOGGER);
				return null;
			}
		}
			
		return null;
	}
	
	@Nullable
	public Boolean removeRelationship(@Nullable Long nodeID1, @Nullable Long nodeID2, @Nullable RelationshipType relationship) {
		if(nodeID1 == null || nodeID2 == null || relationship == null) return false;
		
		StringBuilder queryString = new StringBuilder();
		queryString.append("MATCH (n) WHERE ID(n) = ");
		queryString.append(nodeID1);
		queryString.append("WITH * MATCH (m) WHERE ID(m) = ");
		queryString.append(nodeID2);
		queryString.append("WITH * MATCH (n)-[r:");
		queryString.append(relationship.toDatabaseLabel());
		queryString.append("]-(m) DELETE r RETURN ID(n)");
		
		Statement statement = new Statement(queryString.toString());
		
		try(Session writeSession = this.graph.openWriteSession()){
			StatementResult result = writeSession.run(statement);
			
			if(result.hasNext()) {
				return result.single().get(0).asLong() == nodeID1;
			}
			
		} catch (Exception e) {
			logLostConnection(LOGGER);
			return null;
		}
		
		
		return false;
	}
	
	@Nullable
	public Set<Ingredient> getIngredient(@Nullable Ingredient ingredient) {
	
		Set<Ingredient> ingredients = new HashSet<>();
		
		if(ingredient == null || ingredient.getName().isEmpty()) 
			return ingredients;
		

		String name = ingredient.getName();
		StringBuilder queryString = new StringBuilder();
		queryString.append("MATCH (i:Ingredient) WHERE ");
		if(ingredient.getLanguage() == Language.GERMAN) {
			queryString.append("i.name CONTAINS('");
			queryString.append(name);
			queryString.append("') ");
		}
		else {
			queryString.append("(i.name = '");
			queryString.append(name);
			queryString.append("' OR '");
			queryString.append(name);
			queryString.append("' IN SPLIT(i.name, ' '))");
		}
		queryString.append("WITH * MATCH(i)-[:IS]-(l:Language) ");
		queryString.append("RETURN i, l");
		
		try(Session readSession = this.graph.openReadSession()){
			
			Statement statement = new Statement(queryString.toString());
			
			StatementResult result = readSession.run(statement);
			
			IngredientMap map = new IngredientMap();
			
			if(result.hasNext())
				result.list().forEach(map::put);
			
			return PARSER.parseIngredientsFromNodes(map);
			
		} catch(Exception e) {
			logLostConnection(LOGGER);
			return null;
		}
		
	}
	
	
	/**
 	 * Returns all ingredient names stored in database
	 * @return 			The set of ingredient names in all languages.<br>
	 * 					Empty set in case they do not exist in specified language
	 * 					NULL in case of lost database connection
	 */
	@Nullable
	public Set<String> getIngredientNames(){return getIngredientNames(null);}
	
	/**
	 * Returns all ingredient names stored in database
	 * @param language	The language of the request. NULL or Language.UNDEF in case
	 * 					ever ingredient should be passed back
	 * @return			The set of ingredient names in specified language.<br>
	 * 					Empty set in case they do not exist in specified language
	 * 					NULL in case of lost database connection
	 */
	@Nullable
	public Set<String> getIngredientNames(@Nullable Language language){
		Set<String> ingredientNames = new HashSet<>();
		
		StringBuilder queryString = new StringBuilder();
		
		queryString.append("MATCH (ingredient:Ingredient) ");
		if(language != null && !language.equals(Language.UNDEF)) {
			queryString.append("WITH * MATCH(language:Language) WHERE language.name = '");
			queryString.append(language.getLangCode2());
			queryString.append("' ");
			queryString.append("WITH * MATCH (ingredient)-[]-(language) ");
		}
		queryString.append("RETURN ingredient.name");
		
		try(Session readSession = this.graph.openReadSession()){
			
			Statement statement = new Statement(queryString.toString());
			
			StatementResult result = readSession.run(statement);
			
			if(result.hasNext()) {
				List<Record> records = result.list();
				records.forEach(r -> ingredientNames.add(r.get(0).asString()));
			}
			
		} catch (Exception e) {
			logLostConnection(LOGGER);
			return null;
		}
		
		return ingredientNames;
	}
	
	/**
	 * Adds a relationship between the {@link Ingredient} and another node
	 * if format (ingredient)-[relationship]->(other node)
	 * @param ingredientID	The ingredients ID
	 * @param nodeID		The others node ID
	 * @param relationship	The relationship type to set
	 * @return				<tt>True</tt> if relationship has been set<br>
	 * 						<tt>False</tt> if not<br>
	 * 						<tt>Null</tt> in case of lost database connection
	 */
	@Nullable
	public Long addRelationship(@Nullable Long ingredientID, 
			@Nullable Long nodeID, @Nullable RelationshipType relationship) {
		
		return addRelationshipWithAttributes(ingredientID, nodeID, relationship, null);
	}
	
	@Nullable
	public List<Long> addRelationships(@Nullable List<Long> ingredientIDs, @Nullable Long nodeID,
			@Nullable RelationshipType relationship) {
		
		if(ingredientIDs == null)
			return new ArrayList<>();
		
		Set<Long> idSet = new HashSet<>();
		
		ingredientIDs.forEach(i -> idSet.add(addRelationship(i, nodeID, relationship)));
		
		if(idSet.contains(null))
			return null;
		
		return idSet.stream().collect(Collectors.toList());
	}
}
