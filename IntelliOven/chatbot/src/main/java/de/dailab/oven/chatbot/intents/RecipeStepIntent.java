package de.dailab.oven.chatbot.intents;

import de.dailab.oven.model.IntelliOvenAppState;
import de.dailab.oven.model.data_model.Recipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zone.bot.vici.intent.*;

import javax.annotation.Nonnull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.dailab.oven.model.IntelliOvenAppState.DialogState;

public class RecipeStepIntent extends IntelliOvenIntent {

	@Nonnull
	private static final Logger LOG = LoggerFactory.getLogger(RecipeStepIntent.class);

	public RecipeStepIntent(@Nonnull final MessageOutputChannel channel, @Nonnull final IntelliOvenAppState appState) {
		super(channel, appState, DialogState.RECIPE_STEP, DialogState.RECIPE_CONFIRMATION);
	}

	@Nonnull
	@Override
	public IntentResponse handle(@Nonnull final IntentRequest request) {
		final NamedEntities entities = request.getNamedEntities();
		final NamedEntity relStepEntity = entities.getSingleOrDefault("relStep", "0");
		try {
			final int stepOffset = Integer.parseInt(relStepEntity.getValue());
			final int stepIndex = getAppState().getCurrentStepIndex() + stepOffset;
			final Recipe recipe = getAppState().getSelectedRecipe();
			assert recipe != null;
			final List<String> instructions = recipe.getInstructions();
			if(stepIndex < 0) {
				getOutputChannel().sendMessageToUser(request.getMessage().getLanguage(), "RecipeStep.TooLowStepNumber");
			} else if(stepIndex > instructions.size()-1) {
				getOutputChannel().sendMessageToUser(request.getMessage().getLanguage(), "RecipeStep.TooHighStepNumber");
			} else {
				getAppState().setCurrentStepIndex(stepIndex);
				final Map<String, Object> datamodel = new HashMap<>();
				datamodel.put("stepIndex", stepIndex);
				datamodel.put("instruction", stepIndex);
				getOutputChannel().sendMessageToUser(request.getMessage().getLanguage(), "RecipeStep.Step", datamodel);
			}
		} catch(final NumberFormatException e) {
			LOG.error("Invalid input, could not parse step number offset of value '{}'", relStepEntity.getValue());
			getOutputChannel().sendMessageToUser(request.getMessage().getLanguage(), "DefaultAnswers.INTERNAL_ERROR");
			return IntentResponse.HANDLED;
		}
		setState(DialogState.RECIPE_STEP);
		return IntentResponse.HANDLED;
	}


}
