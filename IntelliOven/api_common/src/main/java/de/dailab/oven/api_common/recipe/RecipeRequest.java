package de.dailab.oven.api_common.recipe;


import de.dailab.oven.model.data_model.filters.RecipeFilter;

public class RecipeRequest {
    private long userID = -1;
    private RecipeFilter recipeFilter;
    private boolean contentBasedRecommendation;
    private boolean collaborativeRecommendation;
    private int persons;


    public RecipeRequest() {
        //Jockson Builder
    }

    public long getUserID() {
        return this.userID;
    }

    public void setUserID(final long userID) {
        this.userID = userID;
    }

    public RecipeFilter getRecipeFilter() {
        return this.recipeFilter;
    }

    public void setRecipeFilter(final RecipeFilter recipeFilter) {
        this.recipeFilter = recipeFilter;
    }

    public int getPersons() {
        return this.persons;
    }

    public void setPersons(final int persons) {
        this.persons = persons;
    }

    public boolean isContentBasedRecommendation() {
        return this.contentBasedRecommendation;
    }

    public void setContentBasedRecommendation(final boolean contentBasedRecommendation) {
        this.contentBasedRecommendation = contentBasedRecommendation;
    }

    public boolean isCollaborativeRecommendation() {
        return this.collaborativeRecommendation;
    }

    public void setCollaborativeRecommendation(final boolean collaborativeRecommendation) {
        this.collaborativeRecommendation = collaborativeRecommendation;
    }
}
