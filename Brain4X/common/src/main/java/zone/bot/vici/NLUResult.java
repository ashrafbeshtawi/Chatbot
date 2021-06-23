package zone.bot.vici;

import zone.bot.vici.intent.Intent;
import zone.bot.vici.intent.NamedEntity;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public class NLUResult {

	@Nonnull
	private final Intent intent;
	@Nonnull
	private final Map<String, List<NamedEntity>> namedEntities;

	public NLUResult(@Nonnull final Intent intent, @Nonnull final Map<String, List<NamedEntity>> namedEntities) {
		this.intent = intent;
		this.namedEntities = namedEntities;
	}

	@Nonnull
	public Intent getIntent() {
		return this.intent;
	}

	@Nonnull
	public Map<String, List<NamedEntity>> getNamedEntities() {
		return this.namedEntities;
	}

}
