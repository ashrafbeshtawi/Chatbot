package zone.bot.vici.intent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface NamedEntity {

    /**
     * Name/Identifier for this entity.
     *
     * @return entity name, never {@code null}
     */
    @Nonnull
    String getName();

    /**
     * Original value from entity before any transformations were made.
     *
     * @return original value or {@code null} if not related to a real input
     */
    @Nullable
    String getRawValue();

    /**
     * Value of this entity. Can be the original value from entity
     *
     * @return entity value, never {@code null}
     */
    @Nonnull
    String getValue();

    /**
     * Get related message token.
     * The list will be empty if this entity does not have any related message token (for example if static entities got assigned).
     * The list can also contain several token when this entity relates to more than one token (for example if a group of words is matched to this entity).
     *
     * @return
     */
    @Nonnull
    List<MessageToken> getMessageToken();

    /**
     * Return the start position of the entities raw value inside the original message or {@code -1} if there is no real occurence.
     *
     * @return start index of raw value or -1 if it does not exist
     */
    int begin();

    /**
     * Return the end position of the entities raw value inside the original message or {@code -1} if there is no real occurence.
     *
     * @return last index of raw value or -1 if it does not exist
     */
    int end();

}
