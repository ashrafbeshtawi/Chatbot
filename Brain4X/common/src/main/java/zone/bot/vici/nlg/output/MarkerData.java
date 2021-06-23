package zone.bot.vici.nlg.output;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

public class MarkerData {

    @Nonnull
    private final String tagName;
    @Nonnull
    private final Optional<String> optUnnamedParameter;
    @Nonnull
    private final Map<String, String> namedParameters;

    public MarkerData(@Nonnull final String tagName, @Nullable final String unnamedParameter, @Nonnull final Map<String, String> namedParameters) {
        this.tagName = tagName;
        this.optUnnamedParameter = Optional.ofNullable(unnamedParameter);
        this.namedParameters = namedParameters;
    }

    @Nonnull
    public String getTagName() {
        return this.tagName;
    }

    @Nonnull
    public Optional<String> getOptUnnamedParameter() {
        return this.optUnnamedParameter;
    }

    @Nonnull
    public Map<String, String> getNamedParameters() {
        return this.namedParameters;
    }
}
