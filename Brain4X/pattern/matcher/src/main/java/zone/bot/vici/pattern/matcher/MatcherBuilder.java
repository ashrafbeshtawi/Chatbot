package zone.bot.vici.pattern.matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zone.bot.vici.pattern.model.*;

import javax.annotation.Nonnull;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

class MatcherBuilder {

	@Nonnull
	private static final Logger LOG = LoggerFactory.getLogger(MatcherBuilder.class);

	@Nonnull
	private final BotPattern pattern;
	private final Deque<List<PatternNodeMatcher>> refContextStack = new LinkedList<>();

	MatcherBuilder(@Nonnull final BotPattern pattern) {
		this.pattern = pattern;
	}

	public PatternNodeMatcher build() {
		return buildFromPattern(this.pattern.getPatternRootNode());
	}

	@Nonnull
	private PatternNodeMatcher buildFromPattern(@Nonnull final BotPatternNode node) {
		if(SequenceNode.class.equals(node.getClass())) {
			final SequenceNode sequenceNode = (SequenceNode) node;
			final List<PatternNodeMatcher> children = sequenceNode.getChildren().stream().map(this::buildFromPattern).collect(Collectors.toList());
			return new SequenceNodeMatcher(sequenceNode, children);
		} else if(WordNode.class.equals(node.getClass())) {
			return new WordNodeMatcher((WordNode) node);
		} else if(WildcardNode.class.equals(node.getClass())) {
			return new WildcardNodeMatcher((WildcardNode) node);
		} else if(AlternativesNode.class.equals(node.getClass())) {
			final AlternativesNode alternativesNode = (AlternativesNode) node;
			final List<PatternNodeMatcher> children = alternativesNode.getChildren().stream().map(this::buildFromPattern).collect(Collectors.toList());
			return new AlternativesNodeMatcher(alternativesNode, children);
		} else if(GroupNode.class.equals(node.getClass())) {
			final GroupNode groupNode = (GroupNode) node;
			return new GroupNodeMatcher(groupNode, buildFromPattern(groupNode.getChild()));
		}else if(OptionalNode.class.equals(node.getClass())) {
			final OptionalNode optionalNode = (OptionalNode) node;
			return new OptionalNodeMatcher(optionalNode, buildFromPattern(optionalNode.getChild()));
		}else if(ConjunctionListNode.class.equals(node.getClass())) {
			final ConjunctionListNode clistNode = (ConjunctionListNode) node;
			return new ConjunctionListNodeMatcher(clistNode, buildFromPattern(clistNode.getListNode()), buildFromPattern(clistNode.getConjunctionNode()));
		}else if(AnyNode.class.equals(node.getClass())) {
			final AnyNode anyNode = (AnyNode) node;
			return new AnyNodeMatcher(anyNode, buildFromPattern(anyNode.getChild()));
		}else if(ManyNode.class.equals(node.getClass())) {
			final ManyNode manyNode = (ManyNode) node;
			return new ManyNodeMatcher(manyNode, buildFromPattern(manyNode.getChild()));
		} else if(RegexNode.class.equals(node.getClass())) {
			return new RegexNodeMatcher((RegexNode) node);
		} else if(LiteralNode.class.equals(node.getClass())) {
			return new LiteralNodeMatcher((LiteralNode) node);
		} else if(ReferenceNode.class.equals(node.getClass())) {
			final ReferenceNode refNode = ((ReferenceNode) node);
			final String refName = refNode.getReferenceId();
			final BotPatternNode referencedNode = this.pattern.getContext().getTemplate(refName);
			if(referencedNode == null) {
				throw new IllegalArgumentException("Pattern contains a reference to a template with name '"+refName+"' but the template is not registered in the corresponding context");
			}
			this.refContextStack.push(refNode.getParameterNodes().stream().map(this::buildFromPattern).collect(Collectors.toList()));
			final PatternNodeMatcher refNodeMatcher = buildFromPattern(referencedNode);
			this.refContextStack.pop();
			return refNodeMatcher;
		} else if(VariableNode.class.equals(node.getClass())) {
			final VariableNode varNode = ((VariableNode) node);
			final List<PatternNodeMatcher> refContextParams = this.refContextStack.peek();
			assert refContextParams != null;
			if(refContextParams.size()<=varNode.getIndex()) {
				throw new IllegalArgumentException("Found a variable node for index "+varNode.getIndex()+" but there is no corresponding parameter available for that index");
			}
			return refContextParams.get(varNode.getIndex());
		}
		LOG.warn("No matcher available for pattern node of type '{}'", node.getClass().getCanonicalName());
		return new FailMatcher();
	}

}
