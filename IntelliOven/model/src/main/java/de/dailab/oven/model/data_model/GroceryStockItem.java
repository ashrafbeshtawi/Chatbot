package de.dailab.oven.model.data_model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.dailab.oven.model.serialization.LocalDateTimeDeserializer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.LocalDateTime;

public class GroceryStockItem extends IngredientDateAmountItem{

	@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
	public GroceryStockItem(@JsonProperty("ingredient") @Nonnull final Ingredient ingredient, @JsonProperty("amount") final float amount, @JsonProperty("unit") @Nullable final Unit unit,
							@JsonProperty("freshnessDate") @JsonDeserialize(using = LocalDateTimeDeserializer.class) @Nullable final LocalDateTime freshnessDate) {
		super(ingredient, amount, unit, freshnessDate);
	}

	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd")
	public LocalDateTime getFreshnessDate() {
		return super.date;
	}

}
