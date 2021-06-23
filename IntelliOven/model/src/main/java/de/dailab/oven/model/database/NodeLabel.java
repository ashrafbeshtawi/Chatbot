package de.dailab.oven.model.database;

import javax.annotation.Nullable;

public enum NodeLabel {
	AUTHOR,
	CATEGORY,
	DURATION,
	FOODLABEL,
	HOUSEHOLD,
	INGREDIENT,
	LANGUAGE,
	RECIPE,
	USER,
	UNDEF;
	
	/**
	 * @return Returns the node label in lower case
	 */
	@Override
	public String toString() {
		return name().toLowerCase();
	}
	
	/**
	 * @return Returns the node label in neo4j convention (Recipe)
	 */
	public String toDatabaseLabel() {
		return toString().substring(0,1).toUpperCase().concat(toString().substring(1));
	}
	
	@Nullable
	public NodeLabel getNodeLabel(@Nullable String stringToParse) {
		if(stringToParse == null) {
			return null;
		}
		else {
			for(NodeLabel nodeLabel : NodeLabel.values()) {
				if(nodeLabel.toString().equalsIgnoreCase(stringToParse.replace(" ", "")
						.replace(":", ""))) {
					return nodeLabel;
				}
			}
		}
		
		return NodeLabel.UNDEF;
	}
}
