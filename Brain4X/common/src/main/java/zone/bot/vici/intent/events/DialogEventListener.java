package zone.bot.vici.intent.events;

public interface DialogEventListener<T extends DialogEvent> {

	void handle(final T event);

}
