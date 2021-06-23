package zone.bot.vici.pattern.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.List;

@Immutable
public final class WildcardNode extends BotPatternNode {

	public WildcardNode(@Nullable final List<BotPatternEntity> namedEntities) {
		super(namedEntities);
	}

	@Override
	protected void nodeToString(@Nonnull final StringBuilder out) {
		out.append('_');
	}
}
