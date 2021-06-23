package zone.bot.vici.pattern.parser;

import zone.bot.vici.pattern.grammar.PatternGrammarBaseListener;
import zone.bot.vici.pattern.grammar.PatternGrammar;
import zone.bot.vici.pattern.model.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

class BotPatternGenerator extends PatternGrammarBaseListener {

	@Nonnull
	private LinkedList<BotPatternNode> stack = new LinkedList<>();
	@Nonnull
	private final Deque<LinkedList<BotPatternNode>> refNodeStack = new LinkedList<>();
	@Nonnull
	private List<BotPatternEntity> entities = new LinkedList<>();

	@Override
	public void exitAnyNode(final PatternGrammar.AnyNodeContext ctx) {
		this.stack.push(new AnyNode(getEntities(), this.stack.pop()));
		super.exitAnyNode(ctx);
	}

	@Override
	public void exitRegexNode(final PatternGrammar.RegexNodeContext ctx) {
		final String value = ctx.patternValue.getText();
		final String regex = value.substring(1, value.length()-1).replace("\\/", "/");
		this.stack.push(new RegexNode(getEntities(), regex));
		super.exitRegexNode(ctx);
	}

	@Override
	public void exitWordNode(final PatternGrammar.WordNodeContext ctx) {
		this.stack.push(new WordNode(getEntities(), ctx.value.getText()));
		super.exitWordNode(ctx);
	}

	@Override
	public void exitSequenceNode(final PatternGrammar.SequenceNodeContext ctx) {
		final LinkedList<BotPatternNode> children = new LinkedList<>();
		final BotPatternNode child2 = this.stack.pop();
		final BotPatternNode child1 = this.stack.pop();
		if(SequenceNode.class.equals(child1.getClass())) {
			children.addAll(((SequenceNode) child1).getChildren());
		} else {
			children.add(child1);
		}
		if(SequenceNode.class.equals(child2.getClass())) {
			children.addAll(((SequenceNode) child2).getChildren());
		} else {
			children.add(child2);
		}
		this.stack.push(new SequenceNode(getEntities(), children));
		super.exitSequenceNode(ctx);
	}

	@Override
	public void exitAlternativesNode(final PatternGrammar.AlternativesNodeContext ctx) {
		final LinkedList<BotPatternNode> children = new LinkedList<>();
		final BotPatternNode child2 = this.stack.pop();
		final BotPatternNode child1 = this.stack.pop();
		if(AlternativesNode.class.equals(child1.getClass())) {
			children.addAll(((AlternativesNode) child1).getChildren());
		} else {
			children.add(child1);
		}
		if(AlternativesNode.class.equals(child2.getClass())) {
			children.addAll(((AlternativesNode) child2).getChildren());
		} else {
			children.add(child2);

		}
		this.stack.push(new AlternativesNode(getEntities(), children));
		super.exitAlternativesNode(ctx);
	}

	@Override
	public void enterReferenceNode(final PatternGrammar.ReferenceNodeContext ctx) {
		this.refNodeStack.push(this.stack);
		this.stack = new LinkedList<>();
		super.enterReferenceNode(ctx);
	}

	@Override
	public void exitReferenceNode(final PatternGrammar.ReferenceNodeContext ctx) {
		final String value = ctx.referenceId.getText();
		Collections.reverse(this.stack);
		final ReferenceNode refNode = new ReferenceNode(getEntities(), value.substring(1), this.stack);
		this.stack = this.refNodeStack.pop();
		this.stack.push(refNode);
		super.exitReferenceNode(ctx);
	}

	@Override
	public void exitLiteralNode(final PatternGrammar.LiteralNodeContext ctx) {
		final String value = ctx.value.getText();
		final char quote = value.charAt(0);
		final String literal = value.substring(1, value.length()-1).replace( "\\"+quote, String.valueOf(quote));
		this.stack.push(new LiteralNode(getEntities(), literal));
		super.exitLiteralNode(ctx);
	}

	@Override
	public void exitOptionalNode(final PatternGrammar.OptionalNodeContext ctx) {
		this.stack.push(new OptionalNode(getEntities(), this.stack.pop()));
		super.exitOptionalNode(ctx);
	}

	@Override
	public void exitGroupNode(final PatternGrammar.GroupNodeContext ctx) {
		this.stack.push(new GroupNode(getEntities(), this.stack.pop()));
		super.exitGroupNode(ctx);
	}

	@Override
	public void exitVariableNode(final PatternGrammar.VariableNodeContext ctx) {
		final String number = ctx.variable.getText().substring(1);
		this.stack.push(new VariableNode(getEntities(), Integer.parseInt(number)));
		super.exitVariableNode(ctx);
	}

	@Override
	public void exitWildcardNode(final PatternGrammar.WildcardNodeContext ctx) {
		this.stack.push(new WildcardNode(getEntities()));
		super.exitWildcardNode(ctx);
	}

	@Override
	public void exitManyNode(final PatternGrammar.ManyNodeContext ctx) {
		this.stack.push(new ManyNode(getEntities(), this.stack.pop()));
		super.exitManyNode(ctx);
	}

	@Override
	public void exitConjunctionListNode(final PatternGrammar.ConjunctionListNodeContext ctx) {
		final BotPatternNode child2 = this.stack.pop();
		final BotPatternNode child1 = this.stack.pop();
		this.stack.push(new ConjunctionListNode(getEntities(), child1, child2));
		super.exitConjunctionListNode(ctx);
	}

	@Override
	public void exitEntity(final PatternGrammar.EntityContext ctx) {
		final String name = ctx.name.getText();
		String value = ctx.fixedValue == null ? null : ctx.fixedValue.getText();
		if(value != null && !value.isEmpty()) {
			final char firstChar = value.charAt(0);
			if((firstChar == '"' || firstChar == '\'') && firstChar == value.charAt(value.length() - 1)) {
				value = value.substring(1, value.length() - 1).replace("\\" + firstChar, String.valueOf(firstChar));
			}
		}
		this.entities.add(new BotPatternEntity(name, value));
		super.exitEntity(ctx);
	}

	@Nullable
	private List<BotPatternEntity> getEntities() {
		if(this.entities.isEmpty()) {
			return null;
		}
		final List<BotPatternEntity> results = this.entities;
		this.entities = new LinkedList<>();
		return results;
	}

	BotPatternNode create() {
		return this.stack.pop();
	}

}
