package de.dailab.oven.database.query;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Statement;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Values;
import org.neo4j.driver.v1.types.Node;

import de.dailab.oven.database.configuration.Graph;
import de.dailab.oven.database.parse.NutritionParser;
import de.dailab.oven.database.validate.NutritionValidator;
import de.dailab.oven.model.data_model.Amount;
import de.dailab.oven.model.data_model.Nutrition;
import de.dailab.oven.model.database.NodeLabel;

/**
 * Query class for querying nutrition within the database
 * @author Tristan Schroer
 * @version 2.0.0
 */
public class NutritionQuery extends AQuery{
	@Nonnull
	private static final Logger LOGGER = Logger.getLogger(NutritionQuery.class.getName());
	@Nonnull
	private static final NutritionParser PARSER = new NutritionParser();
	@Nonnull
	private static final NutritionValidator VALIDATOR = new NutritionValidator();
	@Nonnull
	private static final NodeLabel INGREDIENT_LABEL = NodeLabel.INGREDIENT;
	@Nonnull
	private static final Set<NodeLabel> VALID_LABELS 
			= EnumSet.of(NodeLabel.INGREDIENT, NodeLabel.RECIPE);	

	@Nonnull
	private final IdQuery idQuery;
	
	/**
	 * Initialize empty to set Graph later on
	 */
	public NutritionQuery() {this(null);}
	
	/**
	 * Initialize IngredientQuery with the graph to query
	 * @param graph	The graph to query
	 */
	public NutritionQuery(@Nullable Graph graph) {
		super(graph);
		this.idQuery = (IdQuery) getQuery(IdQuery.class);
	}
	
	/**
	 * Gets the nutrition for the ingredient
	 * @param ingredientID	The specified ID for the ingredient
	 * @return	NULL in case of lost database connection<br>
	 * 			Empty map in case of any failure or nutrition do not exist<br>
	 * 			Retrieved Map of nutrition otherwise
	 */
	@Nullable
	public Map<Nutrition, Amount> getNutrition(@Nullable Long nodeID) {
		if(Boolean.TRUE.equals(this.idQuery.isOneNodeLabelValid(nodeID, VALID_LABELS))) {
			try(Session readSession = this.graph.openReadSession()) {
				Statement statement = new Statement(
						"MATCH (n) WHERE ID(n) = $id "
						+ "RETURN n");
				
				StatementResult result = readSession.run(statement.withParameters(
						Values.parameters("id", nodeID)));
				
				Node node = result.single().get(0).asNode();
				
				return PARSER.parseNutritionFromNode(node);
			} catch (Exception e) {
				logLostConnection(LOGGER);
				return null;
			}
		}

		return new EnumMap<>(Nutrition.class);
	}
	
	/**
	 * Tries to set the given nutrition for a specific node
	 * @param nutrition	The nutrition to set
	 * @param nodeLabel	The nodes label where the nutrition shall be added to
	 * @param nodeID	The nodes ID
	 * @return			NULL in case of lost database connection<br>
	 * 					False if label is not a valid one for nutrition 
	 * 						or nutrition has not been set<br>
	 * 					True if any parameter is NULL or nutrition has been set
	 */
	@Nullable
	public Boolean setNutrition(@Nullable Map<Nutrition, Amount> nutrition, 
			@Nullable NodeLabel nodeLabel,	@Nullable Long nodeID) {
		
		if(nutrition == null || nodeID == null || nodeLabel == null)
			return true;
		
		if(!VALID_LABELS.contains(nodeLabel) || !VALIDATOR.isValid(nutrition))
			return false;
		
		if(Boolean.TRUE.equals(this.idQuery.isNodeIdValid(nodeID, nodeLabel))) {
			String variable = nodeLabel.toString();
			
			StringBuilder queryString = new StringBuilder();
			queryString.append("MATCH (" + variable + ":" + nodeLabel.toDatabaseLabel() +") " 
					+ "WHERE ID(" + variable + ") = $id ");
			
			queryString.append(getSetNutritionString(nutrition, variable));
			queryString.append("RETURN " + variable);
			
			try(Session writeSession = this.graph.openWriteSession()) {
				Statement statement = new Statement(queryString.toString());
				
				StatementResult result = writeSession.run(statement.withParameters(Values.parameters("id", nodeID)));
				
				Map<Nutrition, Amount> parsedNutrition = PARSER.parseNutritionFromNode(result.single().get(0).asNode());
				return parsedNutrition.equals(nutrition);
				
			} catch(Exception e) {
				logLostConnection(LOGGER);
				return null;
			}
		}
		
		return false;			
	}

	/**
	 * Creates a query string which can be used for creating an individual query
	 * @param nutrition The nutrition to set
	 * @param variable	The used variable within the query string which identifies the node
	 * @return For each nutrition key within the nutrition: "SET 'variable'.nutritionKey = amountString "
	 */
	@Nonnull
	public String getSetNutritionString(@Nullable Map<Nutrition, Amount> nutrition, @Nullable String variable) {
		
		if(nutrition != null 
				&& !nutrition.isEmpty() 
				&& variable != null
				&& !variable.isEmpty()) {
			
			StringBuilder nutritionString = new StringBuilder();
			
			String currentNutrition;
			String currentAmount;
			for(Entry<Nutrition, Amount> entry : nutrition.entrySet()) {
				if(entry.getKey() != null) {
					currentNutrition = entry.getKey().toVariable();
					currentAmount = entry.getValue().toString().toLowerCase();
					nutritionString.append("SET " + variable + "." + currentNutrition 
							+ " = '" + currentAmount + "' ");
				}
			}
			return nutritionString.toString();
		}
		
		return "";
	}
}
