package de.dailab.oven.controller;

import de.dailab.oven.api_common.Sendable;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.logging.Level;
import java.util.logging.Logger;

public class WebsocketController {

    public static final String OVEN_RECIPE = "/ws-pull/oven/recipe";

    public static final String OVEN_CHAT_RESPONSE = "/ws-pull/oven/chat/response";
    public static final String OVEN_CHAT_REQUEST = "/ws-pull/oven/chat/request";
    public static final String OVEN_CHAT_CONVERSATION = "/ws-pull/oven/chat/conversation";

    public static final String OVEN_VIEW = "/ws-pull/oven/view";
    public static final String OVEN_NAVIGATION = "/ws-pull/oven/navigation";

    public static final String OVEN_MANAGE = "/ws-pull/oven/manage";

    public static final String OVEN_USER = "/ws-pull/oven/user";

    public static final String OVEN_OVEN = "/ws-pull/oven/oven";

    public static final long BROADCAST = 0;

    private static WebsocketController singleInstance = null;
    private SimpMessagingTemplate template;

    private WebsocketController() {
    }

    public static WebsocketController getInstance() {
        if (singleInstance == null)
            singleInstance = new WebsocketController();
        return singleInstance;
    }

    /**
     * Method called at startup
     *
     * @param template template
     */
    public void setTemplate(final SimpMessagingTemplate template) {
        this.template = template;
    }


    /**
     * Function to send the response to a given Websocket
     *
     * @param topic   String of the WEBSOCKET listener
     * @param user    Long of the User, used to append the topic URL to send only to specific user
     * @param message the response as Response class (will be transfered to JSON)
     */
    public void send(final String topic, final long user, final Sendable message) {
        try {
			this.template.convertAndSend(topic + "/" + user, message);
        } catch (final Exception e) {
            Logger.getLogger(WebsocketController.class.toString() + ".send").log(Level.WARNING, e.getMessage());
        }
    }

}
