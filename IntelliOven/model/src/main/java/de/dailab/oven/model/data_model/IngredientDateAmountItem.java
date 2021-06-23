package de.dailab.oven.model.data_model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.LocalDateTime;

public class IngredientDateAmountItem extends IngredientWithAmount {

	@JsonIgnore
	protected LocalDateTime date;
	
	public IngredientDateAmountItem(@JsonProperty("ingredient") @Nonnull final Ingredient ingredient, @JsonProperty("amount") final float amount, @JsonProperty("unit") @Nullable final Unit unit, @Nullable LocalDateTime date) {
		super(ingredient, amount, unit);
		this.date = date;
	}
	
	@Nullable
	public LocalDateTime getDate() {
		return this.date;
	}
	
	public void setDate(@Nullable LocalDateTime date) {
		this.date = date;
	}

}
