package de.dailab.oven.chatbot.intents;

import de.dailab.oven.model.IntelliOvenAppState;
import de.dailab.oven.model.IntelliOvenAppState.DialogState;
import zone.bot.vici.intent.*;

import javax.annotation.Nonnull;
import java.util.Optional;

public class RecipeConfirmationIntent extends IntelliOvenIntent implements IntelliOvenAppState.DialogStateListener {

	@Nonnull
	private final DialogFlowControl flowControl;
	private boolean hasPriority = false;

	public RecipeConfirmationIntent(@Nonnull final SkillAPI skillAPI, @Nonnull final IntelliOvenAppState appState) {
		super(skillAPI, appState, IntelliOvenAppState.DialogState.RECIPE_CONFIRMATION);
		this.flowControl = skillAPI.getDialogFlowControl();
	}

	@Nonnull
	@Override
	public IntentResponse handle(@Nonnull final IntentRequest request) {
		final Optional<NamedEntity> responseOpt = request.getNamedEntities().getSingle("response");
		if(responseOpt.isPresent()) {
			if("YES".equals(responseOpt.get().getValue())) {
				setState(DialogState.RECIPE_STEP);
				getOutputChannel().sendMessageToUser(request.getMessage().getLanguage(), "RecipeConfirmation.ConfirmRecipe");
				return IntentResponse.HANDLED;
			} else if("NO".equals(responseOpt.get().getValue())) {
				setState(DialogState.GOODBYE);
				getOutputChannel().sendMessageToUser(request.getMessage().getLanguage(), "RecipeConfirmation.DontConfirmRecipe");
				return IntentResponse.HANDLED;
			}
		}
		return new IntentResponse(() -> getOutputChannel().sendMessageToUser(request.getMessage().getLanguage(), "DefaultAnswers.I_DID_NOT_UNDERSTAND_ANSWER_CONFIRMATION"));
	}

	@Override
	public void onDialogStateChanged(@Nonnull final DialogState dialogState) {
		if(!this.hasPriority && DialogState.RECIPE_CONFIRMATION.equals(dialogState)) {
			this.flowControl.addResponseHandler(this);
			this.hasPriority = true;
		} else if(this.hasPriority && !DialogState.RECIPE_CONFIRMATION.equals(dialogState)) {
			this.flowControl.removeResponseHandler(this);
			this.hasPriority = false;
		}
	}
}
