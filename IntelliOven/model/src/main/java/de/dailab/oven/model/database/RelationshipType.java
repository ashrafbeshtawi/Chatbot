package de.dailab.oven.model.database;

public enum RelationshipType {
	AUTHORED,
	BELONGS_TO,
	CONTAINS,
	COOKED,
	IN_STOCK,
	IS,
	IS_INCOMPATIBLE_WITH,
	LIKES,
	PREFERS,
	RATED,
	REPLACEABLE_BY,
	SPEAKS,
	TAKES,
	TO_SHOP,
	WATCHED,
	WRITTEN_IN,
	UNDEF;
	
	
	/**
	 * Relationships are stored in upper case in the database since of neo4j convention
	 * @return Returns an upper case string safely
	 */
	public String toDatabaseLabel() {
		return toString().toUpperCase();
	}
	
	/*
	 * Can be used to use the relationship type / label as a lower case variable for queries
	 * @return
	 */
	public String toVariable() {
		return toString().toLowerCase();
	}
}
