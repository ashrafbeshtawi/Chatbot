package de.dailab.oven.database.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Statement;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Values;

import de.dailab.oven.database.configuration.Graph;
import de.dailab.oven.model.database.NodeLabel;

/**
 * Class for checking an Id in database
 * @author Tristan Schroer
 * @version 2.0.0
 */
public class IdQuery extends AQuery{

	@Nonnull
	private static final NodeLabel UNDEF = NodeLabel.UNDEF;
	@Nonnull
	private static final Logger LOGGER = Logger.getLogger(IdQuery.class.getName());

	/**
	 * Initialize empty to set Graph later on
	 */
	public IdQuery() {this(null);}
	
	/**
	 * Initialize class with a valid graph instance
	 * @param graph The graph to query
	 */
	public IdQuery(@Nullable Graph graph) {
		super(graph);
	}
	
	/**
	 * Queries the graph with the given Id and returns the matching <strong>Node-Labels</strong>
	 * @param idToQuery The Id of interest
	 * @return The found node labels as list. <br> Empty list if Id didn't match anything or is 
	 * 			invalid<br> NULL in case of lost database connection
	 */
	@Nullable
	public List<String> getNodeLabelsForId(@Nullable Long idToQuery) {
		List<String> labels = new ArrayList<>();

		if(idToQuery == null || idToQuery < 0) {
			logInvalidId(idToQuery);
			return labels;
		}
		
		else if(this.graph == null)
			return labels;
		
		try(Session readSession = this.graph.openReadSession()) {
			
			Statement statement = new Statement(
					"MATCH (n) WHERE ID(n) = $idToQuery RETURN labels(n) AS labels");
			
			StatementResult result = readSession.run(statement.withParameters(Values.parameters(
							"idToQuery", idToQuery)));
			
			if(result.hasNext())
				result.list().forEach(r -> r.get("labels").asList()
						.forEach(l -> labels.add(l.toString())));				
			
			if(labels.isEmpty()) 
				LOGGER.log(Level.INFO, "ID {0} did not match any node", idToQuery);
			
			
		}
		catch (Exception e) {
			logLostConnection(LOGGER);
			return null;
		}
		
		return labels;
	}
	
	/**
	 * Queries the graph with the given Id and returns the matching <strong>Relationship
	 * 		-Label</strong> (Equals Relationship-Type)
	 * @param idToQuery The Id of interest
	 * @return The found relationship label. <br> Empty string if Id didn't match anything or is 
	 * 			invalid<br> NULL in case of lost database connection
	 */
	@Nullable
	public String getRelationshipTypeForId(@Nullable Long idToQuery) {
		String label = "";

		if(idToQuery == null || idToQuery < 0) {
			logInvalidId(idToQuery);
			return label;
		}
		
		else if(this.graph == null)
			return label;
		
		try (Session readSession = this.graph.openReadSession()){
			
			//Check if ID is present in database. Not testing would yield neo4j to return a 
			//relationship type even if the id does not exist
			Statement getAllIDsStatement = new Statement("MATCH (n)<-[r]-(m) "
					+ "RETURN ID(r) AS ids ORDER BY ID(r)");
			
			StatementResult tmpResult = readSession.run(getAllIDsStatement);
			
			Set<Long> setIds = new HashSet<>();
			
			tmpResult.list().forEach(record -> setIds.add(record.get(0).asLong()));
			
			boolean idIsInDatabase = setIds.contains(idToQuery);
			
			if(!idIsInDatabase) {
				LOGGER.log(Level.INFO, "Id {0} did not match any relationship", idToQuery);
				return label;
			}
			
			Statement statement = new Statement(
					"MATCH(n)<-[r]-(m) WHERE ID(r) = $idToQuery RETURN type(r) AS type");
			
			StatementResult result = readSession.run(statement.withParameters(Values.parameters(
					"idToQuery", idToQuery)));
			
			if(result.hasNext())
				label = result.single().get("type").asString();
			
		} catch (Exception e) {
			logLostConnection(LOGGER);
			return null;
		} 
		
		return label;
	}
	
	/**
	 * Queries the graph and tests if the expected node label is set for the given ID
	 * @param idToQuery		The ID of interest
	 * @param expectedLabel	The expected node label. NULL or empty string if ID should not
	 * 						in database
	 * @return				True, if label and ID match, false if not.<br> 
	 * 						NULL in case of lost connection
	 */
	@Nullable
	public Boolean isNodeIdValid(@Nullable Long idToQuery, @Nullable NodeLabel expectedLabel) {
		if(idToQuery == null || idToQuery < 0) {
			logInvalidId(idToQuery);
			return false;
		}
		
		List<String> foundLabelsForRecipeId = getNodeLabelsForId(idToQuery);

		if(foundLabelsForRecipeId == null)
			return null;
		
		else if(expectedLabel != null && !expectedLabel.equals(UNDEF)) {
			
			if(foundLabelsForRecipeId.isEmpty() 
					|| !foundLabelsForRecipeId.contains(expectedLabel.toDatabaseLabel())) {
				
				LOGGER.log(Level.INFO, "ID {0} does not match to label {1}" 
						, new Object[] {idToQuery, expectedLabel});
				
				return false;
			}			
			else
				return true;
		}
		
		else {
			
			if(foundLabelsForRecipeId.isEmpty())
				return true;
			
			else {
				LOGGER.log(Level.INFO, "ID {0} already exists in database", idToQuery);				
				return false;
			}
		}
	}

	
	/**
	 * Queries the graph and tests if the expected node label is set for the given ID
	 * @param idToQuery		The ID of interest
	 * @param validLabels	A set with all valid labels
	 * @return				True, if label and ID match, false if not.<br> 
	 * 						NULL in case of lost connection
	 */
	@Nullable
	public Boolean isOneNodeLabelValid(@Nullable Long idToQuery, @Nullable Set<NodeLabel> validLabels) {
		
		if(idToQuery == null || idToQuery < 0) {
			logInvalidId(idToQuery);
			return (validLabels == null || validLabels.isEmpty());
		}
		
		List<String> foundLabelsForRecipeId = getNodeLabelsForId(idToQuery);

		if(foundLabelsForRecipeId == null)
			return null;
		
		else if(foundLabelsForRecipeId.isEmpty()
				&& (validLabels == null 
						|| validLabels.isEmpty()
						|| validLabels.contains(null) 
						|| validLabels.contains(UNDEF))) 			
			return true;
				
		else if(validLabels == null) 
			return false;
	
		for(String nodeLabel : foundLabelsForRecipeId) {
			if(validLabels.contains(UNDEF.getNodeLabel(nodeLabel)))
				return true;
		}
		
		LOGGER.log(Level.INFO, "ID {0} does not match any label from {1}" 
				, new Object[] {idToQuery, validLabels});
		
		return false;
	}
	
	/**
	 * Logs that given ID is invalid
	 * @param idToQuery	The given ID
	 */
	private void logInvalidId(@Nullable Long idToQuery) {
		LOGGER.log(Level.INFO, "Id: {0} is invalid. Querying will be skipped", idToQuery);
	}
}
