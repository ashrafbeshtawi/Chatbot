package de.dailab.oven.api_common.chat.request;

import de.dailab.oven.api_common.chat.ChatMessage;
import zone.bot.vici.intent.IntentRequest.InputType;

public class ChatRequest implements ChatMessage {

    private InputType inputType = InputType.CHAT;
    private ChatInputMessage[] chatInputMessages;
    //set default User
    private ChatUserMatch[] chatUserMatches = new ChatUserMatch[]{new ChatUserMatch()};


    public ChatRequest() {
    }

    public ChatRequest(final ChatInputMessage[] chatInputMessages) {
        this.chatInputMessages = chatInputMessages;
    }

    public ChatInputMessage[] getChatInputMessages() {
        return this.chatInputMessages;
    }

    public void setChatInputMessages(final ChatInputMessage[] chatInputMessages) {
        this.chatInputMessages = chatInputMessages;
    }

    public ChatUserMatch[] getChatUserMatches() {
        return this.chatUserMatches;
    }

    public void setChatUserMatches(final ChatUserMatch[] chatUserMatches) {
        this.chatUserMatches = chatUserMatches;
    }

    public InputType getInputType() {
        return this.inputType;
    }

    public void setInputType(final InputType inputType) {
        this.inputType = inputType;
    }
}
