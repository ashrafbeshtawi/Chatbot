package de.dailab.oven.database.exceptions;
public class InputException extends Exception {
	/**
	 * @author Tristan Schroer
	 * @since  11th October, 2019
	 */
	
	private static final long serialVersionUID = 6346278781963515923L;

	public InputException(final String message) {
        super(message);
    }

    public InputException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
