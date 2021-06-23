package de.dailab.oven.model.data_model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.dailab.oven.model.database.AIdObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class IngredientWithAmount extends AIdObject {

	@Nonnull
	private final Ingredient ingredient;
	private float quantity = 0.0f;
	@Nonnull
	private Unit unit = Unit.UNDEF;

	@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
	public IngredientWithAmount(@JsonProperty("ingredient") @Nonnull final Ingredient ingredient, @JsonProperty("quantity") final float quantity, @JsonProperty("unit") @Nullable final Unit unit) {
		this.ingredient = ingredient;
		
		if(Float.compare(quantity, this.quantity) > 0)
			this.quantity = quantity;
		
		if(unit != null)
			this.unit = unit;
	}

	@Nonnull
	public Ingredient getIngredient() {
		return this.ingredient;
	}

	public void setQuantity(final float quantity) {
		if(Float.compare(quantity, this.quantity) > 0)
			this.quantity = quantity;
	}
	
	/**
	 * Quantity may not be less than zero
	 * @return	The preset quantity
	 */
	public float getQuantity() {
		return this.quantity;
	}

	@Nonnull
	public Unit getUnit() {
		return this.unit;
	}
	
	public void setUnit(@Nullable final Unit unit) {
		if(unit != null)
			this.unit = unit;
	}
}
