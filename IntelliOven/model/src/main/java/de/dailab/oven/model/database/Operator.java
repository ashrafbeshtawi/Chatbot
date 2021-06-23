package de.dailab.oven.model.database;

public enum Operator {
	AS,
	CALL,
	CONTAINS,
	DELETE,
	DETACH,
	ID,
	IN,
	MATCH,
	MERGE,
	WHERE,
	WITH,
	RETURN,
	SET;
	
	/**
	 * @return Returns the node label in lower case
	 */
	@Override
	public String toString() {
		return name().toUpperCase();
	}
	
	/**
	 * @return Returns the node label in neo4j convention (Recipe)
	 */
	public String toDatabaseOperator() {
		return toString().toUpperCase().replace("_", " ");
	}
}
