package zone.bot.vici.pattern.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.Collections;
import java.util.List;

@Immutable
public abstract class BotPatternNode {

	@Nonnull
	private final List<BotPatternEntity> namedEntities;

	BotPatternNode(@Nullable final List<BotPatternEntity> namedEntities) {
		if(namedEntities == null || namedEntities.isEmpty()) {
			this.namedEntities = Collections.emptyList();
		} else {
			this.namedEntities = Collections.unmodifiableList(namedEntities);
		}
	}

	@Nonnull
	public List<BotPatternEntity> getNamedEntities() {
		return this.namedEntities;
	}

	protected abstract void nodeToString(@Nonnull final StringBuilder out);

	@Override
	public String toString() {
		final StringBuilder out = new StringBuilder();
		nodeToString(out);
		for(final BotPatternEntity entity : this.namedEntities) {
			out.append('{');
			final String name = entity.getName();
			out.append(name);
			final String fixedValue = entity.getFixedValue();
			if(fixedValue != null) {
				out.append('=');
				out.append(fixedValue);
			}
			out.append('}');
		}
		return out.toString();
	}
}
