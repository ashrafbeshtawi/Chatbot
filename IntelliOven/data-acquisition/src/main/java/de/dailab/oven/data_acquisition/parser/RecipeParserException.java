package de.dailab.oven.data_acquisition.parser;

/**
 * TODO add missing JavaDoc
 *
 * @author Hendrik Motza
 * @since 18.09
 */
public class RecipeParserException extends Exception {

    public RecipeParserException(final String message) {
        super(message);
    }

    public RecipeParserException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
