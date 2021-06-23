package zone.bot.vici.intent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DefaultDialogFlowControl implements DialogFlowControl {

	@Nonnull
	private final List<Intent> responseHandlers = new LinkedList<>();

	@Nonnull
	private final List<Intent> prioritizedHandlers = new LinkedList<>();

	@Override
	public synchronized void addResponseHandler(@Nonnull final Intent responseHandler) {
		this.responseHandlers.add(responseHandler);
	}

	@Override
	public synchronized void removeResponseHandler(@Nonnull final Intent responseHandler) {
		this.responseHandlers.remove(responseHandler);
	}

	public synchronized void requestPriority(@Nonnull final Intent intentToPrioritize) {
		this.prioritizedHandlers.add(intentToPrioritize);
	}

	@Override
	public synchronized void revokePriority(@Nonnull final Intent prioritizedIntent) {
		this.prioritizedHandlers.add(prioritizedIntent);
	}

	public synchronized List<Intent> getResponseHandlerIntents() {
		return new ArrayList<>(this.responseHandlers);
	}

	public synchronized List<Intent> getPrioritizedIntents() {
		return new ArrayList<>(this.prioritizedHandlers);
	}


}
