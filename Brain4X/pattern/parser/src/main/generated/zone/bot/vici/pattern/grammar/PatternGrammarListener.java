// Generated from zone\bot\vici\pattern\grammar\PatternGrammar.g4 by ANTLR 4.8
package zone.bot.vici.pattern.grammar;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link PatternGrammar}.
 */
public interface PatternGrammarListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link PatternGrammar#pattern}.
	 * @param ctx the parse tree
	 */
	void enterPattern(PatternGrammar.PatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link PatternGrammar#pattern}.
	 * @param ctx the parse tree
	 */
	void exitPattern(PatternGrammar.PatternContext ctx);
	/**
	 * Enter a parse tree produced by the {@code regexNode}
	 * labeled alternative in {@link PatternGrammar#patternNode}.
	 * @param ctx the parse tree
	 */
	void enterRegexNode(PatternGrammar.RegexNodeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code regexNode}
	 * labeled alternative in {@link PatternGrammar#patternNode}.
	 * @param ctx the parse tree
	 */
	void exitRegexNode(PatternGrammar.RegexNodeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code wordNode}
	 * labeled alternative in {@link PatternGrammar#patternNode}.
	 * @param ctx the parse tree
	 */
	void enterWordNode(PatternGrammar.WordNodeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code wordNode}
	 * labeled alternative in {@link PatternGrammar#patternNode}.
	 * @param ctx the parse tree
	 */
	void exitWordNode(PatternGrammar.WordNodeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code sequenceNode}
	 * labeled alternative in {@link PatternGrammar#patternNode}.
	 * @param ctx the parse tree
	 */
	void enterSequenceNode(PatternGrammar.SequenceNodeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code sequenceNode}
	 * labeled alternative in {@link PatternGrammar#patternNode}.
	 * @param ctx the parse tree
	 */
	void exitSequenceNode(PatternGrammar.SequenceNodeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alternativesNode}
	 * labeled alternative in {@link PatternGrammar#patternNode}.
	 * @param ctx the parse tree
	 */
	void enterAlternativesNode(PatternGrammar.AlternativesNodeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alternativesNode}
	 * labeled alternative in {@link PatternGrammar#patternNode}.
	 * @param ctx the parse tree
	 */
	void exitAlternativesNode(PatternGrammar.AlternativesNodeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code referenceNode}
	 * labeled alternative in {@link PatternGrammar#patternNode}.
	 * @param ctx the parse tree
	 */
	void enterReferenceNode(PatternGrammar.ReferenceNodeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code referenceNode}
	 * labeled alternative in {@link PatternGrammar#patternNode}.
	 * @param ctx the parse tree
	 */
	void exitReferenceNode(PatternGrammar.ReferenceNodeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code literalNode}
	 * labeled alternative in {@link PatternGrammar#patternNode}.
	 * @param ctx the parse tree
	 */
	void enterLiteralNode(PatternGrammar.LiteralNodeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code literalNode}
	 * labeled alternative in {@link PatternGrammar#patternNode}.
	 * @param ctx the parse tree
	 */
	void exitLiteralNode(PatternGrammar.LiteralNodeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code wildcardNode}
	 * labeled alternative in {@link PatternGrammar#patternNode}.
	 * @param ctx the parse tree
	 */
	void enterWildcardNode(PatternGrammar.WildcardNodeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code wildcardNode}
	 * labeled alternative in {@link PatternGrammar#patternNode}.
	 * @param ctx the parse tree
	 */
	void exitWildcardNode(PatternGrammar.WildcardNodeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code anyNode}
	 * labeled alternative in {@link PatternGrammar#patternNode}.
	 * @param ctx the parse tree
	 */
	void enterAnyNode(PatternGrammar.AnyNodeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code anyNode}
	 * labeled alternative in {@link PatternGrammar#patternNode}.
	 * @param ctx the parse tree
	 */
	void exitAnyNode(PatternGrammar.AnyNodeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code conjunctionListNode}
	 * labeled alternative in {@link PatternGrammar#patternNode}.
	 * @param ctx the parse tree
	 */
	void enterConjunctionListNode(PatternGrammar.ConjunctionListNodeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code conjunctionListNode}
	 * labeled alternative in {@link PatternGrammar#patternNode}.
	 * @param ctx the parse tree
	 */
	void exitConjunctionListNode(PatternGrammar.ConjunctionListNodeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code optionalNode}
	 * labeled alternative in {@link PatternGrammar#patternNode}.
	 * @param ctx the parse tree
	 */
	void enterOptionalNode(PatternGrammar.OptionalNodeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code optionalNode}
	 * labeled alternative in {@link PatternGrammar#patternNode}.
	 * @param ctx the parse tree
	 */
	void exitOptionalNode(PatternGrammar.OptionalNodeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code groupNode}
	 * labeled alternative in {@link PatternGrammar#patternNode}.
	 * @param ctx the parse tree
	 */
	void enterGroupNode(PatternGrammar.GroupNodeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code groupNode}
	 * labeled alternative in {@link PatternGrammar#patternNode}.
	 * @param ctx the parse tree
	 */
	void exitGroupNode(PatternGrammar.GroupNodeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code variableNode}
	 * labeled alternative in {@link PatternGrammar#patternNode}.
	 * @param ctx the parse tree
	 */
	void enterVariableNode(PatternGrammar.VariableNodeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code variableNode}
	 * labeled alternative in {@link PatternGrammar#patternNode}.
	 * @param ctx the parse tree
	 */
	void exitVariableNode(PatternGrammar.VariableNodeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code manyNode}
	 * labeled alternative in {@link PatternGrammar#patternNode}.
	 * @param ctx the parse tree
	 */
	void enterManyNode(PatternGrammar.ManyNodeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code manyNode}
	 * labeled alternative in {@link PatternGrammar#patternNode}.
	 * @param ctx the parse tree
	 */
	void exitManyNode(PatternGrammar.ManyNodeContext ctx);
	/**
	 * Enter a parse tree produced by {@link PatternGrammar#entity}.
	 * @param ctx the parse tree
	 */
	void enterEntity(PatternGrammar.EntityContext ctx);
	/**
	 * Exit a parse tree produced by {@link PatternGrammar#entity}.
	 * @param ctx the parse tree
	 */
	void exitEntity(PatternGrammar.EntityContext ctx);
	/**
	 * Enter a parse tree produced by {@link PatternGrammar#parameterNodeList}.
	 * @param ctx the parse tree
	 */
	void enterParameterNodeList(PatternGrammar.ParameterNodeListContext ctx);
	/**
	 * Exit a parse tree produced by {@link PatternGrammar#parameterNodeList}.
	 * @param ctx the parse tree
	 */
	void exitParameterNodeList(PatternGrammar.ParameterNodeListContext ctx);
}