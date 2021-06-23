package de.dailab.oven.database;

import de.dailab.oven.database.configuration.Graph;
import de.dailab.oven.database.exceptions.DatabaseException;
import de.dailab.oven.database.query.IngredientQuery;
import de.dailab.oven.model.data_model.Amount;
import de.dailab.oven.model.data_model.GroceryStockItem;
import de.dailab.oven.model.data_model.Ingredient;
import de.dailab.oven.model.data_model.ShoppingListItem;
import zone.bot.vici.Language;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class IngredientController {

	@Nonnull
	private final IngredientQuery ingredientQuery;
	@Nonnull
	private final HouseholdController householdController;
	
	public IngredientController(@Nullable final Graph graph) throws DatabaseException {
		if(graph != null) {
			this.ingredientQuery = new IngredientQuery(graph);
		}
		else {
			throw new DatabaseException("Graph has not been initialized");
		}
		this.householdController = new HouseholdController(graph);
	}

	/**
	 * Lists the names of all stored ingredients for all or the specified language.
	 *
	 * @param language Language for the ingredient names or {@code null} to get the names in all languages
	 * @return list of ingredient names
	 */
	public Set<String> listIngredientNames(@Nullable final Language language) {
		return this.ingredientQuery.getIngredientNames(language);
	}

	@Nonnull
	public List<GroceryStockItem> addToGroceryStock(@Nullable Ingredient ingredient, 
			@Nullable Amount amount, @Nullable LocalDateTime freshnessDate) {
		
		GroceryStockItem item = null;

		if(ingredient != null) {
			if(amount == null) amount = new Amount();
			item = new GroceryStockItem(ingredient, amount.getQuantity(), amount.getUnit(), freshnessDate);
		}
		
		return this.householdController.addGroceryStockItem(item);
		
	}
	
	@Nonnull
	public List<ShoppingListItem> addToShoppingList(@Nullable Ingredient ingredient, 
			@Nullable Amount amount, @Nullable LocalDateTime requiredDate) {
		
		ShoppingListItem item = null;
		if(ingredient != null) {
			if(amount == null) amount = new Amount();
			item = new ShoppingListItem(ingredient, amount.getQuantity(), amount.getUnit(), requiredDate);
		}
		
		return this.householdController.addShoppingListItem(item);
	}
	
	public HouseholdController getHouseholdController() {
		return this.householdController;
	}
}
