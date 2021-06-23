package zone.bot.vici.pattern.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class AlternativesNode extends BotPatternNode {

	@Nonnull
	private final List<BotPatternNode> children;

	public AlternativesNode(@Nullable final List<BotPatternEntity> namedEntities, @Nonnull final List<BotPatternNode> children) {
		super(namedEntities);
		this.children = Collections.unmodifiableList(Objects.requireNonNull(children, "Parameter 'children' must not be null"));
	}

	@Nonnull
	public List<BotPatternNode> getChildren() {
		return this.children;
	}

	@Override
	protected void nodeToString(@Nonnull final StringBuilder out) {
		final Iterator<BotPatternNode> iter = this.children.iterator();
		if(iter.hasNext()) {
			out.append(iter.next().toString());
		}
		while(iter.hasNext()) {
			out.append(" | ");
			out.append(iter.next().toString());
		}
	}

}
