package zone.bot.vici.pattern.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.List;
import java.util.Objects;

@Immutable
public final class WordNode extends BotPatternNode {

	@Nonnull
	private final String word;

	public WordNode(@Nullable final List<BotPatternEntity> namedEntities, @Nonnull final String word) {
		super(namedEntities);
		this.word = Objects.requireNonNull(word, "Parameter 'word' must not be null");
	}

	@Nonnull
	public String getWord() {
		return this.word;
	}

	@Override
	protected void nodeToString(@Nonnull final StringBuilder out) {
		out.append(this.word);
	}
}
