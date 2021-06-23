package de.dailab.oven.api.interfaces.household.api;


import de.dailab.oven.database.HouseholdController;
import de.dailab.oven.database.exceptions.ConfigurationException;
import de.dailab.oven.database.exceptions.DatabaseException;
import de.dailab.oven.database.query.Query;
import de.dailab.oven.model.data_model.GroceryStockItem;
import de.dailab.oven.model.data_model.ShoppingListItem;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("oven/household")
public class HouseholdHTTP {

    @Nonnull
    private final HouseholdController householdController;

    public HouseholdHTTP() throws DatabaseException, ConfigurationException {
        this.householdController = new HouseholdController(new Query().getGraph());
    }

    @GetMapping(value = "/grocerystock")
    public ResponseEntity getGroceryStock() {
        return ResponseEntity.status(HttpStatus.OK).body(this.householdController.getGroceryStock());
    }

    @PostMapping(value = "/grocerystock/add")
    public ResponseEntity addGroceryStockItem(@RequestBody final GroceryStockItem item) {
        this.householdController.addGroceryStockItem(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.householdController.getGroceryStock());
    }

    @PostMapping(value = "/grocerystock/addAll")
    public ResponseEntity addGroceryStockItems(@RequestBody final List<GroceryStockItem> items) {
        this.householdController.addGroceryStockItems(items);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.householdController.getGroceryStock());
    }

    @PostMapping(value = "/grocerystock/update")
    public ResponseEntity updateGroceryStockItem(@RequestBody final GroceryStockItem item) {
        this.householdController.updateGroceryStockItem(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.householdController.getGroceryStock());
    }

    @PostMapping(value = "/grocerystock/remove")
    public ResponseEntity removeGroceryStockItem(@RequestBody final GroceryStockItem item) {
        this.householdController.removeGroceryStockItem(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.householdController.getGroceryStock());
    }

    @GetMapping(value = "/shoppinglist")
    public ResponseEntity getShoppingList() {
        return ResponseEntity.status(HttpStatus.OK).body(this.householdController.getShoppingList());
    }

    @PostMapping(value = "/shoppinglist/add")
    public ResponseEntity addShoppingListItem(@RequestBody final ShoppingListItem item) {
        this.householdController.addShoppingListItem(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.householdController.getShoppingList());
    }

    @PostMapping(value = "/shoppinglist/addAll")
    public ResponseEntity addShoppingListItems(@RequestBody final List<ShoppingListItem> items) {
        this.householdController.addShoppingListItems(items);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.householdController.getShoppingList());
    }

    @PostMapping(value = "/shoppinglist/update")
    public ResponseEntity updateGroceryStockItem(@RequestBody final ShoppingListItem item) {
        this.householdController.updateShoppingListItem(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.householdController.getShoppingList());
    }

    @PostMapping(value = "/shoppinglist/remove")
    public ResponseEntity removeShoppingListItem(@RequestBody final ShoppingListItem item) {
        this.householdController.removeShoppingListItem(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.householdController.getShoppingList());
    }

}
