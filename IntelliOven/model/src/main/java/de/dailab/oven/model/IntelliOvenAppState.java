package de.dailab.oven.model;

import de.dailab.oven.model.data_model.Recipe;
import de.dailab.oven.model.data_model.filters.RecipeFilter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class IntelliOvenAppState {

    public enum DialogState {
        WELCOME("Welcome"), GOODBYE("Goodbye"), RECIPE_CONFIRMATION("RecipeConfirmation"), PROVIDE_RATING("ProvideRating"), RECIPE_STEP("RecipeStep"), USER_PREFERENCES("UserPreferences"), ENROLLING("Enrolling");
        //, COOKING("Cooking"), DEFAULT("Default"), HELP("Help"), OVEN_CONTROL("OvenControl"), SMALLTALK("Smalltalk"), USER_AUTHENTICATION("UserAuthentication"), WEATHER("Weather");

        @Nonnull
        private final String state;

        DialogState(@Nonnull final String name) {
            this.state = name;
        }

        @Nonnull
        public String getState() {
            return this.state;
        }
    }

    public interface DialogStateListener {

        void onDialogStateChanged(@Nonnull final DialogState dialogState);

    }

    @Nullable
    private RecipeFilter recipeFilter;
    @Nullable
    private List<Recipe> recipeSearchResult;
    @Nullable
    private Recipe selectedRecipe;
    private int currentStepIndex = 0;
    
    @Nonnull
    private DialogState dialogState = DialogState.WELCOME;

    @Nonnull
    private final Set<DialogStateListener> dialogStateListeners = new HashSet<>();


    @Nonnull
    public DialogState getDialogState() {
        return this.dialogState;
    }

    public void setDialogState(@Nonnull final DialogState dialogState) {
        Objects.requireNonNull(dialogState, "Parameter 'dialogState' must not be null");
        final boolean notify = this.dialogState != dialogState;
        this.dialogState = dialogState;
        if(!notify) {
            return;
        }
        synchronized(this.dialogStateListeners) {
            for(final DialogStateListener listener : this.dialogStateListeners) {
                listener.onDialogStateChanged(dialogState);
            }
        }
    }

    public void addListener(@Nonnull final DialogStateListener listener) {
        synchronized(this.dialogStateListeners) {
            this.dialogStateListeners.add(listener);
        }
    }

    public void removeListener(@Nonnull final DialogStateListener listener) {
        synchronized(this.dialogStateListeners) {
            this.dialogStateListeners.remove(listener);
        }
    }

    @Nullable
    public RecipeFilter getRecipeFilter() {
        return this.recipeFilter;
    }

    public void setRecipeFilter(@Nullable final RecipeFilter recipeFilter) {
        this.recipeFilter = recipeFilter;
    }

    @Nullable
    public List<Recipe> getRecipeSearchResult() {
        return this.recipeSearchResult;
    }

    public void setRecipeSearchResult(@Nullable final List<Recipe> recipeSearchResult) {
        this.recipeSearchResult = recipeSearchResult;
    }

    @Nullable
    public Recipe getSelectedRecipe() {
        return this.selectedRecipe;
    }

    public void setSelectedRecipe(@Nullable final Recipe selectedRecipe) {
        this.selectedRecipe = selectedRecipe;
        this.currentStepIndex = 0;
    }

    public int getCurrentStepIndex() {
        return this.currentStepIndex;
    }

    public void setCurrentStepIndex(final int currentStepIndex) {
        this.currentStepIndex = currentStepIndex;
    }

    @Override
    public String toString() {
        return "IntelliOvenAppState{" + "dialogState=" + this.dialogState.getState() + '}';
    }
}
