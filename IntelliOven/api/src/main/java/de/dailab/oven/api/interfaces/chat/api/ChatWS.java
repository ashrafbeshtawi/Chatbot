package de.dailab.oven.api.interfaces.chat.api;

import de.dailab.oven.api.interfaces.chat.ChatController;
import de.dailab.oven.api_common.Sendable;
import de.dailab.oven.api_common.chat.request.ChatRequest;
import de.dailab.oven.api_common.error.ResponseException;
import de.dailab.oven.controller.WebsocketController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@Controller
public class ChatWS {

    @Autowired
    private SimpMessagingTemplate template;

    private ChatController chatController;

    @Nonnull
    private static final Logger LOG = LoggerFactory.getLogger(ChatWS.class);

    //set template in Controller
    @PostConstruct
    public void init() {
        try {
			this.chatController = ChatController.getInstance();
        } catch (final Exception e){
            LOG.error(e.getMessage(), e);
        }
    }


    /**
     * c00
     * Make requests to the ChatGroup for Answers
     *
     * @param requestObject ChatRequest object as defined in pojo
     * @return returns error or nothing (retuirn handling is made in ChatController)
     */
    @MessageMapping("/oven/chat/")
    //@SendTo(WebsocketController.OVEN_CHAT_RESPONSE) we already send it in ChatController
    public Sendable getAnswer(final ChatRequest requestObject) {
        try {
            return this.chatController.handle(requestObject);
        } catch (final ResponseException e) {
            return e.getResponse();
        }

    }

    /**
     * c01
     * Make requests to the DB to get Dishes
     *
     * @param userID userID as long
     */
    @MessageMapping("/oven/chat/ping/{userID}")
    //@SendTo(WebsocketController.OVEN_CHAT_RESPONSE) we already send it in ChatController
    public void ping(@DestinationVariable final long userID) {
		this.chatController.pingConversation(userID);
    }

}
