package de.dailab.oven.database.exceptions;

public class DatabaseException extends Exception
{
	
	private static final long serialVersionUID = -3527182360710929441L;

	public DatabaseException() {
        super();
    }

    public DatabaseException(final String message) {
        super(message);
    }

    public DatabaseException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public DatabaseException(final Throwable cause) {
        super(cause);
    }

    protected DatabaseException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
