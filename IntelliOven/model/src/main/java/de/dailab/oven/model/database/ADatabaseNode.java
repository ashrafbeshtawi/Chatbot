package de.dailab.oven.model.database;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class ADatabaseNode extends AIdObject implements IDatabaseObject {
	
	@Nonnull
	private static final Logger LOGGER = Logger.getLogger(ADatabaseNode.class.getName());
	@Nonnull
	private static final DatabaseObjectType NODE = DatabaseObjectType.NODE;
	@Nullable
	private String name;
	private boolean nameFromDatabase = false;
	@Nonnull
	private Set<AttributeTriple> attributeSet = new HashSet<>();
	@Nullable
	private final NodeLabel nodeLabel;
	@Nonnull
	private long numberOfRelationships = 0;
	
	/**
	 * Initialize the node with parameters<br><strong>Note:</strong> Node label is unchangeable
	 * @param name		The nodes ID
	 * @param id		The nodes name
	 * @param nodeLabel	The nodes label
	 */
	public ADatabaseNode(@Nullable String name, @Nullable Long id, @Nullable NodeLabel nodeLabel) {
		setName(name);
		setID(id);
		this.nodeLabel = nodeLabel;
		try {
			this.attributeSet.add(new AttributeTriple("name", this.nameFromDatabase, 
					this.getClass().getMethod("getName")));
			
			this.attributeSet.add(new AttributeTriple("id", super.isIdFromDatabase(), 
					super.getClass().getMethod("getID")));
			
		} catch (Exception e) {
			LOGGER.log(Level.INFO, e.getLocalizedMessage(), e.getCause());
		}
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasID() {return true;}
	
	/**
	 * {@inheritDoc}	
	 */
	@Nonnull
	public DatabaseObjectType getDatabaseObjectType() {return NODE;}
	
	/**
	 * The node label the class labeled within the database
	 * @return The classes node label
	 */
	@Nullable
	public NodeLabel getNodeLabel() {return this.nodeLabel;}
	
	/**
	 * Returns the node name (not equal to label)
	 * @return NULL in case name has not been set yet<br>Nodes name in lower case otherwise
	 */
	@Nullable
	public String getName() {return this.name;}
	
	/**
	 * Sets the name (in lower case) in case name is not NULL or empty
	 * @param name	The name to set
	 */
	public void setName(@Nullable String name) {
		if(name != null && !name.isEmpty()) {
			this.name = name.toLowerCase();
			this.nameFromDatabase = false;
		}
	}
	
	/** 
	 * @return True if name is from database<br>False otherwise
	 */
	public boolean isNameFromDatabase() {return this.nameFromDatabase;}
	
	/**
	 * @return True if no node attribute has been set<br>False otherwise
	 */
	public boolean isEmpty() {return this.name == null && getID() == null;}
	
	/**
	 * @return The set of attributes with identifier if attribute has changed
	 */
	public Set<AttributeTriple> getAttributeSet() {return this.attributeSet;}
	
	/**
	 * Adds the given {@link AttributeTriple} to the set of {@link AttributeTriple}
	 * @param attributeTriple	The {@link AttributeTriple} to add
	 * @return					<tt>True</tt> if triple has been added<br><tt>False</tt> otherwise
	 */
	public boolean addAttributeTriple(@Nullable AttributeTriple attributeTriple) {
		if(attributeTriple != null) return this.attributeSet.add(attributeTriple);
		
		return false;
	}
	
	/**
	 * Sets that all attributes with their data are unchanged from database
	 */
	public void setAllDataFromDatabase() {this.attributeSet.forEach(a -> a.setUnchanged(true));}
	
	/**
	 * Set the total number of relationships connected with this node
	 * @param numberOfRelationships	The total number of relationships
	 */
	public void setNumberOfRelationships(long numberOfRelationships) {
		this.numberOfRelationships = numberOfRelationships;
	}
	
	/**
	 * @return The total number of all connected relationships with this node
	 */
	public long getNumberOfRelationships() {
		return this.numberOfRelationships;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isObjectFromDatabase() {
		for(AttributeTriple a : this.attributeSet) {
			if(Boolean.FALSE.equals(a.getUnchanged())) return false;
		}
			
		return true;
	}
	
	@Override
	public String toString(){
		StringBuilder attributes = new StringBuilder();
		
		this.attributeSet.forEach(a-> attributes.append(a.toString() + ", "));
		
		return this.nodeLabel.toDatabaseLabel() + " |"
			+ " Name: " + this.name
			+ " " + super.toString()
			+ " Attributes: [" + attributes.toString() + "]";
	}

	@Override
	public boolean equals(Object object) {
			
		if(object == null) return false;
		
		if(this.getClass() != object.getClass()) return false;
		
		final ADatabaseNode otherNode = (ADatabaseNode) object;
		
		NodeLabel otherLabel = otherNode.getNodeLabel();
				
		if(!((this.nodeLabel == null) ? (otherLabel != null) 
				: this.nodeLabel.equals(otherLabel))) return false;

		String otherName = otherNode.getName();
		
		return (this.name == null) ? (otherName != null) : this.name.contentEquals(otherName);
	}
	
	@Override
	public int hashCode() {return Objects.hash(this.nodeLabel, this.name);}
}
