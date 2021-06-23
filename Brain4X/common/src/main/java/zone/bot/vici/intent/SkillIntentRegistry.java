package zone.bot.vici.intent;

import zone.bot.vici.Skill;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

class SkillIntentRegistry {

	static final class IntentContextData {

		@Nonnull
		private final String intentName;

		@Nonnull
		private final Intent intent;

		@Nonnull
		private final Skill skill;

		IntentContextData(@Nonnull final String intentName, @Nonnull final Intent intent, @Nonnull final Skill skill) {
			this.intentName = intentName;
			this.intent = intent;
			this.skill = skill;
		}

		@Nonnull
		public String getIntentName() {
			return this.intentName;
		}

		@Nonnull
		public Intent getIntent() {
			return this.intent;
		}

		@Nonnull
		public Skill getSkill() {
			return this.skill;
		}
	}

	@Nonnull
	private final Map<Intent, IntentContextData> contextMap = new HashMap<>();

	void registerSkill(@Nonnull final Skill skill) {
		for(final Map.Entry<String, Intent> entry : skill.getNamedIntents().entrySet()) {
			this.contextMap.put(entry.getValue(), new IntentContextData(entry.getKey(), entry.getValue(), skill));
		}
	}

	@Nullable
	IntentContextData getContext(@Nonnull final Intent intent) {
		return this.contextMap.get(intent);
	}


}
