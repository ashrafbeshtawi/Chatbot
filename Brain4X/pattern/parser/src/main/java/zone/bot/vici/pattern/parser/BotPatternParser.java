package zone.bot.vici.pattern.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.IntervalSet;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zone.bot.vici.pattern.grammar.PatternGrammar;
import zone.bot.vici.pattern.grammar.PatternLexer;
import zone.bot.vici.pattern.model.BotPattern;
import zone.bot.vici.pattern.model.BotPatternContext;
import zone.bot.vici.pattern.model.BotPatternNode;

import javax.annotation.Nonnull;
import java.util.stream.Collectors;

public class BotPatternParser {

	private static class CustomErrorStrategy extends DefaultErrorStrategy {
		@Override
		public void reportError(final Parser recognizer, final RecognitionException e) {
			throw e;
		}

		@Override
		protected void reportNoViableAlternative(final Parser recognizer, final NoViableAltException e) {
			throw e;
		}

		@Override
		protected void reportInputMismatch(final Parser recognizer, final InputMismatchException e) {
			throw e;
		}

		@Override
		protected void reportFailedPredicate(final Parser recognizer, final FailedPredicateException e) {
			throw e;
		}

		/*
		@Override
		protected void reportUnwantedToken(final Parser recognizer) {
			super.reportUnwantedToken(recognizer);
		}
		 */

		@Override
		protected void reportMissingToken(final Parser recognizer) {
			this.beginErrorCondition(recognizer);
			final Token t = recognizer.getCurrentToken();
			final IntervalSet expecting = this.getExpectedTokens(recognizer);
			final String missingToken = expecting.toString(recognizer.getVocabulary());
			final String msg = String.format("line %d:%d missing token %s at %s", t.getLine(), t.getCharPositionInLine(), missingToken, this.getTokenErrorDisplay(t));
			throw new RecognitionException(msg, recognizer, recognizer.getInputStream(), recognizer.getContext());
		}
	}

	@Nonnull
	private static final Logger LOG = LoggerFactory.getLogger(BotPatternParser.class);
	@Nonnull
	private static final ANTLRErrorStrategy ERROR_HANDLER = new CustomErrorStrategy();

	@Nonnull
	private final BotPatternContext context;

	public BotPatternParser() {
		this(new BotPatternContext());
	}

	public BotPatternParser(@Nonnull final BotPatternContext context) {
		this.context = context;
	}

	public void parseAndRegisterTemplate(@Nonnull final String name, @Nonnull final String templatePattern) {
		this.context.addTemplate(name, parsePattern(templatePattern));
	}

	public BotPattern parse(@Nonnull final String pattern) {
		return new BotPattern(parsePattern(pattern), this.context);
	}

	private static BotPatternNode parsePattern(@Nonnull final String pattern) {
		if(LOG.isTraceEnabled()) {
			final PatternLexer l = new PatternLexer(CharStreams.fromString(pattern));
			final String tokens = l.getAllTokens().stream().map(t -> PatternLexer.VOCABULARY.getSymbolicName(t.getType())).collect(Collectors.joining(","));
			LOG.trace("Lexed Tokens: {}", tokens);
		}
		final PatternLexer lexer = new PatternLexer(CharStreams.fromString(pattern));
		final PatternGrammar grammarParser = new PatternGrammar(new CommonTokenStream(lexer));
		grammarParser.setErrorHandler(ERROR_HANDLER);
		final BotPatternGenerator generator = new BotPatternGenerator();
		final PatternGrammar.PatternContext tree = grammarParser.pattern();
		ParseTreeWalker.DEFAULT.walk(generator, tree);
		return generator.create();
	}

}
