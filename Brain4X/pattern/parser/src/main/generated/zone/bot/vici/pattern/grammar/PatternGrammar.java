// Generated from zone\bot\vici\pattern\grammar\PatternGrammar.g4 by ANTLR 4.8
package zone.bot.vici.pattern.grammar;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class PatternGrammar extends Parser {
	static { RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		VARIABLE=1, ALTERNATIVE=2, GROUP_BEGIN=3, GROUP_END=4, ENTITY_BEGIN=5, 
		WILDCARD=6, OPTIONAL=7, CONJ_MANY=8, MANY=9, ANY=10, LIST_SEPARATOR=11, 
		REFERENCE=12, QUOTED_STRING=13, REGEX_PATTERN=14, WORD=15, NL=16, WS=17, 
		ENTITY_NAME=18, ENTITY_ASSIGN=19, ENTITY_VALUE=20, ENTITY_END=21;
	public static final int
		RULE_pattern = 0, RULE_patternNode = 1, RULE_entity = 2, RULE_parameterNodeList = 3;
	private static String[] makeRuleNames() {
		return new String[] {
			"pattern", "patternNode", "entity", "parameterNodeList"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, "'|'", "'('", "')'", "'{'", "'_'", "'?'", "'+('", "'+'", 
			"'*'", "','", null, null, null, null, null, null, null, "'='", null, 
			"'}'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "VARIABLE", "ALTERNATIVE", "GROUP_BEGIN", "GROUP_END", "ENTITY_BEGIN", 
			"WILDCARD", "OPTIONAL", "CONJ_MANY", "MANY", "ANY", "LIST_SEPARATOR", 
			"REFERENCE", "QUOTED_STRING", "REGEX_PATTERN", "WORD", "NL", "WS", "ENTITY_NAME", 
			"ENTITY_ASSIGN", "ENTITY_VALUE", "ENTITY_END"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "PatternGrammar.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public PatternGrammar(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class PatternContext extends ParserRuleContext {
		public PatternNodeContext patternNode() {
			return getRuleContext(PatternNodeContext.class,0);
		}
		public TerminalNode EOF() { return getToken(PatternGrammar.EOF, 0); }
		public PatternContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pattern; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PatternGrammarListener ) ((PatternGrammarListener)listener).enterPattern(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PatternGrammarListener ) ((PatternGrammarListener)listener).exitPattern(this);
		}
	}

	public final PatternContext pattern() throws RecognitionException {
		PatternContext _localctx = new PatternContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_pattern);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(8);
			patternNode(0);
			setState(9);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PatternNodeContext extends ParserRuleContext {
		public PatternNodeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_patternNode; }
	 
		public PatternNodeContext() { }
		public void copyFrom(PatternNodeContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class RegexNodeContext extends PatternNodeContext {
		public Token patternValue;
		public TerminalNode REGEX_PATTERN() { return getToken(PatternGrammar.REGEX_PATTERN, 0); }
		public List<EntityContext> entity() {
			return getRuleContexts(EntityContext.class);
		}
		public EntityContext entity(int i) {
			return getRuleContext(EntityContext.class,i);
		}
		public RegexNodeContext(PatternNodeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PatternGrammarListener ) ((PatternGrammarListener)listener).enterRegexNode(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PatternGrammarListener ) ((PatternGrammarListener)listener).exitRegexNode(this);
		}
	}
	public static class WordNodeContext extends PatternNodeContext {
		public Token value;
		public TerminalNode WORD() { return getToken(PatternGrammar.WORD, 0); }
		public List<EntityContext> entity() {
			return getRuleContexts(EntityContext.class);
		}
		public EntityContext entity(int i) {
			return getRuleContext(EntityContext.class,i);
		}
		public WordNodeContext(PatternNodeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PatternGrammarListener ) ((PatternGrammarListener)listener).enterWordNode(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PatternGrammarListener ) ((PatternGrammarListener)listener).exitWordNode(this);
		}
	}
	public static class SequenceNodeContext extends PatternNodeContext {
		public List<PatternNodeContext> patternNode() {
			return getRuleContexts(PatternNodeContext.class);
		}
		public PatternNodeContext patternNode(int i) {
			return getRuleContext(PatternNodeContext.class,i);
		}
		public List<TerminalNode> WS() { return getTokens(PatternGrammar.WS); }
		public TerminalNode WS(int i) {
			return getToken(PatternGrammar.WS, i);
		}
		public SequenceNodeContext(PatternNodeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PatternGrammarListener ) ((PatternGrammarListener)listener).enterSequenceNode(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PatternGrammarListener ) ((PatternGrammarListener)listener).exitSequenceNode(this);
		}
	}
	public static class AlternativesNodeContext extends PatternNodeContext {
		public List<PatternNodeContext> patternNode() {
			return getRuleContexts(PatternNodeContext.class);
		}
		public PatternNodeContext patternNode(int i) {
			return getRuleContext(PatternNodeContext.class,i);
		}
		public TerminalNode ALTERNATIVE() { return getToken(PatternGrammar.ALTERNATIVE, 0); }
		public List<TerminalNode> WS() { return getTokens(PatternGrammar.WS); }
		public TerminalNode WS(int i) {
			return getToken(PatternGrammar.WS, i);
		}
		public AlternativesNodeContext(PatternNodeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PatternGrammarListener ) ((PatternGrammarListener)listener).enterAlternativesNode(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PatternGrammarListener ) ((PatternGrammarListener)listener).exitAlternativesNode(this);
		}
	}
	public static class ReferenceNodeContext extends PatternNodeContext {
		public Token referenceId;
		public TerminalNode REFERENCE() { return getToken(PatternGrammar.REFERENCE, 0); }
		public ParameterNodeListContext parameterNodeList() {
			return getRuleContext(ParameterNodeListContext.class,0);
		}
		public List<EntityContext> entity() {
			return getRuleContexts(EntityContext.class);
		}
		public EntityContext entity(int i) {
			return getRuleContext(EntityContext.class,i);
		}
		public ReferenceNodeContext(PatternNodeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PatternGrammarListener ) ((PatternGrammarListener)listener).enterReferenceNode(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PatternGrammarListener ) ((PatternGrammarListener)listener).exitReferenceNode(this);
		}
	}
	public static class LiteralNodeContext extends PatternNodeContext {
		public Token value;
		public TerminalNode QUOTED_STRING() { return getToken(PatternGrammar.QUOTED_STRING, 0); }
		public List<EntityContext> entity() {
			return getRuleContexts(EntityContext.class);
		}
		public EntityContext entity(int i) {
			return getRuleContext(EntityContext.class,i);
		}
		public LiteralNodeContext(PatternNodeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PatternGrammarListener ) ((PatternGrammarListener)listener).enterLiteralNode(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PatternGrammarListener ) ((PatternGrammarListener)listener).exitLiteralNode(this);
		}
	}
	public static class WildcardNodeContext extends PatternNodeContext {
		public TerminalNode WILDCARD() { return getToken(PatternGrammar.WILDCARD, 0); }
		public List<EntityContext> entity() {
			return getRuleContexts(EntityContext.class);
		}
		public EntityContext entity(int i) {
			return getRuleContext(EntityContext.class,i);
		}
		public WildcardNodeContext(PatternNodeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PatternGrammarListener ) ((PatternGrammarListener)listener).enterWildcardNode(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PatternGrammarListener ) ((PatternGrammarListener)listener).exitWildcardNode(this);
		}
	}
	public static class AnyNodeContext extends PatternNodeContext {
		public PatternNodeContext patternNode() {
			return getRuleContext(PatternNodeContext.class,0);
		}
		public TerminalNode ANY() { return getToken(PatternGrammar.ANY, 0); }
		public List<EntityContext> entity() {
			return getRuleContexts(EntityContext.class);
		}
		public EntityContext entity(int i) {
			return getRuleContext(EntityContext.class,i);
		}
		public AnyNodeContext(PatternNodeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PatternGrammarListener ) ((PatternGrammarListener)listener).enterAnyNode(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PatternGrammarListener ) ((PatternGrammarListener)listener).exitAnyNode(this);
		}
	}
	public static class ConjunctionListNodeContext extends PatternNodeContext {
		public List<PatternNodeContext> patternNode() {
			return getRuleContexts(PatternNodeContext.class);
		}
		public PatternNodeContext patternNode(int i) {
			return getRuleContext(PatternNodeContext.class,i);
		}
		public TerminalNode CONJ_MANY() { return getToken(PatternGrammar.CONJ_MANY, 0); }
		public TerminalNode GROUP_END() { return getToken(PatternGrammar.GROUP_END, 0); }
		public List<EntityContext> entity() {
			return getRuleContexts(EntityContext.class);
		}
		public EntityContext entity(int i) {
			return getRuleContext(EntityContext.class,i);
		}
		public ConjunctionListNodeContext(PatternNodeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PatternGrammarListener ) ((PatternGrammarListener)listener).enterConjunctionListNode(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PatternGrammarListener ) ((PatternGrammarListener)listener).exitConjunctionListNode(this);
		}
	}
	public static class OptionalNodeContext extends PatternNodeContext {
		public PatternNodeContext patternNode() {
			return getRuleContext(PatternNodeContext.class,0);
		}
		public TerminalNode OPTIONAL() { return getToken(PatternGrammar.OPTIONAL, 0); }
		public List<EntityContext> entity() {
			return getRuleContexts(EntityContext.class);
		}
		public EntityContext entity(int i) {
			return getRuleContext(EntityContext.class,i);
		}
		public OptionalNodeContext(PatternNodeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PatternGrammarListener ) ((PatternGrammarListener)listener).enterOptionalNode(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PatternGrammarListener ) ((PatternGrammarListener)listener).exitOptionalNode(this);
		}
	}
	public static class GroupNodeContext extends PatternNodeContext {
		public TerminalNode GROUP_BEGIN() { return getToken(PatternGrammar.GROUP_BEGIN, 0); }
		public PatternNodeContext patternNode() {
			return getRuleContext(PatternNodeContext.class,0);
		}
		public TerminalNode GROUP_END() { return getToken(PatternGrammar.GROUP_END, 0); }
		public List<TerminalNode> WS() { return getTokens(PatternGrammar.WS); }
		public TerminalNode WS(int i) {
			return getToken(PatternGrammar.WS, i);
		}
		public List<EntityContext> entity() {
			return getRuleContexts(EntityContext.class);
		}
		public EntityContext entity(int i) {
			return getRuleContext(EntityContext.class,i);
		}
		public GroupNodeContext(PatternNodeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PatternGrammarListener ) ((PatternGrammarListener)listener).enterGroupNode(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PatternGrammarListener ) ((PatternGrammarListener)listener).exitGroupNode(this);
		}
	}
	public static class VariableNodeContext extends PatternNodeContext {
		public Token variable;
		public TerminalNode VARIABLE() { return getToken(PatternGrammar.VARIABLE, 0); }
		public List<EntityContext> entity() {
			return getRuleContexts(EntityContext.class);
		}
		public EntityContext entity(int i) {
			return getRuleContext(EntityContext.class,i);
		}
		public VariableNodeContext(PatternNodeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PatternGrammarListener ) ((PatternGrammarListener)listener).enterVariableNode(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PatternGrammarListener ) ((PatternGrammarListener)listener).exitVariableNode(this);
		}
	}
	public static class ManyNodeContext extends PatternNodeContext {
		public PatternNodeContext patternNode() {
			return getRuleContext(PatternNodeContext.class,0);
		}
		public TerminalNode MANY() { return getToken(PatternGrammar.MANY, 0); }
		public List<EntityContext> entity() {
			return getRuleContexts(EntityContext.class);
		}
		public EntityContext entity(int i) {
			return getRuleContext(EntityContext.class,i);
		}
		public ManyNodeContext(PatternNodeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PatternGrammarListener ) ((PatternGrammarListener)listener).enterManyNode(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PatternGrammarListener ) ((PatternGrammarListener)listener).exitManyNode(this);
		}
	}

	public final PatternNodeContext patternNode() throws RecognitionException {
		return patternNode(0);
	}

	private PatternNodeContext patternNode(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		PatternNodeContext _localctx = new PatternNodeContext(_ctx, _parentState);
		PatternNodeContext _prevctx = _localctx;
		int _startState = 2;
		enterRecursionRule(_localctx, 2, RULE_patternNode, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(78);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case GROUP_BEGIN:
				{
				_localctx = new GroupNodeContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(12);
				match(GROUP_BEGIN);
				setState(16);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==WS) {
					{
					{
					setState(13);
					match(WS);
					}
					}
					setState(18);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(19);
				patternNode(0);
				setState(23);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==WS) {
					{
					{
					setState(20);
					match(WS);
					}
					}
					setState(25);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(26);
				match(GROUP_END);
				setState(30);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(27);
						entity();
						}
						} 
					}
					setState(32);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
				}
				}
				break;
			case QUOTED_STRING:
				{
				_localctx = new LiteralNodeContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(33);
				((LiteralNodeContext)_localctx).value = match(QUOTED_STRING);
				setState(37);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(34);
						entity();
						}
						} 
					}
					setState(39);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
				}
				}
				break;
			case REFERENCE:
				{
				_localctx = new ReferenceNodeContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(40);
				((ReferenceNodeContext)_localctx).referenceId = match(REFERENCE);
				setState(42);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
				case 1:
					{
					setState(41);
					parameterNodeList();
					}
					break;
				}
				setState(47);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(44);
						entity();
						}
						} 
					}
					setState(49);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
				}
				}
				break;
			case REGEX_PATTERN:
				{
				_localctx = new RegexNodeContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(50);
				((RegexNodeContext)_localctx).patternValue = match(REGEX_PATTERN);
				setState(54);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,6,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(51);
						entity();
						}
						} 
					}
					setState(56);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,6,_ctx);
				}
				}
				break;
			case WILDCARD:
				{
				_localctx = new WildcardNodeContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(57);
				match(WILDCARD);
				setState(61);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,7,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(58);
						entity();
						}
						} 
					}
					setState(63);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,7,_ctx);
				}
				}
				break;
			case VARIABLE:
				{
				_localctx = new VariableNodeContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(64);
				((VariableNodeContext)_localctx).variable = match(VARIABLE);
				setState(68);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(65);
						entity();
						}
						} 
					}
					setState(70);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
				}
				}
				break;
			case WORD:
				{
				_localctx = new WordNodeContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(71);
				((WordNodeContext)_localctx).value = match(WORD);
				setState(75);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,9,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(72);
						entity();
						}
						} 
					}
					setState(77);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,9,_ctx);
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(138);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,19,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(136);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,18,_ctx) ) {
					case 1:
						{
						_localctx = new SequenceNodeContext(new PatternNodeContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_patternNode);
						setState(80);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(82); 
						_errHandler.sync(this);
						_la = _input.LA(1);
						do {
							{
							{
							setState(81);
							match(WS);
							}
							}
							setState(84); 
							_errHandler.sync(this);
							_la = _input.LA(1);
						} while ( _la==WS );
						setState(86);
						patternNode(3);
						}
						break;
					case 2:
						{
						_localctx = new AlternativesNodeContext(new PatternNodeContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_patternNode);
						setState(87);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(91);
						_errHandler.sync(this);
						_la = _input.LA(1);
						while (_la==WS) {
							{
							{
							setState(88);
							match(WS);
							}
							}
							setState(93);
							_errHandler.sync(this);
							_la = _input.LA(1);
						}
						setState(94);
						match(ALTERNATIVE);
						setState(98);
						_errHandler.sync(this);
						_la = _input.LA(1);
						while (_la==WS) {
							{
							{
							setState(95);
							match(WS);
							}
							}
							setState(100);
							_errHandler.sync(this);
							_la = _input.LA(1);
						}
						setState(101);
						patternNode(2);
						}
						break;
					case 3:
						{
						_localctx = new OptionalNodeContext(new PatternNodeContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_patternNode);
						setState(102);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(103);
						match(OPTIONAL);
						setState(107);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
						while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
							if ( _alt==1 ) {
								{
								{
								setState(104);
								entity();
								}
								} 
							}
							setState(109);
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
						}
						}
						break;
					case 4:
						{
						_localctx = new ConjunctionListNodeContext(new PatternNodeContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_patternNode);
						setState(110);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(111);
						match(CONJ_MANY);
						setState(112);
						patternNode(0);
						setState(113);
						match(GROUP_END);
						setState(117);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,15,_ctx);
						while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
							if ( _alt==1 ) {
								{
								{
								setState(114);
								entity();
								}
								} 
							}
							setState(119);
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,15,_ctx);
						}
						}
						break;
					case 5:
						{
						_localctx = new ManyNodeContext(new PatternNodeContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_patternNode);
						setState(120);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(121);
						match(MANY);
						setState(125);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,16,_ctx);
						while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
							if ( _alt==1 ) {
								{
								{
								setState(122);
								entity();
								}
								} 
							}
							setState(127);
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,16,_ctx);
						}
						}
						break;
					case 6:
						{
						_localctx = new AnyNodeContext(new PatternNodeContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_patternNode);
						setState(128);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(129);
						match(ANY);
						setState(133);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,17,_ctx);
						while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
							if ( _alt==1 ) {
								{
								{
								setState(130);
								entity();
								}
								} 
							}
							setState(135);
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,17,_ctx);
						}
						}
						break;
					}
					} 
				}
				setState(140);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,19,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class EntityContext extends ParserRuleContext {
		public Token name;
		public Token fixedValue;
		public TerminalNode ENTITY_BEGIN() { return getToken(PatternGrammar.ENTITY_BEGIN, 0); }
		public TerminalNode ENTITY_END() { return getToken(PatternGrammar.ENTITY_END, 0); }
		public List<TerminalNode> ENTITY_NAME() { return getTokens(PatternGrammar.ENTITY_NAME); }
		public TerminalNode ENTITY_NAME(int i) {
			return getToken(PatternGrammar.ENTITY_NAME, i);
		}
		public TerminalNode ENTITY_ASSIGN() { return getToken(PatternGrammar.ENTITY_ASSIGN, 0); }
		public TerminalNode ENTITY_VALUE() { return getToken(PatternGrammar.ENTITY_VALUE, 0); }
		public EntityContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_entity; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PatternGrammarListener ) ((PatternGrammarListener)listener).enterEntity(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PatternGrammarListener ) ((PatternGrammarListener)listener).exitEntity(this);
		}
	}

	public final EntityContext entity() throws RecognitionException {
		EntityContext _localctx = new EntityContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_entity);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(141);
			match(ENTITY_BEGIN);
			setState(142);
			((EntityContext)_localctx).name = match(ENTITY_NAME);
			setState(145);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ENTITY_ASSIGN) {
				{
				setState(143);
				match(ENTITY_ASSIGN);
				setState(144);
				((EntityContext)_localctx).fixedValue = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==ENTITY_NAME || _la==ENTITY_VALUE) ) {
					((EntityContext)_localctx).fixedValue = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
			}

			setState(147);
			match(ENTITY_END);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ParameterNodeListContext extends ParserRuleContext {
		public TerminalNode GROUP_BEGIN() { return getToken(PatternGrammar.GROUP_BEGIN, 0); }
		public TerminalNode GROUP_END() { return getToken(PatternGrammar.GROUP_END, 0); }
		public List<TerminalNode> WS() { return getTokens(PatternGrammar.WS); }
		public TerminalNode WS(int i) {
			return getToken(PatternGrammar.WS, i);
		}
		public List<PatternNodeContext> patternNode() {
			return getRuleContexts(PatternNodeContext.class);
		}
		public PatternNodeContext patternNode(int i) {
			return getRuleContext(PatternNodeContext.class,i);
		}
		public List<TerminalNode> LIST_SEPARATOR() { return getTokens(PatternGrammar.LIST_SEPARATOR); }
		public TerminalNode LIST_SEPARATOR(int i) {
			return getToken(PatternGrammar.LIST_SEPARATOR, i);
		}
		public ParameterNodeListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parameterNodeList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PatternGrammarListener ) ((PatternGrammarListener)listener).enterParameterNodeList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PatternGrammarListener ) ((PatternGrammarListener)listener).exitParameterNodeList(this);
		}
	}

	public final ParameterNodeListContext parameterNodeList() throws RecognitionException {
		ParameterNodeListContext _localctx = new ParameterNodeListContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_parameterNodeList);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(149);
			match(GROUP_BEGIN);
			setState(153);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==WS) {
				{
				{
				setState(150);
				match(WS);
				}
				}
				setState(155);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(188);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << VARIABLE) | (1L << GROUP_BEGIN) | (1L << WILDCARD) | (1L << REFERENCE) | (1L << QUOTED_STRING) | (1L << REGEX_PATTERN) | (1L << WORD))) != 0)) {
				{
				setState(156);
				patternNode(0);
				setState(160);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,22,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(157);
						match(WS);
						}
						} 
					}
					setState(162);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,22,_ctx);
				}
				setState(185);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==LIST_SEPARATOR || _la==WS) {
					{
					{
					setState(166);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==WS) {
						{
						{
						setState(163);
						match(WS);
						}
						}
						setState(168);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(169);
					match(LIST_SEPARATOR);
					setState(173);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==WS) {
						{
						{
						setState(170);
						match(WS);
						}
						}
						setState(175);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(176);
					patternNode(0);
					setState(180);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,25,_ctx);
					while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
						if ( _alt==1 ) {
							{
							{
							setState(177);
							match(WS);
							}
							} 
						}
						setState(182);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,25,_ctx);
					}
					}
					}
					setState(187);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(190);
			match(GROUP_END);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 1:
			return patternNode_sempred((PatternNodeContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean patternNode_sempred(PatternNodeContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 2);
		case 1:
			return precpred(_ctx, 1);
		case 2:
			return precpred(_ctx, 7);
		case 3:
			return precpred(_ctx, 6);
		case 4:
			return precpred(_ctx, 5);
		case 5:
			return precpred(_ctx, 4);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\27\u00c3\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\3\2\3\2\3\2\3\3\3\3\3\3\7\3\21\n\3\f\3\16\3\24"+
		"\13\3\3\3\3\3\7\3\30\n\3\f\3\16\3\33\13\3\3\3\3\3\7\3\37\n\3\f\3\16\3"+
		"\"\13\3\3\3\3\3\7\3&\n\3\f\3\16\3)\13\3\3\3\3\3\5\3-\n\3\3\3\7\3\60\n"+
		"\3\f\3\16\3\63\13\3\3\3\3\3\7\3\67\n\3\f\3\16\3:\13\3\3\3\3\3\7\3>\n\3"+
		"\f\3\16\3A\13\3\3\3\3\3\7\3E\n\3\f\3\16\3H\13\3\3\3\3\3\7\3L\n\3\f\3\16"+
		"\3O\13\3\5\3Q\n\3\3\3\3\3\6\3U\n\3\r\3\16\3V\3\3\3\3\3\3\7\3\\\n\3\f\3"+
		"\16\3_\13\3\3\3\3\3\7\3c\n\3\f\3\16\3f\13\3\3\3\3\3\3\3\3\3\7\3l\n\3\f"+
		"\3\16\3o\13\3\3\3\3\3\3\3\3\3\3\3\7\3v\n\3\f\3\16\3y\13\3\3\3\3\3\3\3"+
		"\7\3~\n\3\f\3\16\3\u0081\13\3\3\3\3\3\3\3\7\3\u0086\n\3\f\3\16\3\u0089"+
		"\13\3\7\3\u008b\n\3\f\3\16\3\u008e\13\3\3\4\3\4\3\4\3\4\5\4\u0094\n\4"+
		"\3\4\3\4\3\5\3\5\7\5\u009a\n\5\f\5\16\5\u009d\13\5\3\5\3\5\7\5\u00a1\n"+
		"\5\f\5\16\5\u00a4\13\5\3\5\7\5\u00a7\n\5\f\5\16\5\u00aa\13\5\3\5\3\5\7"+
		"\5\u00ae\n\5\f\5\16\5\u00b1\13\5\3\5\3\5\7\5\u00b5\n\5\f\5\16\5\u00b8"+
		"\13\5\7\5\u00ba\n\5\f\5\16\5\u00bd\13\5\5\5\u00bf\n\5\3\5\3\5\3\5\2\3"+
		"\4\6\2\4\6\b\2\3\4\2\24\24\26\26\2\u00e3\2\n\3\2\2\2\4P\3\2\2\2\6\u008f"+
		"\3\2\2\2\b\u0097\3\2\2\2\n\13\5\4\3\2\13\f\7\2\2\3\f\3\3\2\2\2\r\16\b"+
		"\3\1\2\16\22\7\5\2\2\17\21\7\23\2\2\20\17\3\2\2\2\21\24\3\2\2\2\22\20"+
		"\3\2\2\2\22\23\3\2\2\2\23\25\3\2\2\2\24\22\3\2\2\2\25\31\5\4\3\2\26\30"+
		"\7\23\2\2\27\26\3\2\2\2\30\33\3\2\2\2\31\27\3\2\2\2\31\32\3\2\2\2\32\34"+
		"\3\2\2\2\33\31\3\2\2\2\34 \7\6\2\2\35\37\5\6\4\2\36\35\3\2\2\2\37\"\3"+
		"\2\2\2 \36\3\2\2\2 !\3\2\2\2!Q\3\2\2\2\" \3\2\2\2#\'\7\17\2\2$&\5\6\4"+
		"\2%$\3\2\2\2&)\3\2\2\2\'%\3\2\2\2\'(\3\2\2\2(Q\3\2\2\2)\'\3\2\2\2*,\7"+
		"\16\2\2+-\5\b\5\2,+\3\2\2\2,-\3\2\2\2-\61\3\2\2\2.\60\5\6\4\2/.\3\2\2"+
		"\2\60\63\3\2\2\2\61/\3\2\2\2\61\62\3\2\2\2\62Q\3\2\2\2\63\61\3\2\2\2\64"+
		"8\7\20\2\2\65\67\5\6\4\2\66\65\3\2\2\2\67:\3\2\2\28\66\3\2\2\289\3\2\2"+
		"\29Q\3\2\2\2:8\3\2\2\2;?\7\b\2\2<>\5\6\4\2=<\3\2\2\2>A\3\2\2\2?=\3\2\2"+
		"\2?@\3\2\2\2@Q\3\2\2\2A?\3\2\2\2BF\7\3\2\2CE\5\6\4\2DC\3\2\2\2EH\3\2\2"+
		"\2FD\3\2\2\2FG\3\2\2\2GQ\3\2\2\2HF\3\2\2\2IM\7\21\2\2JL\5\6\4\2KJ\3\2"+
		"\2\2LO\3\2\2\2MK\3\2\2\2MN\3\2\2\2NQ\3\2\2\2OM\3\2\2\2P\r\3\2\2\2P#\3"+
		"\2\2\2P*\3\2\2\2P\64\3\2\2\2P;\3\2\2\2PB\3\2\2\2PI\3\2\2\2Q\u008c\3\2"+
		"\2\2RT\f\4\2\2SU\7\23\2\2TS\3\2\2\2UV\3\2\2\2VT\3\2\2\2VW\3\2\2\2WX\3"+
		"\2\2\2X\u008b\5\4\3\5Y]\f\3\2\2Z\\\7\23\2\2[Z\3\2\2\2\\_\3\2\2\2][\3\2"+
		"\2\2]^\3\2\2\2^`\3\2\2\2_]\3\2\2\2`d\7\4\2\2ac\7\23\2\2ba\3\2\2\2cf\3"+
		"\2\2\2db\3\2\2\2de\3\2\2\2eg\3\2\2\2fd\3\2\2\2g\u008b\5\4\3\4hi\f\t\2"+
		"\2im\7\t\2\2jl\5\6\4\2kj\3\2\2\2lo\3\2\2\2mk\3\2\2\2mn\3\2\2\2n\u008b"+
		"\3\2\2\2om\3\2\2\2pq\f\b\2\2qr\7\n\2\2rs\5\4\3\2sw\7\6\2\2tv\5\6\4\2u"+
		"t\3\2\2\2vy\3\2\2\2wu\3\2\2\2wx\3\2\2\2x\u008b\3\2\2\2yw\3\2\2\2z{\f\7"+
		"\2\2{\177\7\13\2\2|~\5\6\4\2}|\3\2\2\2~\u0081\3\2\2\2\177}\3\2\2\2\177"+
		"\u0080\3\2\2\2\u0080\u008b\3\2\2\2\u0081\177\3\2\2\2\u0082\u0083\f\6\2"+
		"\2\u0083\u0087\7\f\2\2\u0084\u0086\5\6\4\2\u0085\u0084\3\2\2\2\u0086\u0089"+
		"\3\2\2\2\u0087\u0085\3\2\2\2\u0087\u0088\3\2\2\2\u0088\u008b\3\2\2\2\u0089"+
		"\u0087\3\2\2\2\u008aR\3\2\2\2\u008aY\3\2\2\2\u008ah\3\2\2\2\u008ap\3\2"+
		"\2\2\u008az\3\2\2\2\u008a\u0082\3\2\2\2\u008b\u008e\3\2\2\2\u008c\u008a"+
		"\3\2\2\2\u008c\u008d\3\2\2\2\u008d\5\3\2\2\2\u008e\u008c\3\2\2\2\u008f"+
		"\u0090\7\7\2\2\u0090\u0093\7\24\2\2\u0091\u0092\7\25\2\2\u0092\u0094\t"+
		"\2\2\2\u0093\u0091\3\2\2\2\u0093\u0094\3\2\2\2\u0094\u0095\3\2\2\2\u0095"+
		"\u0096\7\27\2\2\u0096\7\3\2\2\2\u0097\u009b\7\5\2\2\u0098\u009a\7\23\2"+
		"\2\u0099\u0098\3\2\2\2\u009a\u009d\3\2\2\2\u009b\u0099\3\2\2\2\u009b\u009c"+
		"\3\2\2\2\u009c\u00be\3\2\2\2\u009d\u009b\3\2\2\2\u009e\u00a2\5\4\3\2\u009f"+
		"\u00a1\7\23\2\2\u00a0\u009f\3\2\2\2\u00a1\u00a4\3\2\2\2\u00a2\u00a0\3"+
		"\2\2\2\u00a2\u00a3\3\2\2\2\u00a3\u00bb\3\2\2\2\u00a4\u00a2\3\2\2\2\u00a5"+
		"\u00a7\7\23\2\2\u00a6\u00a5\3\2\2\2\u00a7\u00aa\3\2\2\2\u00a8\u00a6\3"+
		"\2\2\2\u00a8\u00a9\3\2\2\2\u00a9\u00ab\3\2\2\2\u00aa\u00a8\3\2\2\2\u00ab"+
		"\u00af\7\r\2\2\u00ac\u00ae\7\23\2\2\u00ad\u00ac\3\2\2\2\u00ae\u00b1\3"+
		"\2\2\2\u00af\u00ad\3\2\2\2\u00af\u00b0\3\2\2\2\u00b0\u00b2\3\2\2\2\u00b1"+
		"\u00af\3\2\2\2\u00b2\u00b6\5\4\3\2\u00b3\u00b5\7\23\2\2\u00b4\u00b3\3"+
		"\2\2\2\u00b5\u00b8\3\2\2\2\u00b6\u00b4\3\2\2\2\u00b6\u00b7\3\2\2\2\u00b7"+
		"\u00ba\3\2\2\2\u00b8\u00b6\3\2\2\2\u00b9\u00a8\3\2\2\2\u00ba\u00bd\3\2"+
		"\2\2\u00bb\u00b9\3\2\2\2\u00bb\u00bc\3\2\2\2\u00bc\u00bf\3\2\2\2\u00bd"+
		"\u00bb\3\2\2\2\u00be\u009e\3\2\2\2\u00be\u00bf\3\2\2\2\u00bf\u00c0\3\2"+
		"\2\2\u00c0\u00c1\7\6\2\2\u00c1\t\3\2\2\2\36\22\31 \',\618?FMPV]dmw\177"+
		"\u0087\u008a\u008c\u0093\u009b\u00a2\u00a8\u00af\u00b6\u00bb\u00be";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}