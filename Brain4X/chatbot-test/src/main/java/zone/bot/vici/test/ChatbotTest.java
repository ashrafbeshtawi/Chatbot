package zone.bot.vici.test;

import zone.bot.vici.Language;
import zone.bot.vici.SkillResolverService;
import zone.bot.vici.intent.*;
import zone.bot.vici.intent.IntentRequest.InputType;
import zone.bot.vici.intent.events.DialogEvent;
import zone.bot.vici.intent.events.DialogEventListener;
import zone.bot.vici.test.matcher.MatcherAPI;

import javax.annotation.Nonnull;
import java.util.function.BiFunction;

public class ChatbotTest {

	public static class ChatbotTestBuilder {

		@Nonnull
		private final DialogManager dm;

		private ChatbotTestBuilder(@Nonnull final SkillResolverService skillRegistry, @Nonnull final BiFunction<SkillAPI, IntentRequest, IntentResponse> defaultIntent) {
			this.dm = new DialogManager(defaultIntent, (l, m) -> { }, skillRegistry);
		}

		public <T> ChatbotTestBuilder registerApi(@Nonnull final T instance, @Nonnull final Class<T> apiClass) {
			this.dm.registerApi(instance, apiClass);
			return this;
		}

		public ChatbotTest build() {
			this.dm.init();
			return new ChatbotTest(this.dm);
		}

	}

	@Nonnull
	private final DialogManager dm;

	private ChatbotTest(@Nonnull final DialogManager dm) {
		this.dm = dm;
	}

	public ChatbotTestStepBuilder createTest() {
		return new ChatbotTestStepBuilder(new MatcherAPI() {
			@Override
			public <T extends DialogEvent> void addEventListener(@Nonnull final Class<T> eventType, @Nonnull final DialogEventListener<T> listener) {
				ChatbotTest.this.dm.addEventListener(eventType, listener);
			}
		});
	}

	public void sendMessage(@Nonnull final Language language, final int userId, @Nonnull final String message) {
		sendMessage(language, userId, message, InputType.CHAT);
	}

	public void sendMessage(@Nonnull final Language language, final int userId, @Nonnull final String message, @Nonnull final InputType type) {
		this.dm.handleInputMessage(new InputMessage[] {new InputMessage() {
			@Override
			public float getProbability() {
				return 1;
			}

			@Nonnull
			@Override
			public String getMessage() {
				return message;
			}

			@Nonnull
			@Override
			public Language getLanguage() {
				return language;
			}
		}}, new UserMatch[] {new UserMatch() {
			@Nonnull
			@Override
			public long getUserID() {
				return userId;
			}

			@Override
			public float getProbability() {
				return 1;
			}
		}}, type);
	}

	public static ChatbotTestBuilder configure(@Nonnull final SkillResolverService skillRegistry) {
		return configure(skillRegistry, (skillAPI, intentRequest) -> IntentResponse.NOT_HANDLED);
	}

	public static ChatbotTestBuilder configure(@Nonnull final SkillResolverService skillRegistry, @Nonnull final BiFunction<SkillAPI, IntentRequest, IntentResponse> defaultIntent) {
		return new ChatbotTestBuilder(skillRegistry, defaultIntent);
	}

}
