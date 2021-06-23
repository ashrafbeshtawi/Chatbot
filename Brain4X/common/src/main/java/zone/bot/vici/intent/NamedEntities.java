package zone.bot.vici.intent;

import javax.annotation.Nonnull;
import java.util.*;

public class NamedEntities {

	@Nonnull
	private final Map<String, List<NamedEntity>> entityMap;

	public NamedEntities(@Nonnull final Map<String, List<NamedEntity>> namedEntities) {
		this.entityMap = Objects.requireNonNull(namedEntities, "Parameter 'namedEntities' must not be null");
	}

	@Nonnull
	public Optional<NamedEntity> getSingle(@Nonnull  final String key) {
		final List<NamedEntity> entities = this.entityMap.get(key);
		if(entities == null || entities.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(entities.get(0));
	}

	@Nonnull
	public NamedEntity getSingleOrDefault(@Nonnull final String key, @Nonnull final String defaultValue) {
		final List<NamedEntity> entities = this.entityMap.get(key);
		if(entities == null || entities.isEmpty()) {
			return new StaticNamedEntity(key, defaultValue);
		}
		return entities.get(0);
	}

	public int size() {
		return this.entityMap.size();
	}

	public boolean isEmpty() {
		return this.entityMap.isEmpty();
	}

	public boolean containsKey(final String key) {
		return this.entityMap.containsKey(key);
	}

	@Nonnull
	public List<NamedEntity> get(final String key) {
		return this.entityMap.computeIfAbsent(key,  (k) -> new LinkedList<>());
	}

	@Nonnull
	public Set<String> keySet() {
		return this.entityMap.keySet();
	}

	@Nonnull
	public Collection<List<NamedEntity>> values() {
		return this.entityMap.values();
	}

	@Nonnull
	public Set<Map.Entry<String, List<NamedEntity>>> entrySet() {
		return this.entityMap.entrySet();
	}
}
