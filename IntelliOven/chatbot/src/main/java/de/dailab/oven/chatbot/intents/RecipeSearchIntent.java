package de.dailab.oven.chatbot.intents;

import de.dailab.brain4x.nlp.utils.turkish.TurkishWordsToNumber;
import de.dailab.chatbot.aal.utils.ChatbotUtils;
import de.dailab.oven.api_common.recipe.RecipeRequest;
import de.dailab.oven.controller.DatabaseController;
import de.dailab.oven.database.UserController;
import de.dailab.oven.database.exceptions.InputException;
import de.dailab.oven.model.IntelliOvenAppState;
import de.dailab.oven.model.data_model.Category;
import de.dailab.oven.model.data_model.Ingredient;
import de.dailab.oven.model.data_model.Recipe;
import de.dailab.oven.model.data_model.User;
import de.dailab.oven.model.data_model.filters.RecipeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zone.bot.vici.Language;
import zone.bot.vici.intent.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static de.dailab.oven.model.IntelliOvenAppState.DialogState;

public class RecipeSearchIntent extends IntelliOvenIntent {

	@Nonnull
	private static final Logger LOG = LoggerFactory.getLogger(RecipeSearchIntent.class);

	@Nonnull
	private final UserController userController;
	@Nonnull
	private final DatabaseController databaseController;

	public RecipeSearchIntent(@Nonnull final MessageOutputChannel channel, @Nonnull final IntelliOvenAppState appState, @Nonnull final UserController userController, @Nonnull final DatabaseController databaseController) {
		super(channel, appState, DialogState.WELCOME, DialogState.GOODBYE, DialogState.PROVIDE_RATING);
		this.userController = userController;
		this.databaseController = databaseController;
	}

	@Nonnull
	@Override
	public IntentResponse handle(@Nonnull final IntentRequest request) {
		final NamedEntities entities = request.getNamedEntities();
		final Optional<NamedEntity> planEntity = entities.getSingle("plan");
		final Optional<NamedEntity> filterUpdateModeEntity = entities.getSingle("filterUpdateMode");
		final boolean weeklyPlan = planEntity.isPresent() && "WEEKLY".equals(planEntity.get().getValue());
		final User user = getUser(request);
		final RecipeFilter recipeFilter;
		if(filterUpdateModeEntity.isPresent() && "ADD".equals(filterUpdateModeEntity.get().getValue()) && getAppState().getRecipeSearchResult() != null) {
			assert getAppState().getRecipeFilter() != null;
			recipeFilter = extractFilters(getAppState().getRecipeFilter(), entities, request.getMessage().getLanguage());
		} else {
			recipeFilter = extractFilters(new RecipeFilter(), entities, request.getMessage().getLanguage());
			addUserPreferences(user, recipeFilter, request.getMessage().getLanguage());
		}
		final RecipeRequest recipeRequest = prepareRecipeRequest(user, recipeFilter);
		if(LOG.isInfoEnabled()) {
			LOG.info("Querying recipes using filter: {}\n", recipeFilter);
		}

		final HashMap<String, Object> dataModel = new HashMap<>();
		dataModel.put("recipeFilter", recipeFilter);
		final List<Recipe> recipeRecommendations;
		try {
			recipeRecommendations = this.databaseController.getRecipes(recipeRequest);
			LOG.info("{} Recipe suggestions received by recommender", recipeRecommendations.size());
		} catch (final Exception e) {
			LOG.info("Failed to get recipe recommendations by recommender");
			getOutputChannel().sendMessageToUser(request.getMessage().getLanguage(), "DefaultAnswers.INTERNAL_ERROR");
			setState(DialogState.GOODBYE);
			return IntentResponse.HANDLED;
		}
		if(weeklyPlan) {
			return handleWeeklyRequest(request, recipeRecommendations, dataModel);
		}
		return handleRegularRequest(request, recipeRecommendations, recipeFilter, dataModel);
	}

	@Nonnull
	private IntentResponse handleRegularRequest(@Nonnull final IntentRequest request, @Nonnull final List<Recipe> recipeRecommendations, @Nonnull final RecipeFilter recipeFilter, @Nonnull final HashMap<String, Object> dataModel) {
		if(recipeRecommendations.isEmpty()) {
			getOutputChannel().sendMessageToUser(request.getMessage().getLanguage(), "DefaultAnswers.INVALID_MEALNAME_ANSWER", Collections.singletonMap("numOfResults", recipeRecommendations.size()));
			return IntentResponse.HANDLED;
		}
		final Recipe selectedRecipe = recipeRecommendations.remove(0);
		getAppState().setRecipeFilter(recipeFilter);
		getAppState().setRecipeSearchResult(recipeRecommendations);
		getAppState().setSelectedRecipe(selectedRecipe);
		dataModel.put("recipe", selectedRecipe);
		dataModel.put("ingredients", ChatbotUtils.ingredientListAsNiceString(selectedRecipe.getIngredients(), request.getMessage().getLanguage()));
		getOutputChannel().sendMessageToUser(request.getMessage().getLanguage(), "Cooking.RecipeSearchWithIngredients", dataModel);
		setState(DialogState.RECIPE_CONFIRMATION);
		return IntentResponse.HANDLED;
	}

	@Nonnull
	private IntentResponse handleWeeklyRequest(@Nonnull final IntentRequest request, @Nonnull final List<Recipe> recipeRecommendations, @Nonnull final HashMap<String, Object> dataModel) {
		if(recipeRecommendations.size() < 7) {
			getOutputChannel().sendMessageToUser(request.getMessage().getLanguage(), "DefaultAnswers.INVALID_MEALNAME_ANSWER", Collections.singletonMap("numOfResults", recipeRecommendations.size()));
			return IntentResponse.HANDLED;
		}
		final List<Recipe> plan = recipeRecommendations.subList(0,7);
		dataModel.put("weeklyPlan", plan);
		getOutputChannel().sendMessageToUser(request.getMessage().getLanguage(), "Cooking.GetWeeklyPlan", dataModel);
		setState(DialogState.GOODBYE);
		return IntentResponse.HANDLED;
	}

	@Nonnull
	private static RecipeRequest prepareRecipeRequest(@Nullable final User user, @Nonnull final RecipeFilter recipeFilter) {
		final RecipeRequest recipeRequest = new RecipeRequest();
		recipeRequest.setCollaborativeRecommendation(false);
		recipeRequest.setContentBasedRecommendation(true);
		recipeRequest.setRecipeFilter(recipeFilter);
		if(user != null) {
			recipeRequest.setUserID(user.getId());
		}
		return recipeRequest;
	}

	@Nullable
	private User getUser(@Nonnull final IntentRequest request) {
		try {
			return this.userController.getUserById(request.getUser().getUserID());
		} catch (final InputException | InterruptedException e) {
			LOG.warn(e.getMessage(), e);
			return null;
		}
	}

	private static void addUserPreferences(@Nullable final User user, @Nonnull final RecipeFilter recipeFilter, @Nonnull final Language languageOfRequest) {
		if(user == null) {
			recipeFilter.addRecipeLanguage(languageOfRequest);
		} else {
			for(final Ingredient ingredient : user.getIncompatibleIngredients()) {
				recipeFilter.addExcludedIngredient(ingredient);
			}
			for(final Language lang : user.getSpokenLanguages()) {
				recipeFilter.addRecipeLanguage(lang);
			}
			for(final Category category : user.getPreferredCategories()) {
				recipeFilter.addRequiredCategory(category);
			}
		}
	}

	@Nonnull
	private static RecipeFilter extractFilters(@Nonnull final RecipeFilter recipeFilter, @Nonnull final NamedEntities entities, @Nonnull final Language language) {
		final Optional<NamedEntity> conjunctionEntity = entities.getSingle("ConjIngredInc");
		if(conjunctionEntity.isPresent() && "OR".equals(conjunctionEntity.get().getValue())) {
			entities.get("IngredInc").forEach(i -> recipeFilter.addPossibleIngredient(new Ingredient(i.getValue(), language)));
		} else {
			entities.get("IngredInc").forEach(i -> recipeFilter.addRequiredIngredient(new Ingredient(i.getValue(), language)));
		}
		entities.get("IngredExc").forEach(i -> recipeFilter.addExcludedIngredient(new Ingredient(i.getValue(), language)));
		final Duration maxTime = getMaxTime(entities);
		if(maxTime != null) {
			recipeFilter.setCookedWithin(maxTime);
		}
		return recipeFilter;
	}

	@Nullable
	private static Duration getMaxTime(@Nonnull final NamedEntities entities) {
		final Optional<NamedEntity> maxHourOpt = entities.getSingle("MaxTimeHours");
		final Optional<NamedEntity> maxMinOpt = entities.getSingle("MaxTimeMinutes");
		if(!maxHourOpt.isPresent() && !maxMinOpt.isPresent()) {
			return null;
		}
		Duration maxTime = Duration.ZERO;
		if(maxHourOpt.isPresent()) {
			final String v = new TurkishWordsToNumber().apply(maxHourOpt.get().getValue(), Language.TURKISH);
			try {
				maxTime = maxTime.plusHours(Integer.parseInt(v));
			} catch (final IllegalArgumentException e) {
				LOG.error("Could not parse the value '{}' as integer", v, e);
			}
		}
		if(maxMinOpt.isPresent()) {
			final String v = new TurkishWordsToNumber().apply(maxMinOpt.get().getValue(), Language.TURKISH);
			try {
				maxTime = maxTime.plusMinutes(Integer.parseInt(v));
			} catch (final IllegalArgumentException e) {
				LOG.error("Could not parse the value '{}' as integer", v, e);
			}
		}
		return maxTime;
	}

}
