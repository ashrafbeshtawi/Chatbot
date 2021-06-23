package de.dailab.oven.database.exceptions;

public class QueryException extends Exception
{

	private static final long serialVersionUID = -7195163963039521511L;

	public QueryException() {
        super();
    }

    public QueryException(final String message) {
        super(message);
    }

    public QueryException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public QueryException(final Throwable cause) {
        super(cause);
    }

    protected QueryException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
