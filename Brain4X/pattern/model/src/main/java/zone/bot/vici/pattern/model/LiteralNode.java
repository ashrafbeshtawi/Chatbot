package zone.bot.vici.pattern.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.List;
import java.util.Objects;

@Immutable
public class LiteralNode extends BotPatternNode {

	@Nonnull
	private final String literal;

	public LiteralNode(@Nullable final List<BotPatternEntity> namedEntities, @Nonnull final String literal) {
		super(namedEntities);
		this.literal = Objects.requireNonNull(literal, "Parameter 'literal' must not be null");
	}

	@Nonnull
	public String getLiteral() {
		return this.literal;
	}

	@Override
	protected void nodeToString(@Nonnull final StringBuilder out) {
		out.append("'");
		out.append(this.literal.replace("'", "\\'"));
		out.append("'");
	}

}
