package zone.bot.vici.pattern.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Immutable
public class RegexNode extends BotPatternNode {

	// regular expression to extract the name of named groups in a pattern
	private static final Pattern NAMED_CAPTURING_GROUP_PATTERN = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9_]*)>");

	@Nonnull
	private final Pattern pattern;

	public RegexNode(@Nullable final List<BotPatternEntity> namedEntities, @Nonnull final String regex) {
		this(namedEntities, Pattern.compile(regex));
	}

	public RegexNode(@Nullable final List<BotPatternEntity> namedEntities, @Nonnull final Pattern pattern) {
		super(mergeEntitiesWithNamedCapturingGroups(namedEntities, pattern));
		this.pattern = pattern;
	}

	public Pattern getPattern() {
		return this.pattern;
	}

	private static List<BotPatternEntity> mergeEntitiesWithNamedCapturingGroups(@Nullable final List<BotPatternEntity> namedEntities, @Nonnull final Pattern pattern) {
		final List<BotPatternEntity> mergedList = new LinkedList<>();
		if(namedEntities != null) {
			mergedList.addAll(namedEntities);
		}
		mergedList.addAll(findRegexNamedEntities(pattern.pattern()));
		return mergedList;
	}

	private static int countEscapeCharactersBeforePos(@Nonnull final CharSequence input, final int pos) {
		if(pos==0) return 0;
		for(int i=pos-1; i>=0; i--) {
			if(input.charAt(i) != '\\') {
				return pos-1-i;
			}
		}
		return pos-1;
	}

	private static List<BotPatternEntity> findRegexNamedEntities(@Nonnull final CharSequence pattern) {
		final List<BotPatternEntity> namedEntities = new LinkedList<>();
		final Matcher matcher = NAMED_CAPTURING_GROUP_PATTERN.matcher(pattern);
		while(matcher.find()) {
			final int escapeCharCount = countEscapeCharactersBeforePos(pattern, matcher.start());
			if(escapeCharCount%2 == 1) {
				continue;
			}
			namedEntities.add(new BotPatternEntity(matcher.group(1), null));
		}
		return namedEntities;
	}

	@Override
	protected void nodeToString(@Nonnull final StringBuilder out) {
		out.append("/");
		out.append(this.pattern.pattern().replace("/", "\\/"));
		out.append("/");
	}
}
