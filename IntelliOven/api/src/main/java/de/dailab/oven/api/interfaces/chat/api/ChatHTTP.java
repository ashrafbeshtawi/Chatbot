package de.dailab.oven.api.interfaces.chat.api;


import de.dailab.oven.api.helper.serialization.ErrorHandler;
import de.dailab.oven.api.interfaces.chat.ChatController;
import de.dailab.oven.api_common.chat.request.ChatInputMessage;
import de.dailab.oven.api_common.chat.request.ChatRequest;
import de.dailab.oven.api_common.chat.request.ChatUserMatch;
import de.dailab.oven.api_common.error.ResponseException;
import de.dailab.oven.controller.WebsocketController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import zone.bot.vici.Language;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("oven/chat")
public class ChatHTTP {

    @Autowired
    private SimpMessagingTemplate template;

    private ChatController chatController;

    @Nonnull
    private static final Logger LOG = LoggerFactory.getLogger(ChatController.class);

    //set template in Controller
    @PostConstruct
    public void init() {
        WebsocketController.getInstance().setTemplate(this.template);
        try {
			this.chatController = ChatController.getInstance();
        } catch (final Exception e){
            LOG.error(e.getMessage(), e);
        }
    }


    @GetMapping(value = "/getLanguages")
    public static ResponseEntity getLanguages(@RequestBody final ChatRequest requestObject) {
        final Map<String, String> languageMap = new HashMap<>();
        for(final Language language : Language.getLanguages()) {
            languageMap.put(language.getLangCode2(), language.getName());
        }
        return ResponseEntity.status(HttpStatus.OK).body(languageMap);
    }


    /*
     * C00
     * Calls with the given Text and Parameters the COnversation Group
     *
     * @param requestObject
     * @return returns the Answer from the Conversation Group
     */
    @PostMapping(value = "/")
    public ResponseEntity getResponseText(@RequestBody final ChatRequest requestObject) {
        try {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(this.chatController.handle(requestObject));
        } catch (final ResponseException e) {
            return ErrorHandler.get(e.getStatus(), e.getResponse().getMessage());
        }
    }

    @PostMapping(value = "/sendMessageToUser")
    public ResponseEntity sendMessageToUser(@RequestBody final ChatInputMessage message) {
		this.chatController.sendMessageToUser(message.getLanguage(), message.getMessage());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(null);
    }


    /*
     * C99
     * remove, only used for debugging
     *
     * @return remove, only used for debugging
     */
    @PostMapping(value = "/getExample")
    public ResponseEntity getResponseTextExample(@RequestBody final ChatRequest requestObject) {

        //Set filter if not set, only done to show the default...
        if (requestObject.getChatInputMessages() == null || requestObject.getChatInputMessages().length == 0) {

            final ChatInputMessage inputMessage = new ChatInputMessage();
            inputMessage.setLanguage(Language.GERMAN);
            inputMessage.setMessage("Hello");
            inputMessage.setProbability(1);
            final ChatInputMessage[] inputMessages = new ChatInputMessage[]{inputMessage};
            requestObject.setChatInputMessages(inputMessages);

            final ChatUserMatch chatUserMatch = new ChatUserMatch();
            chatUserMatch.setProbability(1);
            chatUserMatch.setUserID(55);

            final ChatUserMatch[] userMatch = new ChatUserMatch[]{chatUserMatch};
            requestObject.setChatUserMatches(userMatch);
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(requestObject);

    }


}