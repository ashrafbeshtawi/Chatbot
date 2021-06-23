package de.dailab.oven.chatbot.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import zone.bot.vici.Language;
import zone.bot.vici.intent.UserMatch;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ChatLogging {

    @Nonnull
    private static final Logger LOG = LoggerFactory.getLogger(ChatLogging.class);
    @Nonnull
    private static final Marker MARKER_CHAT = MarkerFactory.getMarker("CHAT");
    @Nonnull
    private static final Marker MARKER_CHAT_MESSAGE = MarkerFactory.getMarker("CHAT_MESSAGE");
    @Nonnull
    private static final Marker MARKER_INPUT_MESSAGE = MarkerFactory.getMarker("INPUT_MESSAGE");
    @Nonnull
    private static final Marker MARKER_OUTPUT_MESSAGE = MarkerFactory.getMarker("OUTPUT_MESSAGE");
    @Nonnull
    private static final Marker MARKER_CHAT_STATE = MarkerFactory.getMarker("CHAT_STATE");

    @Nullable
    private final Supplier<String> chatStateProvider;

    static {
        MARKER_CHAT_MESSAGE.add(MARKER_CHAT);
        MARKER_INPUT_MESSAGE.add(MARKER_CHAT_MESSAGE);
        MARKER_OUTPUT_MESSAGE.add(MARKER_CHAT_MESSAGE);
        MARKER_CHAT_STATE.add(MARKER_CHAT);
    }

    public ChatLogging(@Nullable final Supplier<String> chatStateSupplier) {
        this.chatStateProvider = chatStateSupplier;
    }

    public void onInputMessage(@Nonnull final Language language, @Nonnull final String message, @Nonnull final UserMatch userMatch) {
        if(this.chatStateProvider != null && LOG.isDebugEnabled()) {
            LOG.debug(MARKER_CHAT_STATE, "Chat State before input message got processed:\n{}", this.chatStateProvider.get());
        }
        LOG.info(MARKER_INPUT_MESSAGE, "Input Message received in {} from user with id {}:\n{}", language.getName(), userMatch.getUserID(), message);
    }

    public void onOutputMessage(@Nonnull final Language language, @Nonnull final String message) {
        LOG.info(MARKER_OUTPUT_MESSAGE, "Output Message sent in {}:\n{}", language.getName(), message);
        if(this.chatStateProvider != null && LOG.isDebugEnabled()) {
            LOG.debug(MARKER_CHAT_STATE, "Chat State after output message got generated:\n{}", this.chatStateProvider.get());
        }
    }

}
