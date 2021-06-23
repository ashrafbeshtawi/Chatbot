package de.dailab.oven.database.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.types.Node;

public class IngredientMap {
	
	@Nonnull
	private static final Logger LOGGER = Logger.getLogger(IngredientMap.class.getName());
	@Nonnull
	private final Map<Node, Node> map = new HashMap<>();
	
	public Map<Node, Node> getIngredientMap() {
		return this.map;
	}
	
	public void put(@Nullable Node ingredientNode, @Nullable Node languageNode) {
		this.map.put(ingredientNode, languageNode);
	}
	
	public void putAll(Collection<Record> records) {
		if(records != null)
			records.forEach(this::put);
	}
	
	public void put(@Nullable Record record) {
		if(record != null) {
			try {
				put(record.get(0).asNode(), record.get(1).asNode());														
			} catch(Exception e) {
				LOGGER.log(Level.INFO, "Could not parse ingredient or language since of"
						+ "{0}", e.getLocalizedMessage());
			}				
		}
	}
}
