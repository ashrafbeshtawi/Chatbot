package de.dailab.oven.api.interfaces.user.api;

import de.dailab.oven.api.helper.serialization.ErrorHandler;
import de.dailab.oven.api_common.error.ResponseException;
import de.dailab.oven.api_common.user.UserObj;
import de.dailab.oven.controller.DatabaseController;
import de.dailab.oven.database.UserController;
import de.dailab.oven.database.exceptions.ConfigurationException;
import de.dailab.oven.database.exceptions.DatabaseException;
import de.dailab.oven.database.exceptions.InputException;
import de.dailab.oven.database.query.Query;
import de.dailab.oven.model.data_model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("oven/user")
public class UserHTTP {

    @Nonnull
    private final UserController userController;

    public UserHTTP() throws DatabaseException, ConfigurationException {
        this.userController = new UserController(new Query().getGraph());
    }


    @GetMapping(value = "")
    public ResponseEntity getAllUsers() {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(this.userController.getAllUsers());
        } catch (final InputException | InterruptedException e) {
            return ErrorHandler.get(500, e.getMessage());
        }
    }


    /*
     * U01
     * receive a User
     *
     * @return the UserList or Error
     * @body userRequest userID or Username as long or String, furthermore the rating as int and RecipeID as long
     */
    @GetMapping(value = "/get")
    public ResponseEntity getCurrentView(@RequestHeader(value = "userName", defaultValue = "") final String userName, @RequestHeader(value = "userID", defaultValue = "-1") final long userID) {
        try {
            if(userID >= 0) {
                return ResponseEntity.status(HttpStatus.OK).body(this.userController.getUserById(userID));
            }
            if(!userName.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK).body(this.userController.getUserByUserName(userName));
            }
            return getAllUsers();
        } catch (final InputException | InterruptedException e) {
            return ErrorHandler.get(500, e.getMessage());
        }
    }

    /**
     * U02
     * change a User
     *
     * @param user userID or Username as long or String
     * @return returns true or error
     */
    @PutMapping(value = "/put")
    public ResponseEntity putUser(@RequestBody final User user) {
        try {
            if(user.getId()<0) {
                return ResponseEntity.status(HttpStatus.OK).body(this.userController.addAndGetUser(user));
            }
            this.userController.updateUser(user);
            return ResponseEntity.status(HttpStatus.OK).body(this.userController.getUserById(user.getId()));
        } catch(final Exception e) {
            return ErrorHandler.get(500, e.getMessage());
        }
    }

    /*
     * U03
     * rate a recipe
     *
     * @return returns true or error
     * @body userRequest userID or Username as long or String, furthermore the rating as int and RecipeID as long
     */
    @PutMapping(value = "/rate")
    public ResponseEntity rateRecipe(@RequestBody final UserObj.UserRequest userRequest) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(DatabaseController.getInstance().rateRecipe(userRequest));
        } catch (final ResponseException e) {
            return ErrorHandler.get(e.getStatus(), e.getResponse().getMessage());
        }
    }
}