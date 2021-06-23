package zone.bot.vici.pattern.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class BotPatternContext {

	@Nonnull
	private final Map<String, BotPatternNode> templates = new HashMap<>();

	public void addTemplate(@Nonnull final String name, @Nonnull final BotPatternNode template) {
		this.templates.put(name, template);
	}

	@Nullable
	public BotPatternNode getTemplate(@Nonnull final String name) {
		return this.templates.get(name);
	}

}
