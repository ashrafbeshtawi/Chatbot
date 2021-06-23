package de.dailab.oven.api_common.chat;

import de.dailab.oven.model.data_model.Recipe;

import java.util.List;

public class ChatResponse implements ChatMessage {

    //normal Object
    private String response;
    private List<Recipe> recipes;

    public ChatResponse(final String response, final List<Recipe> recipes) {
        this.response = response;
        this.recipes = recipes;
    }

    private ChatResponse() {
    }

    //get
    public String getResponse() {
        return this.response;
    }


}
