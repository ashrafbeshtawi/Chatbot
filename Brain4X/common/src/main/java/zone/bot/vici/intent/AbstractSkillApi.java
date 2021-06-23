package zone.bot.vici.intent;

import zone.bot.vici.Language;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractSkillApi implements SkillAPI {

	@Nonnull
	private final MessageOutputChannel outputChannel;
	@Nonnull
	private final DialogFlowControl dialogFlowControl;

	public AbstractSkillApi(@Nonnull final MessageOutputChannel channel, @Nonnull final DialogFlowControl flowControl) {
		this.outputChannel = channel;
		this.dialogFlowControl = flowControl;
	}

	@Nonnull
	@Override
	public DialogFlowControl getDialogFlowControl() {
		return this.dialogFlowControl;
	}

	@Override
	public void sendRawMessageToUser(@Nonnull final Language language, @Nonnull final String message) {
		this.outputChannel.sendRawMessageToUser(language, message);
	}

	@Override
	public void sendMessageToUser(@Nonnull final Language language, @Nonnull final String responseTemplateKey, @Nullable final Object dataModel) {
		this.outputChannel.sendMessageToUser(language, responseTemplateKey, dataModel);
	}

}
