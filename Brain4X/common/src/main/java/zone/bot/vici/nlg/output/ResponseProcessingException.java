package zone.bot.vici.nlg.output;

public class ResponseProcessingException extends RuntimeException{

    public ResponseProcessingException(final String message) {
        super(message);
    }

    public ResponseProcessingException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
