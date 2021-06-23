package zone.bot.vici.intent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class StaticNamedEntity implements NamedEntity {

    @Nonnull
    private final String name;

    @Nonnull
    private final String value;

    public StaticNamedEntity(@Nonnull final String name, @Nonnull final String value) {
        this.name = name;
        this.value = value;
    }

    @Nonnull
    @Override
    public String getName() {
        return this.name;
    }

    @Nullable
    @Override
    public String getRawValue() {
        return null;
    }

    @Nonnull
    @Override
    public String getValue() {
        return this.value;
    }

    @Nonnull
    @Override
    public List<MessageToken> getMessageToken() {
        return Collections.emptyList();
    }

    @Override
    public int begin() {
        return 0;
    }

    @Override
    public int end() {
        return 0;
    }
}
