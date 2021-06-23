package de.dailab.oven.model.database;

/**
 * Interface for all later on database objects
 * @author Tristan Schroer
 * @version 2.0.0
 */
public interface IDatabaseObject {
	
	/**
	 * Returns the objects store type within the database {@link DatabaseObjectType}
	 * @return The matching store type
	 */
	public DatabaseObjectType getDatabaseObjectType();

	/**
	 * Returns if the object has a matching ID in database
	 * @return True if object has an ID<br>False if not
	 */
	public boolean hasID();
	
	/**
	 * Returns if objects attributes are completely unchanged from database
	 * @return True if all attributes are unchanged from database<br>False otherwise
	 */
	public boolean isObjectFromDatabase();
		
}