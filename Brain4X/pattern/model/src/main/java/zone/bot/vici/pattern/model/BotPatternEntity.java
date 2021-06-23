package zone.bot.vici.pattern.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.Objects;

@Immutable
public class BotPatternEntity {

	@Nonnull
	private final String name;
	@Nullable
	private final String fixedValue;

	public BotPatternEntity(@Nonnull final String name, @Nullable final String fixedValue) {
		this.name = Objects.requireNonNull(name, "Parameter 'name' must not be null");
		this.fixedValue = fixedValue;
	}

	@Nonnull
	public String getName() {
		return this.name;
	}

	@Nullable
	public String getFixedValue() {
		return this.fixedValue;
	}

}
