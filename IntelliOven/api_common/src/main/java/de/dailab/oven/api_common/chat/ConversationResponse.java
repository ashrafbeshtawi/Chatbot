package de.dailab.oven.api_common.chat;


import de.dailab.oven.api_common.Sendable;
import de.dailab.oven.api_common.view.Viewable;

import java.util.List;

public class ConversationResponse implements Sendable, Viewable {

    private List<ChatMessage> conversationList;

    public ConversationResponse(final List<ChatMessage> conversationList) {
        this.conversationList = conversationList;
    }

    public List<ChatMessage> getConversationList() {
        return this.conversationList;
    }

    public void setConversationList(final List<ChatMessage> conversationList) {
        this.conversationList = conversationList;
    }

    @Override
    public boolean up() {
        return false;
    }

    @Override
    public boolean down() {
        return false;
    }

    @Override
    public boolean left() {
        return false;
    }

    @Override
    public boolean right() {
        return false;
    }

    @Override
    public boolean volUp() {
        return false;
    }

    @Override
    public boolean volDown() {
        return false;
    }

    @Override
    public boolean mute() {
        return false;
    }

    @Override
    public boolean action() {
        return false;
    }

    @Override
    public boolean back() {
        return false;
    }

    @Override
    public boolean forth() {
        return false;
    }

    @Override
    public boolean set(final int index) {
        return false;
    }
}
