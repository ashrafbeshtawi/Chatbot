package zone.bot.vici.intent;

import javax.annotation.Nonnull;

public interface DialogFlowControl {

	void addResponseHandler(@Nonnull final Intent responseHandler);

	void removeResponseHandler(@Nonnull final Intent responseHandler);

	void requestPriority(@Nonnull final Intent intentToPrioritize);

	void revokePriority(@Nonnull final Intent prioritizedIntent);

}
