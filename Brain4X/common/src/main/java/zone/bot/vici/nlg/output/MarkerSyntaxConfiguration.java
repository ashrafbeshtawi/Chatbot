package zone.bot.vici.nlg.output;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class MarkerSyntaxConfiguration {

    public enum ParameterType {
        NO_PARAMETERS, SINGLE_PARAMETER, NAMED_PARAMETERS, SINGLE_AND_NAMED_PARAMETERS
	}

    @Nonnull
    public abstract String getBeginTagPrefix();

    @Nonnull
    public abstract String getBeginTagSuffix();

    @Nonnull
    public abstract String getEndTagPrefix();

    @Nonnull
    public abstract String getEndTagSuffix();

    @Nullable
    public abstract String getTagNameRegex();

    @Nonnull
    public abstract ParameterType getParameterType();

    @Nullable
    public abstract String getSingleParameterDelimiter();

    @Nullable
    public abstract String getParameterKeyValueDelimiter();

}