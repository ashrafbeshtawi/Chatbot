lexer grammar PatternLexer;

VARIABLE: '$' [0-9];
ALTERNATIVE: '|';
GROUP_BEGIN: '(';
GROUP_END: ')';
ENTITY_BEGIN: '{' -> pushMode(ENTITY_MODE);
WILDCARD: '_';
OPTIONAL: '?';
CONJ_MANY: '+(';
MANY: '+';
ANY: '*';
LIST_SEPARATOR: ',';

fragment FRAG_NL: '\r'? '\n';
fragment FRAG_WS: (' ' | '\t' | '\r' | '\\u000B' | '\\u000C' | '\\u0085' | '\\u00A0' | '\\u1680' | '\\u180E' | '\\u2000' | '\\u2001' | '\\u2002' | '\\u2003' | '\\u2004' | '\\u2005' | '\\u2006' | '\\u2007' | '\\u2008' | '\\u2009' | '\\u200A' | '\\u2028' | '\\u2029' | '\\u202F' | '\\u205F' | '\\u3000');
fragment IDENTIFIER: ('A'..'Z' | 'a'..'z') ('A'..'Z' | 'a'..'z' | '0'..'9' | '_' )+;
REFERENCE: '~' IDENTIFIER;
fragment SINGLE_QUOTED_STRING: ('\'' ( '\\\'' | ~('\''))* '\'');
fragment DOUBLE_QUOTED_STRING: ('"' ( '\\"' | ~('"'))* '"');
QUOTED_STRING: SINGLE_QUOTED_STRING | DOUBLE_QUOTED_STRING;

REGEX_PATTERN: '/' ( '\\/' | ~('/'))+ '/';
WORD: ~(' ' | '\t' | '\r' | '\n' | '.' | ',' | ':' | '?' | '*' | '+' | '|' | '(' | ')' | '{' | '}' | '/' | '_' | '"' | '\'' | '=')+;

NL:  FRAG_NL;
WS: FRAG_WS;

mode ENTITY_MODE;
ENTITY_NAME: IDENTIFIER;
ENTITY_ASSIGN: '=';
ENTITY_VALUE: WORD | QUOTED_STRING;
ENTITY_END: '}' -> popMode;
