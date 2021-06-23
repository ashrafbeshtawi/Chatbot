package zone.bot.vici.intent.events;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Predicate;

public final class FilteredListener<T extends DialogEvent> implements DialogEventListener<T> {

	@Nonnull
	private final DialogEventListener<T> listener;
	@Nonnull
	private final Predicate<T>[] predicates;

	public FilteredListener(@Nonnull final DialogEventListener<T> listener, @Nonnull final Predicate<T>[] predicates) {
		this.listener = Objects.requireNonNull(listener, "Parameter 'listener' must not be null");
		this.predicates = Objects.requireNonNull(predicates, "Parameter 'predicates' must not be null");
	}

	@Override
	public void handle(final T event) {
		for(final Predicate<T> predicate : this.predicates) {
			if(!predicate.test(event)) {
				return;
			}
		}
		this.listener.handle(event);
	}

	@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	@Override
	public boolean equals(final Object o) {
		return this.listener.equals(o);
	}

	@Override
	public int hashCode() {
		return this.listener.hashCode();
	}
}
