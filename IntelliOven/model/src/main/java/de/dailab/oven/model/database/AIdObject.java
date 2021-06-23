package de.dailab.oven.model.database;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.annotation.Nullable;

public abstract class AIdObject {

	@Nullable
	private Long id;
	@JsonIgnore
	private boolean idFromDatabase = false;
	
	/**
	 * @return True if ID is from database<br>False otherwise
	 */
	public boolean isIdFromDatabase() {return this.idFromDatabase;}
	
	/**
	 * Returns the categories ID
	 * @return NULL in case ID has not been set yet<br>The categories ID otherwise
	 */
	@Nullable
	public Long getID() {return this.id;}
	
	/**
	 * Sets the categories ID in case it is not NULL and not negative
	 * @param id The ID to set for the category
	 */
	public void setID(@Nullable Long id) {
		if(id != null && id >= 0l) {
			this.id = id;
			this.idFromDatabase = false;
		}
	}
	
	@Override
	public String toString() {
		return "ID: " + this.id;
	}
}

