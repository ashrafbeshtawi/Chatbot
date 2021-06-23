package de.dailab.oven.api_common.chat.request;

import zone.bot.vici.Language;
import zone.bot.vici.intent.InputMessage;

import javax.annotation.Nonnull;

public class ChatInputMessage implements InputMessage {

    String message;
    Language language = Language.UNDEF;
    float probability;
    float distance;

    public ChatInputMessage() {
        //DO nothing, JACKSON
    }

    @Nonnull
    @Override
    public String getMessage() {
        return this.message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    @Override
    public float getProbability() {
        return this.probability;
    }

    public void setProbability(final float probability) {
        this.probability = probability;
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return this.language;
    }

    public void setLanguage(final Language language) {
        this.language = language;
    }

}
