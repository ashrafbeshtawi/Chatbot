package zone.bot.vici.pattern.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.List;
import java.util.Objects;

@Immutable
public class GroupNode extends BotPatternNode {

	@Nonnull
	private final BotPatternNode child;

	public GroupNode(@Nullable final List<BotPatternEntity> namedEntities, @Nonnull final BotPatternNode child) {
		super(namedEntities);
		this.child = Objects.requireNonNull(child, "Parameter 'child' must not be null");
	}

	@Nonnull
	public BotPatternNode getChild() {
		return this.child;
	}

	@Override
	protected void nodeToString(@Nonnull final StringBuilder out) {
		out.append('(');
		out.append(this.child.toString());
		out.append(')');
	}
}
