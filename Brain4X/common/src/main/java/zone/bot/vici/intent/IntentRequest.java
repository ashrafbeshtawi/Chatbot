package zone.bot.vici.intent;

import javax.annotation.Nonnull;
import java.util.List;

public interface IntentRequest {

    enum InputType {
        SPEECH, CHAT, EVENT
    }

    @Nonnull
    InputMessage getMessage();

    @Nonnull
    List<MessageToken> getMessageTokens();

    @Nonnull
    UserMatch getUser();

    @Nonnull
    UserMatch[] getAlternativeUsers();

    @Nonnull
    NamedEntities getNamedEntities();

    @Nonnull
    InputType getInputType();

}
