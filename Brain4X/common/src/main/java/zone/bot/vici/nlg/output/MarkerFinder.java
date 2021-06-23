package zone.bot.vici.nlg.output;

import zone.bot.vici.nlg.output.MarkerSyntaxConfiguration.ParameterType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

class MarkerFinder {

    class MarkerNode {

        private int begin;
        private int end;
        private int contentBegin;
        private int contentEnd;

        private MarkerData markerData;
        private List<MarkerNode> childMarker;

        public int getBegin() {
            return this.begin;
        }

        public int getEnd() {
            return this.end;
        }

        public int getContentBegin() {
            return this.contentBegin;
        }

        public int getContentEnd() {
            return this.contentEnd;
        }

        public MarkerData getMarkerData() {
            return this.markerData;
        }

        public List<MarkerNode> getChildMarker() {
            return this.childMarker;
        }
    }

    @Nonnull
    private static final Pattern WHITESPACES_PATTERN = Pattern.compile("\\s+");

    @Nonnull
    private final Pattern tagNamePattern;
    @Nonnull
    private final String beginTagPrefix;
    @Nonnull
    private final String beginTagSuffix;
    @Nonnull
    private final String endTagPrefix;
    @Nonnull
    private final String endTagSuffix;
    @Nonnull
    private final ParameterType parameterType;
    @Nullable
    private final String singleParamDelimiter;
    @Nullable
    private final String paramKeyValueDelimiter;

    MarkerFinder(@Nonnull final MarkerSyntaxConfiguration cfg) {
        Objects.requireNonNull(cfg.getTagNameRegex(), "'cfg.getTagNameRegex()' must not be null");
        this.beginTagPrefix = Objects.requireNonNull(cfg.getBeginTagPrefix(), "'cfg.getBeginTagPrefix()' must not be null");
        this.beginTagSuffix = Objects.requireNonNull(cfg.getBeginTagSuffix(), "'cfg.getBeginTagSuffix()' must not be null");
        this.endTagPrefix = Objects.requireNonNull(cfg.getEndTagPrefix(), "'cfg.getEndTagPrefix()' must not be null");
        this.endTagSuffix = Objects.requireNonNull(cfg.getEndTagSuffix(), "'cfg.getEndTagSuffix()' must not be null");
        this.parameterType = Objects.requireNonNull(cfg.getParameterType(), "'cfg.getParameterType()' must not be null");
        if(ParameterType.SINGLE_PARAMETER.equals(cfg.getParameterType()) || ParameterType.SINGLE_AND_NAMED_PARAMETERS.equals(cfg.getParameterType())) {
            this.singleParamDelimiter = Objects.requireNonNull(cfg.getSingleParameterDelimiter(), "'cfg.getSingleParameterDelimiter()' must not be null when single parameters are supported");
        } else {
            this.singleParamDelimiter = null;
        }
        if(ParameterType.NAMED_PARAMETERS.equals(cfg.getParameterType()) || ParameterType.SINGLE_AND_NAMED_PARAMETERS.equals(cfg.getParameterType())) {
            this.paramKeyValueDelimiter = Objects.requireNonNull(cfg.getParameterKeyValueDelimiter(), "'cfg.getParameterKeyValueDelimiter()' must not be null when key-value parameters are supported");
        } else {
            this.paramKeyValueDelimiter = null;
        }
        this.tagNamePattern = Pattern.compile(cfg.getTagNameRegex());
    }

    public List<MarkerNode> findMarker(@Nonnull final String input) {
        return findMarker(input, 0, input.length(), null);
    }

    private List<MarkerNode> findMarker(@Nonnull final String input, final int startIndex, final int endIndex, @Nullable final MarkerNode currentNode) {
        String endSequence = null;
        if(currentNode != null) {
            endSequence = this.endTagPrefix + currentNode.markerData.getTagName() + this.endTagSuffix;
        }
        final List<MarkerNode> markerList = new LinkedList<>();
        for(int i=startIndex; i<endIndex; i++) {
            final char c = input.charAt(i);
            if(c == '\\') {
                i++;
                continue;
            }
            if(endSequence != null && subStringEquals(endSequence, input, i)) {
                currentNode.contentEnd = i;
                currentNode.end = i + endSequence.length();
                currentNode.childMarker = markerList;
                return markerList;
            }
            if(subStringEquals(this.beginTagPrefix, input, i)) {
                final MarkerNode markerNode = processMarker(input, i, endIndex);
                markerList.add(markerNode);
                i = markerNode.end-1;
            }
        }
        if(endSequence != null && currentNode.childMarker == null) {
            throw new PatternSyntaxException("Could not find expected closing marker '"+endSequence+"'", input, startIndex);
        }
        return markerList;
    }

    private MarkerNode processMarker(@Nonnull final String input, final int tagStartIndex, final int endIndex) {
        final int tagNameStartIndex = tagStartIndex + this.beginTagPrefix.length();
        final String tagName = getTagName(input, tagNameStartIndex);
        // extract parameters
        final int tagParameterSectionStartIndex = tagNameStartIndex + tagName.length();
        int tagParameterSectionEndIndex = tagParameterSectionStartIndex;
        String singleParam = null;
        if(ParameterType.SINGLE_PARAMETER.equals(this.parameterType) || ParameterType.SINGLE_AND_NAMED_PARAMETERS.equals(this.parameterType)) {
            final boolean delimiterFound = subStringEquals(this.singleParamDelimiter, input, tagParameterSectionStartIndex);
            if(delimiterFound) {
                tagParameterSectionEndIndex += this.singleParamDelimiter.length();
                final ParameterValueResult parameterValueResult = parseParameterValue(input, new char[]{'"', '\''}, tagParameterSectionEndIndex, false, this.beginTagSuffix);
                singleParam = parameterValueResult.value;
                tagParameterSectionEndIndex = parameterValueResult.endIndex;
            }
        }
        final Map<String, String> namedParams = new HashMap<>();
        if(ParameterType.NAMED_PARAMETERS.equals(this.parameterType) || (singleParam == null && ParameterType.SINGLE_AND_NAMED_PARAMETERS.equals(this.parameterType))) {
            tagParameterSectionEndIndex += countWhitespaces(input, tagParameterSectionEndIndex);
            while(!subStringEquals(this.beginTagSuffix, input, tagParameterSectionEndIndex)) {
                String key = "";
                for(int i=tagParameterSectionEndIndex; i<endIndex; i++) {
                    if(subStringEquals(this.paramKeyValueDelimiter, input, i) || countWhitespaces(input, i)>0) {
                        key = input.substring(tagParameterSectionEndIndex, i);
                        break;
                    }
                }
                if(key.isEmpty()) {
                    throw new PatternSyntaxException("Marker tag is incomplete or malformed", input, tagParameterSectionEndIndex);
                }
                tagParameterSectionEndIndex += key.length() + this.paramKeyValueDelimiter.length();
                final ParameterValueResult valueResult = parseParameterValue(input, new char[]{'"', '\''}, tagParameterSectionEndIndex, true, this.beginTagSuffix);
                namedParams.put(key, valueResult.value);
                tagParameterSectionEndIndex = valueResult.endIndex;
                tagParameterSectionEndIndex += countWhitespaces(input, tagParameterSectionEndIndex);
            }
        }
        if(!subStringEquals(this.beginTagSuffix, input, tagParameterSectionEndIndex)) {
            throw new PatternSyntaxException("Expected but could not determine end of tag", input, tagParameterSectionEndIndex);
        }

        final MarkerNode markerNode = new MarkerNode();
        markerNode.markerData = new MarkerData(tagName, singleParam, namedParams);
        markerNode.begin = tagStartIndex;
        markerNode.contentBegin = tagParameterSectionEndIndex + this.beginTagSuffix.length();
        findMarker(input, markerNode.contentBegin, endIndex, markerNode);
        return markerNode;
    }

    private static int countWhitespaces(@Nonnull final String input, final int startIndex) {
        final Matcher matcher = WHITESPACES_PATTERN.matcher(input);
        if(!matcher.find(startIndex) || matcher.start() != startIndex) {
            return 0;
        }
        return matcher.group().length();
    }

    private String getTagName(@Nonnull final String input, final int tagNameStartIndex) {
        final Matcher matcher = this.tagNamePattern.matcher(input);
        if(!matcher.find(tagNameStartIndex) || matcher.start() != tagNameStartIndex) {
            throw new PatternSyntaxException("No valid tag name found", input, tagNameStartIndex);
        }
        return matcher.group();
    }

    private static class ParameterValueResult {
        // value of the parameter without quotation
        final String value;
        // indexes for begin/end of value including quotations
        final int beginIndex;
        final int endIndex;

        private ParameterValueResult(@Nonnull final String value, final int beginIndex, final int endIndex) {
            this.value = value;
            this.beginIndex = beginIndex;
            this.endIndex = endIndex;
        }
    }

    private static ParameterValueResult parseParameterValue(@Nonnull final String input, final char[] allowedQuoteChars, final int startIndex, final boolean stopAtWhitespaces, final String stopString)  {
        final char firstChar = input.charAt(startIndex);
        boolean isQuoted = false;
        for(final char c : allowedQuoteChars) {
            if(c == firstChar) {
                isQuoted = true;
                break;
            }
        }
        final int valueStartIndex = startIndex + (isQuoted ? 1 : 0);
        for(int i=valueStartIndex;i<input.length();i++) {
            if((isQuoted && input.charAt(i) == firstChar) || (!isQuoted && subStringEquals(stopString, input, i)) || (!isQuoted && stopAtWhitespaces && countWhitespaces(input, i)>0)) {
                return new ParameterValueResult(input.substring(valueStartIndex, i), startIndex, i + (isQuoted ? 1 : 0));
            }
        }
        throw new PatternSyntaxException("Could not detect end of "+(isQuoted ? "quoted parameter" : "parameter/tag"), input, startIndex);
    }

    private static boolean subStringEquals(@Nonnull final String lookupString, @Nonnull final String input, @Nonnull final int index) {
        if(input.length()-index < lookupString.length()) {
            return false;
        }
        for(int i=0;i<lookupString.length();i++) {
            if(lookupString.charAt(i) != input.charAt(index+i)) {
                return false;
            }
        }
        return true;
    }

}
