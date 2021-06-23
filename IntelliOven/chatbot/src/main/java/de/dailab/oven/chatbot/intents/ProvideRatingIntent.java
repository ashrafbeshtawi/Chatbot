package de.dailab.oven.chatbot.intents;

import de.dailab.oven.api_common.error.ResponseException;
import de.dailab.oven.api_common.user.UserObj;
import de.dailab.oven.controller.DatabaseController;
import de.dailab.oven.database.UserController;
import de.dailab.oven.database.exceptions.InputException;
import de.dailab.oven.model.IntelliOvenAppState;
import de.dailab.oven.model.IntelliOvenAppState.DialogState;
import de.dailab.oven.model.data_model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zone.bot.vici.intent.IntentRequest;
import zone.bot.vici.intent.IntentResponse;
import zone.bot.vici.intent.NamedEntity;
import zone.bot.vici.intent.SkillAPI;

import javax.annotation.Nonnull;
import java.util.Optional;

public class ProvideRatingIntent extends IntelliOvenIntent {

	private static final Logger LOG = LoggerFactory.getLogger(ProvideRatingIntent.class);

	@Nonnull
	private final UserController userController;
	@Nonnull
	private final DatabaseController databaseController;

	public ProvideRatingIntent(@Nonnull final SkillAPI skillAPI, @Nonnull final IntelliOvenAppState appState, @Nonnull final UserController userController, @Nonnull final DatabaseController databaseController) {
		super(skillAPI, appState, DialogState.PROVIDE_RATING);
		this.userController = userController;
		this.databaseController = databaseController;
	}

	@Nonnull
	@Override
	public IntentResponse handle(@Nonnull final IntentRequest request) {
		final Optional<NamedEntity> ratingOpt = request.getNamedEntities().getSingle("rating");
		if(!ratingOpt.isPresent()) {
			return IntentResponse.NOT_HANDLED;
		}
		final String ratingValue = ratingOpt.get().getValue();
		try {
			final int rating = Integer.parseInt(ratingValue);
			final User user = this.userController.getUserById(request.getUser().getUserID());
			if(user == null) {
				getOutputChannel().sendMessageToUser(request.getMessage().getLanguage(), "DefaultAnswers.USER_NOT_LOGGED_IN");
			} else {
				final int adjustedRating = rating * 2 - 10;
				assert getAppState().getSelectedRecipe() != null;
				this.databaseController.rateRecipe(new UserObj.UserRequest(user.getId(), adjustedRating, getAppState().getSelectedRecipe().getId()));
				getOutputChannel().sendMessageToUser(request.getMessage().getLanguage(), "ProvideRating");
			}
		} catch (final InputException e) {
			getOutputChannel().sendMessageToUser(request.getMessage().getLanguage(), "DefaultAnswers.USER_NOT_LOGGED_IN");
		} catch(final NumberFormatException e) {
			LOG.warn("Could not convert input '{}' into an integer", ratingValue);
			getOutputChannel().sendMessageToUser(request.getMessage().getLanguage(), "DefaultAnswers.INTERNAL_ERROR");
		} catch (final ResponseException | InterruptedException e) {
			getOutputChannel().sendMessageToUser(request.getMessage().getLanguage(), "DefaultAnswers.INTERNAL_ERROR");
		}
		setState(DialogState.GOODBYE);
		return IntentResponse.HANDLED;
	}
}
