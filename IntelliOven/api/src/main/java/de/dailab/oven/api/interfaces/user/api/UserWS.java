package de.dailab.oven.api.interfaces.user.api;

import de.dailab.oven.api_common.Sendable;
import de.dailab.oven.api_common.error.ErrorResponse;
import de.dailab.oven.api_common.error.ResponseException;
import de.dailab.oven.api_common.user.UserObj;
import de.dailab.oven.controller.DatabaseController;
import de.dailab.oven.controller.WebsocketController;
import de.dailab.oven.database.UserController;
import de.dailab.oven.database.exceptions.ConfigurationException;
import de.dailab.oven.database.exceptions.DatabaseException;
import de.dailab.oven.database.query.Query;
import de.dailab.oven.model.data_model.User;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import javax.annotation.Nonnull;
import java.util.Collections;


@Controller
@MessageMapping("/oven/user")
public class UserWS {


    @Nonnull
    private final UserController userController;

    public UserWS() throws DatabaseException, ConfigurationException {
        this.userController = new UserController(new Query().getGraph());
    }

    /**
     * U01
     * get the given User
     *
     * @param userRequest requestObject see in this class
     * @return the UserList or Error
     */
    @MessageMapping(value = "/get")
    @SendTo(WebsocketController.OVEN_USER)
    public Sendable get(final UserObj.UserRequest userRequest) {
        final String username = userRequest.getUserName();
        final long userId = userRequest.getUserID();
        try {
            if(userId >= 0) {
                return new UserObj.UserResponse(Collections.singletonList(this.userController.getUserById(userId)));
            }
            if(username != null && !username.isEmpty()) {
                return new UserObj.UserResponse(Collections.singletonList(this.userController.getUserByUserName(username)));
            }
            return new UserObj.UserResponse(this.userController.getAllUsers());
        } catch(final Exception e) {
            return new ErrorResponse(500, e.getMessage());
        }
    }

    /**
     * U02
     * change a User
     *
     * @param user userID or Username as long or String
     * @return returns true or error
     */
    @MessageMapping(value = "/put")
    @SendTo(WebsocketController.OVEN_USER)
    public Sendable put(final User user) {
        try {
            return new UserObj.UserResponse(Collections.singletonList(userController.addAndGetUser(user)));
        } catch(final Exception e) {
            return new ErrorResponse(500, e.getMessage());
        }
    }

    /**
     * U03
     * rate a recipe
     *
     * @param userRequest userID or Username as long or String, furthermore the rating as int and RecipeID as long
     * @return returns true or error
     */
    @MessageMapping(value = "/rate")
    @SendTo(WebsocketController.OVEN_USER)
    public Sendable rate(final UserObj.UserRequest userRequest) {
        try {
            return DatabaseController.getInstance().rateRecipe(userRequest);
        } catch (final ResponseException e) {
            return e.getResponse();
        }
    }
}
