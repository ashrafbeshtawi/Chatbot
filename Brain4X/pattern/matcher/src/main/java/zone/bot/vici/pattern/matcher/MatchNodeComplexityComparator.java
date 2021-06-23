package zone.bot.vici.pattern.matcher;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Pattern using Wildcards in combination with multipliers can easily consume more token than they are supposed to.
 * This comparator assumes that a matching solutions is more likely to be preferred if a larger set of pattern nodes got matched because a too greedy wildcard might consume tokens that otherwise might be matched by nodes which might never match anything else otherwise.
 * Therefore a higher complexity (more matching pattern nodes) will be sorted to the top of lists.
 */
public class MatchNodeComplexityComparator implements Comparator<MatcherContext> {

	@Nonnull
	private final Map<MatcherContext, Integer> complexityScoreCache = new HashMap<>();

	private static int calculateScore(@Nonnull final MatcherContext context) {
		return context.getMatches().stream().map(PatternNodeMatch::getNode).collect(Collectors.toSet()).size();
	}

	private Integer getScore(@Nullable final MatcherContext context) {
		if(context == null) {
			return 0;
		}
		return this.complexityScoreCache.computeIfAbsent(context, MatchNodeComplexityComparator::calculateScore);
	}

	@Override
	public int compare(final MatcherContext o1, final MatcherContext o2) {
		return getScore(o2) - getScore(o1);
	}

}
