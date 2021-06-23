package de.dailab.oven.api_common.chat.request;

import zone.bot.vici.intent.UserMatch;

import javax.annotation.Nonnull;

public class ChatUserMatch implements UserMatch {

    //default User values
    long userID = -1;
    float probability = 1;
    float distance = 0;

    public ChatUserMatch() {
        //DO nothing, JACKSON
    }

    @Nonnull
    @Override
    public long getUserID() {
        return this.userID;
    }

    public void setUserID(final long userID) {
        this.userID = userID;
    }

    @Override
    public float getProbability() {
        return this.probability;
    }

    public void setProbability(final float probability) {
        this.probability = probability;
    }
}
