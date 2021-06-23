package zone.bot.vici.nlg.output;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BBCodeSyntaxConfiguration extends MarkerSyntaxConfiguration {

    @Nonnull
    @Override
    public String getBeginTagPrefix() {
        return "[";
    }

    @Nonnull
    @Override
    public String getBeginTagSuffix() {
        return "]";
    }

    @Nullable
    @Override
    public String getTagNameRegex() {
        return "[A-Za-z][A-Za-z0-9_]*";
    }

    @Nonnull
    @Override
    public String getEndTagPrefix() {
        return "[/";
    }

    @Nonnull
    @Override
    public String getEndTagSuffix() {
        return "]";
    }

    @Nonnull
    @Override
    public ParameterType getParameterType() {
        return MarkerSyntaxConfiguration.ParameterType.SINGLE_AND_NAMED_PARAMETERS;
    }

    @Nullable
    @Override
    public String getSingleParameterDelimiter() {
        return "=";
    }

    @Nullable
    @Override
    public String getParameterKeyValueDelimiter() {
        return "=";
    }
}
