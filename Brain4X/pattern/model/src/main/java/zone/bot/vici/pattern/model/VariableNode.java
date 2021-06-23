package zone.bot.vici.pattern.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.List;

@Immutable
public final class VariableNode extends BotPatternNode {

	private final int index;

	public VariableNode(@Nullable final List<BotPatternEntity> namedEntities, final int varIndex) {
		super(namedEntities);
		if(varIndex<0) {
			throw new IllegalArgumentException("Parameter 'varIndex' must not be negative");
		}
		this.index = varIndex;
	}

	public int getIndex() {
		return this.index;
	}

	@Override
	protected void nodeToString(@Nonnull final StringBuilder out) {
		out.append('$').append(this.index);
	}
}
