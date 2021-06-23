// Generated from zone\bot\vici\pattern\grammar\PatternLexer.g4 by ANTLR 4.8
package zone.bot.vici.pattern.grammar;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class PatternLexer extends Lexer {
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
		ENTITY_MODE=1;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE", "ENTITY_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"VARIABLE", "ALTERNATIVE", "GROUP_BEGIN", "GROUP_END", "ENTITY_BEGIN", 
			"WILDCARD", "OPTIONAL", "CONJ_MANY", "MANY", "ANY", "LIST_SEPARATOR", 
			"FRAG_NL", "FRAG_WS", "IDENTIFIER", "REFERENCE", "SINGLE_QUOTED_STRING", 
			"DOUBLE_QUOTED_STRING", "QUOTED_STRING", "REGEX_PATTERN", "WORD", "NL", 
			"WS", "ENTITY_NAME", "ENTITY_ASSIGN", "ENTITY_VALUE", "ENTITY_END"
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


	public PatternLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "PatternLexer.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\27\u0120\b\1\b\1"+
		"\4\2\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t"+
		"\n\4\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4"+
		"\22\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4"+
		"\31\t\31\4\32\t\32\4\33\t\33\3\2\3\2\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3\6\3"+
		"\6\3\6\3\6\3\7\3\7\3\b\3\b\3\t\3\t\3\t\3\n\3\n\3\13\3\13\3\f\3\f\3\r\5"+
		"\rT\n\r\3\r\3\r\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16"+
		"\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16"+
		"\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16"+
		"\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16"+
		"\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16"+
		"\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16"+
		"\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16"+
		"\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16"+
		"\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16"+
		"\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\5\16\u00dd\n\16\3\17"+
		"\3\17\6\17\u00e1\n\17\r\17\16\17\u00e2\3\20\3\20\3\20\3\21\3\21\3\21\3"+
		"\21\7\21\u00ec\n\21\f\21\16\21\u00ef\13\21\3\21\3\21\3\22\3\22\3\22\3"+
		"\22\7\22\u00f7\n\22\f\22\16\22\u00fa\13\22\3\22\3\22\3\23\3\23\5\23\u0100"+
		"\n\23\3\24\3\24\3\24\3\24\6\24\u0106\n\24\r\24\16\24\u0107\3\24\3\24\3"+
		"\25\6\25\u010d\n\25\r\25\16\25\u010e\3\26\3\26\3\27\3\27\3\30\3\30\3\31"+
		"\3\31\3\32\3\32\5\32\u011b\n\32\3\33\3\33\3\33\3\33\2\2\34\4\3\6\4\b\5"+
		"\n\6\f\7\16\b\20\t\22\n\24\13\26\f\30\r\32\2\34\2\36\2 \16\"\2$\2&\17"+
		"(\20*\21,\22.\23\60\24\62\25\64\26\66\27\4\2\3\n\3\2\62;\5\2\13\13\17"+
		"\17\"\"\4\2C\\c|\6\2\62;C\\aac|\3\2))\3\2$$\3\2\61\61\r\2\13\f\17\17\""+
		"\"$$).\60\61<<??AAaa}\177\2\u013a\2\4\3\2\2\2\2\6\3\2\2\2\2\b\3\2\2\2"+
		"\2\n\3\2\2\2\2\f\3\2\2\2\2\16\3\2\2\2\2\20\3\2\2\2\2\22\3\2\2\2\2\24\3"+
		"\2\2\2\2\26\3\2\2\2\2\30\3\2\2\2\2 \3\2\2\2\2&\3\2\2\2\2(\3\2\2\2\2*\3"+
		"\2\2\2\2,\3\2\2\2\2.\3\2\2\2\3\60\3\2\2\2\3\62\3\2\2\2\3\64\3\2\2\2\3"+
		"\66\3\2\2\2\48\3\2\2\2\6;\3\2\2\2\b=\3\2\2\2\n?\3\2\2\2\fA\3\2\2\2\16"+
		"E\3\2\2\2\20G\3\2\2\2\22I\3\2\2\2\24L\3\2\2\2\26N\3\2\2\2\30P\3\2\2\2"+
		"\32S\3\2\2\2\34\u00dc\3\2\2\2\36\u00de\3\2\2\2 \u00e4\3\2\2\2\"\u00e7"+
		"\3\2\2\2$\u00f2\3\2\2\2&\u00ff\3\2\2\2(\u0101\3\2\2\2*\u010c\3\2\2\2,"+
		"\u0110\3\2\2\2.\u0112\3\2\2\2\60\u0114\3\2\2\2\62\u0116\3\2\2\2\64\u011a"+
		"\3\2\2\2\66\u011c\3\2\2\289\7&\2\29:\t\2\2\2:\5\3\2\2\2;<\7~\2\2<\7\3"+
		"\2\2\2=>\7*\2\2>\t\3\2\2\2?@\7+\2\2@\13\3\2\2\2AB\7}\2\2BC\3\2\2\2CD\b"+
		"\6\2\2D\r\3\2\2\2EF\7a\2\2F\17\3\2\2\2GH\7A\2\2H\21\3\2\2\2IJ\7-\2\2J"+
		"K\7*\2\2K\23\3\2\2\2LM\7-\2\2M\25\3\2\2\2NO\7,\2\2O\27\3\2\2\2PQ\7.\2"+
		"\2Q\31\3\2\2\2RT\7\17\2\2SR\3\2\2\2ST\3\2\2\2TU\3\2\2\2UV\7\f\2\2V\33"+
		"\3\2\2\2W\u00dd\t\3\2\2XY\7^\2\2YZ\7w\2\2Z[\7\62\2\2[\\\7\62\2\2\\]\7"+
		"\62\2\2]\u00dd\7D\2\2^_\7^\2\2_`\7w\2\2`a\7\62\2\2ab\7\62\2\2bc\7\62\2"+
		"\2c\u00dd\7E\2\2de\7^\2\2ef\7w\2\2fg\7\62\2\2gh\7\62\2\2hi\7:\2\2i\u00dd"+
		"\7\67\2\2jk\7^\2\2kl\7w\2\2lm\7\62\2\2mn\7\62\2\2no\7C\2\2o\u00dd\7\62"+
		"\2\2pq\7^\2\2qr\7w\2\2rs\7\63\2\2st\78\2\2tu\7:\2\2u\u00dd\7\62\2\2vw"+
		"\7^\2\2wx\7w\2\2xy\7\63\2\2yz\7:\2\2z{\7\62\2\2{\u00dd\7G\2\2|}\7^\2\2"+
		"}~\7w\2\2~\177\7\64\2\2\177\u0080\7\62\2\2\u0080\u0081\7\62\2\2\u0081"+
		"\u00dd\7\62\2\2\u0082\u0083\7^\2\2\u0083\u0084\7w\2\2\u0084\u0085\7\64"+
		"\2\2\u0085\u0086\7\62\2\2\u0086\u0087\7\62\2\2\u0087\u00dd\7\63\2\2\u0088"+
		"\u0089\7^\2\2\u0089\u008a\7w\2\2\u008a\u008b\7\64\2\2\u008b\u008c\7\62"+
		"\2\2\u008c\u008d\7\62\2\2\u008d\u00dd\7\64\2\2\u008e\u008f\7^\2\2\u008f"+
		"\u0090\7w\2\2\u0090\u0091\7\64\2\2\u0091\u0092\7\62\2\2\u0092\u0093\7"+
		"\62\2\2\u0093\u00dd\7\65\2\2\u0094\u0095\7^\2\2\u0095\u0096\7w\2\2\u0096"+
		"\u0097\7\64\2\2\u0097\u0098\7\62\2\2\u0098\u0099\7\62\2\2\u0099\u00dd"+
		"\7\66\2\2\u009a\u009b\7^\2\2\u009b\u009c\7w\2\2\u009c\u009d\7\64\2\2\u009d"+
		"\u009e\7\62\2\2\u009e\u009f\7\62\2\2\u009f\u00dd\7\67\2\2\u00a0\u00a1"+
		"\7^\2\2\u00a1\u00a2\7w\2\2\u00a2\u00a3\7\64\2\2\u00a3\u00a4\7\62\2\2\u00a4"+
		"\u00a5\7\62\2\2\u00a5\u00dd\78\2\2\u00a6\u00a7\7^\2\2\u00a7\u00a8\7w\2"+
		"\2\u00a8\u00a9\7\64\2\2\u00a9\u00aa\7\62\2\2\u00aa\u00ab\7\62\2\2\u00ab"+
		"\u00dd\79\2\2\u00ac\u00ad\7^\2\2\u00ad\u00ae\7w\2\2\u00ae\u00af\7\64\2"+
		"\2\u00af\u00b0\7\62\2\2\u00b0\u00b1\7\62\2\2\u00b1\u00dd\7:\2\2\u00b2"+
		"\u00b3\7^\2\2\u00b3\u00b4\7w\2\2\u00b4\u00b5\7\64\2\2\u00b5\u00b6\7\62"+
		"\2\2\u00b6\u00b7\7\62\2\2\u00b7\u00dd\7;\2\2\u00b8\u00b9\7^\2\2\u00b9"+
		"\u00ba\7w\2\2\u00ba\u00bb\7\64\2\2\u00bb\u00bc\7\62\2\2\u00bc\u00bd\7"+
		"\62\2\2\u00bd\u00dd\7C\2\2\u00be\u00bf\7^\2\2\u00bf\u00c0\7w\2\2\u00c0"+
		"\u00c1\7\64\2\2\u00c1\u00c2\7\62\2\2\u00c2\u00c3\7\64\2\2\u00c3\u00dd"+
		"\7:\2\2\u00c4\u00c5\7^\2\2\u00c5\u00c6\7w\2\2\u00c6\u00c7\7\64\2\2\u00c7"+
		"\u00c8\7\62\2\2\u00c8\u00c9\7\64\2\2\u00c9\u00dd\7;\2\2\u00ca\u00cb\7"+
		"^\2\2\u00cb\u00cc\7w\2\2\u00cc\u00cd\7\64\2\2\u00cd\u00ce\7\62\2\2\u00ce"+
		"\u00cf\7\64\2\2\u00cf\u00dd\7H\2\2\u00d0\u00d1\7^\2\2\u00d1\u00d2\7w\2"+
		"\2\u00d2\u00d3\7\64\2\2\u00d3\u00d4\7\62\2\2\u00d4\u00d5\7\67\2\2\u00d5"+
		"\u00dd\7H\2\2\u00d6\u00d7\7^\2\2\u00d7\u00d8\7w\2\2\u00d8\u00d9\7\65\2"+
		"\2\u00d9\u00da\7\62\2\2\u00da\u00db\7\62\2\2\u00db\u00dd\7\62\2\2\u00dc"+
		"W\3\2\2\2\u00dcX\3\2\2\2\u00dc^\3\2\2\2\u00dcd\3\2\2\2\u00dcj\3\2\2\2"+
		"\u00dcp\3\2\2\2\u00dcv\3\2\2\2\u00dc|\3\2\2\2\u00dc\u0082\3\2\2\2\u00dc"+
		"\u0088\3\2\2\2\u00dc\u008e\3\2\2\2\u00dc\u0094\3\2\2\2\u00dc\u009a\3\2"+
		"\2\2\u00dc\u00a0\3\2\2\2\u00dc\u00a6\3\2\2\2\u00dc\u00ac\3\2\2\2\u00dc"+
		"\u00b2\3\2\2\2\u00dc\u00b8\3\2\2\2\u00dc\u00be\3\2\2\2\u00dc\u00c4\3\2"+
		"\2\2\u00dc\u00ca\3\2\2\2\u00dc\u00d0\3\2\2\2\u00dc\u00d6\3\2\2\2\u00dd"+
		"\35\3\2\2\2\u00de\u00e0\t\4\2\2\u00df\u00e1\t\5\2\2\u00e0\u00df\3\2\2"+
		"\2\u00e1\u00e2\3\2\2\2\u00e2\u00e0\3\2\2\2\u00e2\u00e3\3\2\2\2\u00e3\37"+
		"\3\2\2\2\u00e4\u00e5\7\u0080\2\2\u00e5\u00e6\5\36\17\2\u00e6!\3\2\2\2"+
		"\u00e7\u00ed\7)\2\2\u00e8\u00e9\7^\2\2\u00e9\u00ec\7)\2\2\u00ea\u00ec"+
		"\n\6\2\2\u00eb\u00e8\3\2\2\2\u00eb\u00ea\3\2\2\2\u00ec\u00ef\3\2\2\2\u00ed"+
		"\u00eb\3\2\2\2\u00ed\u00ee\3\2\2\2\u00ee\u00f0\3\2\2\2\u00ef\u00ed\3\2"+
		"\2\2\u00f0\u00f1\7)\2\2\u00f1#\3\2\2\2\u00f2\u00f8\7$\2\2\u00f3\u00f4"+
		"\7^\2\2\u00f4\u00f7\7$\2\2\u00f5\u00f7\n\7\2\2\u00f6\u00f3\3\2\2\2\u00f6"+
		"\u00f5\3\2\2\2\u00f7\u00fa\3\2\2\2\u00f8\u00f6\3\2\2\2\u00f8\u00f9\3\2"+
		"\2\2\u00f9\u00fb\3\2\2\2\u00fa\u00f8\3\2\2\2\u00fb\u00fc\7$\2\2\u00fc"+
		"%\3\2\2\2\u00fd\u0100\5\"\21\2\u00fe\u0100\5$\22\2\u00ff\u00fd\3\2\2\2"+
		"\u00ff\u00fe\3\2\2\2\u0100\'\3\2\2\2\u0101\u0105\7\61\2\2\u0102\u0103"+
		"\7^\2\2\u0103\u0106\7\61\2\2\u0104\u0106\n\b\2\2\u0105\u0102\3\2\2\2\u0105"+
		"\u0104\3\2\2\2\u0106\u0107\3\2\2\2\u0107\u0105\3\2\2\2\u0107\u0108\3\2"+
		"\2\2\u0108\u0109\3\2\2\2\u0109\u010a\7\61\2\2\u010a)\3\2\2\2\u010b\u010d"+
		"\n\t\2\2\u010c\u010b\3\2\2\2\u010d\u010e\3\2\2\2\u010e\u010c\3\2\2\2\u010e"+
		"\u010f\3\2\2\2\u010f+\3\2\2\2\u0110\u0111\5\32\r\2\u0111-\3\2\2\2\u0112"+
		"\u0113\5\34\16\2\u0113/\3\2\2\2\u0114\u0115\5\36\17\2\u0115\61\3\2\2\2"+
		"\u0116\u0117\7?\2\2\u0117\63\3\2\2\2\u0118\u011b\5*\25\2\u0119\u011b\5"+
		"&\23\2\u011a\u0118\3\2\2\2\u011a\u0119\3\2\2\2\u011b\65\3\2\2\2\u011c"+
		"\u011d\7\177\2\2\u011d\u011e\3\2\2\2\u011e\u011f\b\33\3\2\u011f\67\3\2"+
		"\2\2\20\2\3S\u00dc\u00e2\u00eb\u00ed\u00f6\u00f8\u00ff\u0105\u0107\u010e"+
		"\u011a\4\7\3\2\6\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}