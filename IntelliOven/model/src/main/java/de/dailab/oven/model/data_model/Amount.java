package de.dailab.oven.model.data_model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.dailab.oven.model.serialization.UnitToNameSerializer;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Amount {
	
	@Nonnull
    private Float quantity = 0.0f;
	@JsonSerialize(using = UnitToNameSerializer.class)
	@Nonnull
    private Unit unit = Unit.UNDEF;

	/**
	 * Initialize empty if setting quantity and unit will be done later on
	 */
    public Amount(){
    	super();
	}

    /**
     * @param quantity Integer will be converted to Long
     * @param unit Unit of the Ingredient
     */
    public Amount(int quantity, @Nullable Unit unit)
	{
    	setUnit(unit);
    	setQuantity((float) quantity);
	}

    /**
     * @param quantity quantity as float
     * @param unit Unit of the Ingredient
     */
	public Amount(float quantity, @Nullable Unit unit)
	{
		setUnit(unit);
		setQuantity(quantity);
	}
	
	/**
	 * @param quantity quantity as float
	 * @param unit as string will be converted to unit enum
	 */
	public Amount(float quantity, @Nullable String unit){	
		setUnit(unit);
		setQuantity(quantity);
	}
	
	/**
	 * @return quantity as float
	 */
	public float getQuantity() {
		return this.quantity;
	}

	/**
	 * @param quantity set quantity as float
	 */
	public void setQuantity(Float quantity){
		this.quantity = quantity;
	}

	/**
	 * @return Unit of mapped amount
	 */
	@Nonnull
	public Unit getUnit() {
		return this.unit;
	}
	
	/**
	 * @param unit pass unit of ingredient
	 */
	public void setUnit(@Nullable Unit unit){
		if(unit != null) {
			this.unit = unit;			
		}
	}
	
	/**
	 * @param unit pass unit of ingredient
	 */
	public void setUnit(@Nullable String unit) {
		Unit newUnit = Unit.UNDEF;
		if(unit != null) {
			this.unit = newUnit.getUnitFromAbbreviation(unit);
		}
		else {
			this.unit = newUnit;
		}
	}
	
	/**
	 * Overriding to concatenate quantity with unit
	 */
	@Override
	@Nonnull
	public String toString(){
		return Float.toString(this.getQuantity()) + " " + this.getUnit().toString();
	}

	@Override
	public boolean equals(Object object) {
		if(object == null)
			return false;
		
		if(this.getClass() != object.getClass())
			return false;
		
		final Amount otherAmount = (Amount) object;
		
		if(!this.unit.equals(otherAmount.getUnit()))
			return false;
		
		return this.quantity.equals(otherAmount.getQuantity());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.unit, this.quantity);
	}
}

