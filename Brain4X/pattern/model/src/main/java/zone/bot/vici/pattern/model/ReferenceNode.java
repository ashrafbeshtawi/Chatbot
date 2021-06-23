package zone.bot.vici.pattern.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@Immutable
public class ReferenceNode extends BotPatternNode {

	@Nonnull
	private final String referenceId;
	@Nonnull
	private final List<BotPatternNode> parameterNodes;

	public ReferenceNode(@Nullable final List<BotPatternEntity> namedEntities, @Nonnull final String referenceId, @Nonnull final List<BotPatternNode> params) {
		super(namedEntities);
		this.referenceId = Objects.requireNonNull(referenceId, "Parameter 'referenceId' must not be null");
		this.parameterNodes = Objects.requireNonNull(params, "Parameter 'referenceId' must not be null");
	}

	@Nonnull
	public String getReferenceId() {
		return this.referenceId;
	}

	@Nonnull
	public List<BotPatternNode> getParameterNodes() {
		return this.parameterNodes;
	}

	@Override
	protected void nodeToString(@Nonnull final StringBuilder out) {
		out.append("~");
		out.append(this.referenceId);
		if(this.parameterNodes.isEmpty()) {
			return;
		}
		out.append('(');
		final Iterator<BotPatternNode> iter = this.parameterNodes.iterator();
		out.append(iter.next());
		while(iter.hasNext()) {
			out.append(", ");
			out.append(iter.next());
		}
		out.append(')');
	}

}
