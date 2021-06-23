package zone.bot.vici.pattern.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.List;
import java.util.Objects;

@Immutable
public class ConjunctionListNode extends BotPatternNode {

	@Nonnull
	private final BotPatternNode listNode;
	@Nonnull
	private final BotPatternNode conjunctionNode;

	public ConjunctionListNode(@Nullable final List<BotPatternEntity> namedEntities, @Nonnull final BotPatternNode listNode, @Nonnull final BotPatternNode conjunctionNode) {
		super(namedEntities);
		this.listNode = Objects.requireNonNull(listNode, "Parameter 'listNode' must not be null");
		this.conjunctionNode = Objects.requireNonNull(conjunctionNode, "Parameter 'conjunctionNode' must not be null");
	}

	@Nonnull
	public BotPatternNode getListNode() {
		return this.listNode;
	}

	@Nonnull
	public BotPatternNode getConjunctionNode() {
		return this.conjunctionNode;
	}

	@Override
	protected void nodeToString(@Nonnull final StringBuilder out) {
		out.append(this.listNode.toString());
		out.append("+(");
		out.append(this.conjunctionNode.toString());
		out.append(")");
	}
}
