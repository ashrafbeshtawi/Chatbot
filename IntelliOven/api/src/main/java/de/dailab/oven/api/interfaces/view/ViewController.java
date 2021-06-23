package de.dailab.oven.api.interfaces.view;

import de.dailab.oven.api.interfaces.chat.ChatController;
import de.dailab.oven.api.interfaces.recipe.RecipeController;
import de.dailab.oven.api_common.Sendable;
import de.dailab.oven.api_common.chat.ConversationResponse;
import de.dailab.oven.api_common.recipe.RecipeResponse;
import de.dailab.oven.api_common.view.Viewable;
import de.dailab.oven.controller.WebsocketController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class ViewController {

    public static final int CHATVIEW = 0;
    public static final int RECIPEVIEW = 1;
    //Singleton
    private static ViewController singleInstance = null;
    //map of current Views of current Users
    private final Map<Long, ViewContainer> viewMap = new HashMap<>();

    private ChatController chatController;

    @Nonnull
    private static final Logger LOG = LoggerFactory.getLogger(ChatController.class);

    private ViewController() {
        try {
			this.chatController = ChatController.getInstance();
        } catch (final Exception e){
            LOG.error(e.getMessage(), e);
        }
    }

    public static ViewController getInstance() {
        if (singleInstance == null)
            singleInstance = new ViewController();
        return singleInstance;
    }

    /**
     * v01
     *
     * @param userID userID as long
     * @return the View
     */
    public Viewable getCurrentView(final long userID) {
        final Viewable currentView = getViewContainer(userID).getCurrentView();
        WebsocketController.getInstance().send(WebsocketController.OVEN_VIEW, userID, currentView);
        return currentView;
    }

    /*
     * v02
     *
     * @param userID userID as long
     * @param viewID 0=CHATVIEW, 1=RECIPEVIEW
     * @return
     */
    public Viewable changeView(final long userID, final int viewID) {
        //default is CHatview
        Viewable view = getViewContainer(userID).getConversationResponse();

        if (viewID == RECIPEVIEW) {
            view = getViewContainer(userID).getRecipeResponse();
        }

        getViewContainer(userID).setCurrentView(view);
        WebsocketController.getInstance().send(WebsocketController.OVEN_VIEW, userID, view);
        return view;
    }

    /**
     * v03
     *
     * @param userID           userID as long
     * @param navigationString e.g. up,down,left,right,volUp,volDown,mute,action,back,forth
     * @return boolean equivalent to success
     */
    public boolean navigate(final long userID, final String navigationString) {
        final boolean success;
        boolean validNavigationString = true;
        switch (navigationString) {
            case "up":
                success = getViewContainer(userID).getCurrentView().up();
                break;
            case "down":
                success = getViewContainer(userID).getCurrentView().down();
                break;
            case "left":
                success = getViewContainer(userID).getCurrentView().left();
                break;
            case "right":
                success = getViewContainer(userID).getCurrentView().right();
                break;
            case "volUp":
                success = getViewContainer(userID).getCurrentView().volUp();
                break;
            case "volDown":
                success = getViewContainer(userID).getCurrentView().volDown();
                break;
            case "mute":
                success = getViewContainer(userID).getCurrentView().mute();
                break;
            case "action":
                success = getViewContainer(userID).getCurrentView().action();
                break;
            case "back":
                success = getViewContainer(userID).getCurrentView().back();
                break;
            case "forth":
                success = getViewContainer(userID).getCurrentView().forth();
                break;
            default:
                success = false;
                validNavigationString = false;
        }

        if (validNavigationString)
            WebsocketController.getInstance().send(WebsocketController.OVEN_NAVIGATION, userID, new NavigationStringSendable(navigationString));


        //send current view to websocket
        if (success)
            WebsocketController.getInstance().send(WebsocketController.OVEN_VIEW, userID, getViewContainer(userID).getCurrentView());
        return success;
    }

    /**
     * @param userID userID as long
     * @param index  index of Array position of new Viewed Object
     * @return boolean equivalent to success
     */
    public boolean set(final long userID, final int index) {
        final boolean succ = getViewContainer(userID).getCurrentView().set(index);
        if (succ)
            WebsocketController.getInstance().send(WebsocketController.OVEN_VIEW, userID, getViewContainer(userID).getCurrentView());
        return succ;
    }

    /**
     * updates all views, load new content
     *
     * @param userID userID as long
     */
    public void update(final long userID) {
        getViewContainer(userID).setConversationResponse(this.chatController.getConversation(userID));

        //because we create on each new search a new object and delete the old reference of the currentObject we need to recreate the link of the currentView
        final RecipeResponse recipeResponse = RecipeController.getInstance().getRecipeResponseMap(userID);
        getViewContainer(userID).setRecipeResponse(recipeResponse);
        if (getViewContainer(userID).getCurrentView().getClass() == recipeResponse.getClass())
            getViewContainer(userID).setCurrentView(recipeResponse);

        //send current view to websocket
        WebsocketController.getInstance().send(WebsocketController.OVEN_VIEW, userID, getViewContainer(userID).getCurrentView());
    }

    /**
     * returns the ViewContainer (where the current view and all other views get stored)
     *
     * @param userID userID as long
     * @return ViewContainer
     */
    private ViewContainer getViewContainer(final long userID) {
        //get the current user view, check if its empty...
		this.viewMap.computeIfAbsent(userID, k -> new ViewContainer(
				this.chatController.getConversation(userID),
                RecipeController.getInstance().getRecipeResponseMap(userID))
        );
        return this.viewMap.get(userID);
    }

    private class ViewContainer {

        private RecipeResponse recipeResponse;
        private ConversationResponse conversationResponse;
        private Viewable currentView;

        private ViewContainer(final ConversationResponse conversationResponse, final RecipeResponse recipeResponse) {
            this.conversationResponse = conversationResponse;
            this.recipeResponse = recipeResponse;
            this.currentView = this.conversationResponse;
        }

        ConversationResponse getConversationResponse() {
            return this.conversationResponse;
        }

        void setConversationResponse(final ConversationResponse conversationResponse) {
            this.conversationResponse = conversationResponse;
        }

        RecipeResponse getRecipeResponse() {
            return this.recipeResponse;
        }

        void setRecipeResponse(final RecipeResponse recipeResponse) {
            this.recipeResponse = recipeResponse;
        }

        Viewable getCurrentView() {
            return this.currentView;
        }

        void setCurrentView(final Viewable view) {
            this.currentView = view;
        }
    }

    public static class NavigationStringSendable implements Sendable {
        private final String navigationString;

        NavigationStringSendable(final String navigationString) {
            this.navigationString = navigationString;
        }

        public String getNavigationString() {
            return this.navigationString;
        }
    }
}
