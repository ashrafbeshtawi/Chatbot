package de.dailab.oven.model.data_model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.dailab.oven.model.serialization.LocalDateTimeDeserializer;

import java.time.LocalDateTime;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ShoppingListItem extends IngredientDateAmountItem {

	@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
	public ShoppingListItem(@JsonProperty("ingredient") @Nonnull final Ingredient ingredient, @JsonProperty("amount") final float amount, @JsonProperty("unit") @Nullable final Unit unit,
							@JsonProperty("requiredDate") @JsonDeserialize(using = LocalDateTimeDeserializer.class) @Nullable final LocalDateTime requiredDate) {
		super(ingredient, amount, unit, requiredDate);
	}

	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd")
	public LocalDateTime getRequiredDate() {
		return super.date;
	}
}
