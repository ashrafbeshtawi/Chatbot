package de.dailab.oven.model.database;

import java.lang.reflect.Method;
import java.util.Objects;

import javax.annotation.Nullable;

/**
 * Class for storing attributes with their getter methods and the identifier if they unchanged from
 * database
 * @author Tristan Schroer
 * @version 2.0.0
 */
public class AttributeTriple {

	@Nullable
	private String attributeKey;
	@Nullable
	private Boolean unchanged;
	@Nullable
	private Method method;
	
	/**
	 * Initialize the attribute triple with the given parameters<br><strong>Note:</strong> that key
	 * and method can <strong>not</strong> be changed later on
	 * @param attributeKey	The attributes key
	 * @param unchanged		The state if it is unchanged
	 * @param method		The getter method for the attribute
	 */
	public AttributeTriple(@Nullable String attributeKey, @Nullable Boolean unchanged, @Nullable Method method) {
		this.attributeKey = attributeKey;
		this.unchanged = unchanged;
		this.method = method;
	}
	
	/**
	 * @return The attributes key
	 */
	@Nullable
	public String getKey() {return this.attributeKey;}
	
	/**
	 * @return The state of the attribute being unchanged
	 */
	@Nullable
	public Boolean getUnchanged() {return this.unchanged;}
	
	/**
	 * Set the state of the attribute being unchanged
	 * @param unchanged True if attribute is unchanged<br>False if it has been changed
	 */
	public void setUnchanged(@Nullable Boolean unchanged) {this.unchanged = unchanged;}
	
	/**
	 * @return The attributes getter method
	 */
	@Nullable
	public Method getAttributeGetter() {return this.method;}
	
	@Override
	@Nullable
	public String toString(){
		return this.attributeKey 
				+ " - Unchanged : " + this.unchanged 
				+ " Getter method: " + this.method.getName();
	}

	@Override
	public boolean equals(Object object) {
		
		if(object == null) return false;
		
		if(this.getClass() != object.getClass()) return false;
		
		final AttributeTriple otherAttribute = (AttributeTriple) object;
		
		String otherKey = otherAttribute.getKey();
		
		if(!((this.attributeKey == null) ? (otherKey != null) 
				: !this.attributeKey.contentEquals(otherKey))) return false;
		
		Method otherMethod = otherAttribute.getAttributeGetter();
		
		return (this.method == null) ? (otherMethod != null) : !this.method.equals(otherMethod);
	}
	
	@Override
	public int hashCode() {return Objects.hash(this.attributeKey, this.method);}
}

