package zone.bot.vici.intent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class IntentResponse {

    /**
     * Default response to signalize that input message was successfully handled
     */
    public static final IntentResponse HANDLED = new IntentResponse(true);
    /**
     * Default response to signalize that input message could not get handled successfully
     */
    public static final IntentResponse NOT_HANDLED = new IntentResponse(false);

    private final boolean handled;
    @Nullable
    private final Runnable alternativeHandler;

    private IntentResponse(final boolean isHandled) {
        this.handled = isHandled;
        this.alternativeHandler = null;
    }

    /**
     * Creates an instance that signalizes that the intent could not handle the input accordingly and therefore the system might continue looking for other intents that might be able to handle the request.
     * If no better match is found the provided {@code alternativeHandler} might be called. This alternative handler could for example send a message to the user containing usage hints about how the user could interact with this intent.
     *
     * @param alternativeHandler Alternative handling strategy that could be called if no better match by another intent is found
     */
    public IntentResponse(@Nonnull final Runnable alternativeHandler) {
        this.handled = false;
        this.alternativeHandler = alternativeHandler;
    }

    boolean handled() {
        return this.handled;
    }

    @Nullable
    Runnable getAlternativeHandler() {
        return this.alternativeHandler;
    }

}
