package zone.bot.vici.exceptions;

import javax.annotation.Nonnull;

public class LifecycleException extends Exception {

	public LifecycleException(@Nonnull final String message, @Nonnull final Throwable cause) {
		super(message, cause);
	}

	public LifecycleException(@Nonnull final String message) {
		super(message);
	}

	public LifecycleException(@Nonnull final Throwable cause) {
		super(cause);
	}

}
