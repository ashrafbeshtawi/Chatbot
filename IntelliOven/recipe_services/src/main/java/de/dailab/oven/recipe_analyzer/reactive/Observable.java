package de.dailab.oven.recipe_analyzer.reactive;

import java.util.LinkedList;
import java.util.List;

/**
 * Implements the observerable design pattern. Intended to be subclassed with concrete events.
 *
 * @param <T> The type of observer
 */
public abstract class Observable<T>
{
    /**
     * Interface to allow easy use of lambda.
     */
    protected interface Event<U>
    {
        void sendEvent(U observer);
    }

    private final List<T> observers = new LinkedList<>();

    public void subscribe(final T handler)
    {
		this.observers.add(handler);
    }

    public void unsubscribe(final T handler)
    {
		this.observers.remove(handler);
    }

    protected void sendEvent(final Event<T> event)
    {
        for (final T e : this.observers)
        {
            event.sendEvent(e);
        }
    }

    /**
     * @return Whether there is at least one subscriber.
     */
    public boolean hasSubscribers()
    {
        return !this.observers.isEmpty();
    }

}
