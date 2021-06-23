package zone.bot.vici.nlg.output;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HTMLSyntaxConfiguration extends MarkerSyntaxConfiguration {

    @Nonnull
    @Override
    public String getBeginTagPrefix() {
        return "<";
    }

    @Nonnull
    @Override
    public String getBeginTagSuffix() {
        return ">";
    }

    @Nonnull
    @Override
    public String getEndTagPrefix() {
        return "</";
    }

    @Nonnull
    @Override
    public String getEndTagSuffix() {
        return ">";
    }

    @Nullable
    @Override
    public String getTagNameRegex() {
        return "[A-Za-z][A-Za-z0-9_]*";
    }

    @Nonnull
    @Override
    public ParameterType getParameterType() {
        return ParameterType.NAMED_PARAMETERS;
    }

    @Nullable
    @Override
    public String getSingleParameterDelimiter() {
        return null;
    }

    @Nullable
    @Override
    public String getParameterKeyValueDelimiter() {
        return "=";
    }
}
