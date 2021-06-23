package zone.bot.vici.intent;

import javax.annotation.Nonnull;

public interface MessageToken {

    /**
     * Return the start position of the token inside the original message.
     *
     * @return start index of raw value
     */
    int begin();

    /**
     * Return the end position of the token inside the original message.
     *
     * @return last index of token
     */
    int end();

    /**
     * Token value from its original source.
     *
     * @return value, never {@code null}
     */
    @Nonnull
    String getValue();

    /**
     * Token value from its original source in lowercase letters.
     *
     * @return lowercase value, never {@code null}
     */
    @Nonnull
    String getLowerCaseValue();

}
