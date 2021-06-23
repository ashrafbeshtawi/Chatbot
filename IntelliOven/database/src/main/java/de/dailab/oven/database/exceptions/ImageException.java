package de.dailab.oven.database.exceptions;

public class ImageException extends Exception
{

	private static final long serialVersionUID = 6509143859557034225L;

	public ImageException() {
        super();
    }

    public ImageException(final String message) {
        super(message);
    }

    public ImageException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ImageException(final Throwable cause) {
        super(cause);
    }

    protected ImageException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
