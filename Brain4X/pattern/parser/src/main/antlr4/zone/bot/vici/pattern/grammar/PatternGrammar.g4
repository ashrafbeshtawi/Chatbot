parser grammar PatternGrammar;

options { tokenVocab=PatternLexer; }

pattern:
    patternNode
    EOF;

patternNode:
	GROUP_BEGIN WS* patternNode WS* GROUP_END entity*		# groupNode
	| value=QUOTED_STRING entity*							# literalNode
	| referenceId=REFERENCE parameterNodeList? entity*      # referenceNode
	| patternValue=REGEX_PATTERN entity*					# regexNode
	| WILDCARD entity*										# wildcardNode
	| variable=VARIABLE entity*										# variableNode
	| patternNode OPTIONAL entity*                      	# optionalNode
	| patternNode CONJ_MANY patternNode GROUP_END entity*   # conjunctionListNode
	| patternNode MANY entity*                          	# manyNode
	| patternNode ANY entity*                           	# anyNode
	| value=WORD entity*                                	# wordNode
	| patternNode WS+ patternNode							# sequenceNode
	| patternNode WS* ALTERNATIVE WS* patternNode			# alternativesNode
;

entity: ENTITY_BEGIN name=ENTITY_NAME (ENTITY_ASSIGN fixedValue=(ENTITY_NAME|ENTITY_VALUE))? ENTITY_END;

parameterNodeList: GROUP_BEGIN WS* (patternNode WS* (WS* LIST_SEPARATOR WS* patternNode WS*)*)? GROUP_END;