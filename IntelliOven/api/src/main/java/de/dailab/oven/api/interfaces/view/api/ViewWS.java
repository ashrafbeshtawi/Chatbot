package de.dailab.oven.api.interfaces.view.api;

import de.dailab.oven.api.interfaces.view.ViewController;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;


@Controller
@MessageMapping("/oven/view")
public class ViewWS {

    /**
     * V01
     * trigger the Websocket to send the current View
     *
     * @param userID userID as long
     */
    @MessageMapping(value = "/get/{userID}")
    //@SendTo(WebsocketController.OVEN_RECIPE) we already send the result in RecipeController
    public void get(@DestinationVariable final long userID) {
        ViewController.getInstance().getCurrentView(userID);
    }

    /**
     * V02
     * change the current View
     *
     * @param userID userID as long
     * @param viewID viewID (0 = chatView; 1= RecipeView)
     */
    @MessageMapping(value = "/change/{userID}/{viewID}")
    //@SendTo(WebsocketController.OVEN_RECIPE) we already send the result in RecipeController
    public void changeCurrentView(@DestinationVariable final long userID, @DestinationVariable final int viewID) {
        ViewController.getInstance().changeView(userID, viewID);
    }

    /**
     * V03
     * Navigate on current VIew
     *
     * @param userID           userID as long
     * @param navigationString String to navigate (e.g. 'up', 'down', 'left'...)
     */
    @MessageMapping(value = "/navigation/{navigationString}/{userID}")
    //@SendTo(WebsocketController.OVEN_RECIPE) we already send the result in RecipeController
    public void navigate(@DestinationVariable final long userID, @DestinationVariable final String navigationString) {
        ViewController.getInstance().navigate(userID, navigationString);
    }

    /*
     * V04
     * Set a new Viewed Object
     *
     * @param userID userID as long
     * @param index  index of Array position of new Viewed Object
     * @return returns 200 or 422, if it is possible/processed or not
     */
    @MessageMapping(value = "/set/{index}/{userID}")
    //@SendTo(WebsocketController.OVEN_RECIPE) we already send the result in RecipeController
    public void set(@DestinationVariable final long userID, @DestinationVariable final int index) {
        ViewController.getInstance().set(userID, index);
    }

}
