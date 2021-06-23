package zone.bot.vici.pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zone.bot.vici.intent.NamedEntity;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EqualValuesEntityValidator implements NamedEntitiesValidator {

	@Nonnull
	private static final Logger LOG = LoggerFactory.getLogger(EqualValuesEntityValidator.class);

	@Nonnull
	private final String name;
	@Nonnull
	private final List<String> values;

	public EqualValuesEntityValidator(@Nonnull final String name, @Nonnull final List<String> values) {
		this.name = name;
		this.values = values;
	}

	public EqualValuesEntityValidator(@Nonnull final String name, @Nonnull final String value) {
		this(name, Collections.singletonList(value));
	}

	@Override
	public boolean test(final Map<String, List<NamedEntity>> namedEntities) {
		final List<NamedEntity> foundEntries = namedEntities.get(this.name);
		if(foundEntries == null) {
			LOG.warn("Entity {} not available", this.name);
			return false;
		}
		if(this.values.size() != foundEntries.size()) {
			LOG.warn("Entity {} should have {} entries but {} were found", this.name, this.values.size(), foundEntries.size());
			return false;
		}
		for(int i = 0; i<this.values.size(); i++) {
			final String expected = this.values.get(i);
			final String actual = foundEntries.get(i).getValue();
			if(!expected.equals(actual)) {
				LOG.warn("Entity {} should have value of '{}' instead of '{}' at index {}", this.name, expected, actual, i);
				return false;
			}
		}
		return true;
	}

}
