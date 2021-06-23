package de.dailab.oven.chatbot.intents;

import de.dailab.oven.database.UserController;
import de.dailab.oven.database.exceptions.InputException;
import de.dailab.oven.model.IntelliOvenAppState;
import de.dailab.oven.model.IntelliOvenAppState.DialogState;
import de.dailab.oven.model.data_model.User;
import de.dailab.oven.model.data_model.VegetarianDietCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zone.bot.vici.Language;
import zone.bot.vici.intent.*;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserPreferencesIntent extends IntelliOvenIntent {

	@Nonnull
	private static final Logger LOG = LoggerFactory.getLogger(UserPreferencesIntent.class);

	@Nonnull
	private final UserController userController;

	public UserPreferencesIntent(@Nonnull final MessageOutputChannel channel, @Nonnull final IntelliOvenAppState appState, @Nonnull final UserController userController) {
		super(channel, appState, DialogState.WELCOME, DialogState.GOODBYE, DialogState.PROVIDE_RATING);
		this.userController = userController;
	}

	@Nonnull
	@Override
	public IntentResponse handle(@Nonnull final IntentRequest request) {
		try {
			final User user = this.userController.getUserById(request.getUser().getUserID());
			if(user != null) {
				return handleRequest(request, user);
			}
			getOutputChannel().sendMessageToUser(request.getMessage().getLanguage(), "DefaultAnswers.USER_NOT_LOGGED_IN");
		} catch (final InputException e) {
			getOutputChannel().sendMessageToUser(request.getMessage().getLanguage(), "DefaultAnswers.USER_NOT_LOGGED_IN");
		} catch (final Exception e) {
			LOG.error(e.getMessage(), e);
			getOutputChannel().sendMessageToUser(request.getMessage().getLanguage(), "DefaultAnswers.INTERNAL_ERROR");
		}
		setState(DialogState.GOODBYE);
		return IntentResponse.HANDLED;
	}

	private IntentResponse handleRequest(@Nonnull final IntentRequest request, @Nonnull final User user) throws Exception {
		final Language language = request.getMessage().getLanguage();
		final NamedEntities entities = request.getNamedEntities();
		final Optional<NamedEntity> actionCodeEntity = entities.getSingle("action");
		if(!actionCodeEntity.isPresent()) {
			LOG.info("Action code missing");
			return IntentResponse.NOT_HANDLED;
		}
		final String actionCode = actionCodeEntity.get().getValue();
		if("AddDietLabel".equals(actionCode)) {
			final Optional<NamedEntity> dietTypeEntity = entities.getSingle("dietType");
			if(!dietTypeEntity.isPresent()) {
				LOG.info("Diet type not specified");
				return IntentResponse.NOT_HANDLED;
			}
			final Optional<VegetarianDietCategory> dietCategory = VegetarianDietCategory.valueOf(dietTypeEntity.get().getValue());
			if(!dietCategory.isPresent()) {
				getOutputChannel().sendMessageToUser(language, "UserPreferences.UnknownDietType");
				return IntentResponse.HANDLED;
			}
			this.userController.addPreferences(dietCategory.get(), user);
			getOutputChannel().sendMessageToUser(language, "UserPreferences.AddDietType", Collections.singletonMap("dietType", dietCategory.get()));
		} else if("FilterAllergies".equals(actionCode)) {
			final List<String> allergies = entities.get("allergy").stream().map(NamedEntity::getValue).collect(Collectors.toList());
			for (final String allergy : allergies) {
				this.userController.addIncompatibleIngredient(allergy, user);
			}
			getOutputChannel().sendMessageToUser(language, "UserPreferences.FilterAllergies", Collections.singletonMap("allergies", allergies));
		} else {
			throw new IllegalStateException("Action code '"+actionCode+"' is not defined!");
		}
		setState(DialogState.GOODBYE);
		return IntentResponse.HANDLED;
	}

}
