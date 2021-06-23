package de.dailab.oven.api.interfaces.chat;

import de.dailab.brain4x.nlp.utils.turkish.MaryTTSTurkishPronounciationFix;
import de.dailab.brain4x.nlp.utils.turkish.NumberToTurkishWords;
import de.dailab.oven.chatbot.log.ChatLogging;
import de.dailab.oven.DummyOven;
import de.dailab.oven.api.interfaces.view.ViewController;
import de.dailab.oven.api_common.Sendable;
import de.dailab.oven.api_common.chat.ChatMessage;
import de.dailab.oven.api_common.chat.ChatResponse;
import de.dailab.oven.api_common.chat.ConversationResponse;
import de.dailab.oven.api_common.chat.request.ChatInputMessage;
import de.dailab.oven.api_common.chat.request.ChatRequest;
import de.dailab.oven.api_common.error.ErrorResponse;
import de.dailab.oven.api_common.error.ResponseException;
import de.dailab.oven.chatbot.FallbackHandler;
import de.dailab.oven.controller.DatabaseController;
import de.dailab.oven.controller.WebsocketController;
import de.dailab.oven.database.UserController;
import de.dailab.oven.database.configuration.Configuration;
import de.dailab.oven.database.exceptions.ConfigurationException;
import de.dailab.oven.database.exceptions.DatabaseException;
import de.dailab.oven.database.query.Query;
import de.dailab.oven.model.IntelliOvenAppState;
import marytts.datatypes.MaryDataType;
import marytts.exceptions.MaryConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import zone.bot.vici.Language;
import zone.bot.vici.SkillRegistry;
import zone.bot.vici.intent.DialogManager;
import zone.bot.vici.intent.InputMessage;
import zone.bot.vici.intent.UserMatch;
import zone.bot.vici.nlg.output.BBCodeSyntaxConfiguration;
import zone.bot.vici.nlg.output.MarkerParser;
import zone.bot.vici.nlg.output.processors.StripMarkerProcessor;
import zone.bot.vici.tts.AnnotatedTextToRawMaryXmlConverter;
import zone.bot.vici.tts.MaryTTS;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ChatController {

    @Nonnull
    private static final Logger LOG = LoggerFactory.getLogger(ChatController.class);

    //Singleton
    private static ChatController singleInstance = null;
    //Map with userID and MessageList
    private final Map<Long, List<ChatMessage>> conversationList = new HashMap<>();
    private final ConversationGroup conversationGroup;
    @Nonnull
    private final IntelliOvenAppState appState = new IntelliOvenAppState();

    private ChatController()
            throws MaryConfigurationException, ConfigurationException, DatabaseException {
        this.conversationGroup = new ConversationGroup(this.appState);
    }

    public static synchronized ChatController getInstance() throws DatabaseException, MaryConfigurationException, ConfigurationException {
        if (singleInstance == null) {
                singleInstance = new ChatController();
        }
        return singleInstance;
    }

    /**
     * @param requestObject ChatRequest Object, as transfered via HTTP or Webssocket
     * @return returns Response for returning to Client
     * @throws ResponseException throwse Exception with HTTP code and message to return to client
     */
    public ChatResponse handle(final ChatRequest requestObject) throws ResponseException {

        checkRequest(requestObject);

        //send request to Websocket
        this.sendMessage(WebsocketController.OVEN_CHAT_REQUEST, requestObject.getChatUserMatches()[0].getUserID(), requestObject);


        //set default language if no language set
        for (final ChatInputMessage inputMessage : requestObject.getChatInputMessages()) {
            if (inputMessage.getLanguage().equals(Language.UNDEF)) {
                inputMessage.setLanguage(Language.ENGLISH);
            }
        }

        //call the conversation group
        return this.conversationGroup.handle(requestObject);
    }

    /**
     * checks the Request for missing fields...
     *
     * @param requestObject request object from api
     * @throws ResponseException throws an Response Error
     */
    public static void checkRequest(final ChatRequest requestObject) throws ResponseException {
        //handle Error if no User
        if (requestObject.getChatUserMatches() == null || requestObject.getChatUserMatches().length == 0) {
            final ErrorResponse response = new ErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Required message is not readable, body or user is missing");
            throw new ResponseException(response);
        }

        //handle Error if no Text
        if (requestObject.getChatInputMessages() == null || requestObject.getChatInputMessages().length == 0 || requestObject.getChatInputMessages()[0].getMessage() == null) {
            final ErrorResponse response = new ErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Required message is not readable, body or text is missing");
            //send response to Websocket
            try {
                ChatController.getInstance().sendMessage(WebsocketController.OVEN_CHAT_RESPONSE, requestObject.getChatUserMatches()[0].getUserID(), response);
            } catch (final Exception ignored) {
                //ignored
            }

            throw new ResponseException(response);
        }
    }

    public void sendMessageToUser(@Nonnull final Language l, @Nonnull final String s) {
        this.conversationGroup.handleResponse(l, s);
    }


    /**
     * Sends messages to the Webscoket, saves it in the Conversation if it is a ChatMessage
     *
     * @param topic   the TopicString, WebsocketController. ...
     * @param userID  the userID o the requested user
     * @param message only CHatMessages will be added to the Conversation, every other only gets transmtted to the WebsocketController
     */
    private void sendMessage(final String topic, final long userID, final Sendable message) {

        //add to conversationList, send to conversation Websocket as well
        if (ChatMessage.class.isAssignableFrom(message.getClass())) {
            final List<ChatMessage> l = getChatMessages(userID);
            l.add((ChatMessage) message);
            pingConversation(userID);
            ViewController.getInstance().update(userID);
        }

        WebsocketController.getInstance().send(topic, userID, message);
    }

    public void pingConversation(final long userID) {
        WebsocketController.getInstance().send(WebsocketController.OVEN_CHAT_CONVERSATION, userID, new ConversationResponse(getChatMessages(userID)));
    }

    public ConversationResponse getConversation(final long userID) {
        return new ConversationResponse(getChatMessages(userID));
    }

    private List<ChatMessage> getChatMessages(final long userID) {
        //initilize when empty
        this.conversationList.computeIfAbsent(userID, k -> new ArrayList<>());
        return this.conversationList.get(userID);
    }

    /**
     * resets the conversation. e.g. after goodbye.
     *
     * @param userID the id of the user
     */
    public void resetConversation(final long userID) {
        this.conversationList.remove(userID);
    }

    @Nonnull
    public IntelliOvenAppState getAppState() {
        return this.appState;
    }


    private static class ConversationGroup {

        @Nonnull
        private static final String[] OUTPUT_DEVICE_FILTER_KEYWORDS = new String[]{"ReSpeaker", "seeed2micvoicec"};
        private final MaryTTS mary;
        @Nonnull
        private final MarkerParser chatResponseMarkerParser;
        @Nonnull
        private final DialogManager dm;
        @Nonnull
        private final ChatLogging chatLogging;

        private long currentUserId = 0;

        /** @deprecated
         * Used for arcelik test group
         */
        @Deprecated
        private final File responseLogFile = new File("/home/test/ftp/answer.txt");
        private int responseCounter = 0;

        public ConversationGroup(@Nonnull final IntelliOvenAppState appState)
                throws MaryConfigurationException, ConfigurationException, DatabaseException {
            Objects.requireNonNull(appState, "Parameter 'appState' must not be null");
            this.mary = initMaryTTS();
            this.chatResponseMarkerParser = new MarkerParser(new BBCodeSyntaxConfiguration(), new StripMarkerProcessor());
            this.dm = new DialogManager(new FallbackHandler(), this::handleResponse, new SkillRegistry());
            final UserController userController = new UserController(new Query().getGraph());
            this.chatLogging = new ChatLogging(appState::toString);
            final DatabaseController databaseController = DatabaseController.getInstance();
            this.dm.registerApi(appState, IntelliOvenAppState.class);
            this.dm.registerApi(userController, UserController.class);
            this.dm.registerApi(databaseController, DatabaseController.class);
            this.dm.registerApi(new DummyOven(), DummyOven.class);
            this.dm.init();
        }

        private static MaryTTS initMaryTTS() throws MaryConfigurationException, ConfigurationException {
            final String programDataDirectory = Configuration.getInstance().getProgramDataDirectory();
            final String ttsStateFile = programDataDirectory + "spokenTrue.txt";
            final Mixer mixer = findSpeaker();
            if (mixer == null) {
                LOG.warn("ReSpeaker device was not found, default audio device will be used instead.");
            }
            final MaryTTS tts = mixer == null ? new MaryTTS() : new MaryTTS(mixer);
            tts.setInputType(MaryDataType.RAWMARYXML);
            tts.setPreProcessors(new NumberToTurkishWords(), new MaryTTSTurkishPronounciationFix(), new AnnotatedTextToRawMaryXmlConverter(tts));
            tts.setVolume(0.20f);
            if (new File(ttsStateFile).getParentFile().exists()) {
                tts.setTtsStateFile(ttsStateFile);
                LOG.info("Found base directory for ttsStateFile");
            } else {
                LOG.warn("Could not find subdirectories for ttsStateFile ['" + ttsStateFile + "'] in working directory. For use with Arcelik's ASR solution, check if you ran this application from the right working directory.");
            }
            return tts;
        }

        @Nullable
        private static Mixer findSpeaker() {
            Mixer mixer = null;
            final Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
            for (final Mixer.Info mixerInfo : mixerInfos) {
                LOG.info("Speaker device found: Name='" + mixerInfo.getName() + "', Vendor='" + mixerInfo.getVendor() + "', Description='" + mixerInfo.getDescription() + "'");
                for (final String keyword : OUTPUT_DEVICE_FILTER_KEYWORDS) {
                    if (mixerInfo.getDescription().contains("Direct") && mixerInfo.getDescription().contains(keyword)) {
                        mixer = AudioSystem.getMixer(mixerInfo);
                        LOG.info("ReSpeaker device was found and will be used for audio output");
                    }
                }
            }
            return mixer;
        }

        private void handleResponse(@Nonnull final Language l, @Nonnull final String s) {
            this.chatLogging.onOutputMessage(l, s);
            try {
                this.mary.speak(l, s);
            } catch (final Exception e) {
                LOG.error("Could not generate and play speech from chatbot response", e);
            }
            this.responseCounter++;
            if (this.responseLogFile.getParentFile().exists()) {
                try (final OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(this.responseLogFile), StandardCharsets.UTF_8)) {
                    writer.write(s);
                    writer.write('\n');
                    writer.write(Integer.toString(this.responseCounter));
                } catch (final IOException e) {
                    LOG.error(e.getMessage());
                }
            }
            final ChatResponse response = new ChatResponse(this.chatResponseMarkerParser.process(s), null);
            //back to Websocket and update the View.
            try {
                    ChatController.getInstance().sendMessage(WebsocketController.OVEN_CHAT_RESPONSE, this.currentUserId, response);
            } catch (final Exception ignore) {
                //ignore
            }
        }

        private ChatResponse handle(final ChatRequest requestObject) {
            final UserMatch currentUser = requestObject.getChatUserMatches()[0];
            this.currentUserId = currentUser.getUserID();
            final InputMessage firstMessage = requestObject.getChatInputMessages()[0];
            this.chatLogging.onInputMessage(firstMessage.getLanguage(), firstMessage.getMessage(), currentUser);
            this.dm.handleInputMessage(requestObject.getChatInputMessages(), requestObject.getChatUserMatches(), requestObject.getInputType());

            //return empty HTTP Response
            return new ChatResponse("Accepted", null);

        }
    }


}
