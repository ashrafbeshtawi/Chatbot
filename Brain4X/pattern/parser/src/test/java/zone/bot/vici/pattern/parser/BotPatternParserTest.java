package zone.bot.vici.pattern.parser;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.RecognitionException;
import org.junit.Assert;
import org.junit.Test;
import zone.bot.vici.pattern.grammar.PatternLexer;
import zone.bot.vici.pattern.model.*;

import java.util.List;
import java.util.stream.Collectors;

public class BotPatternParserTest {

	@Test
	public void testSingleQuoteLiteral() {
		final String input = "'hello world'";
		final BotPatternParser parser = new BotPatternParser();
		final BotPatternNode pattern = parser.parse(input).getPatternRootNode();
		Assert.assertEquals(LiteralNode.class, pattern.getClass());
		Assert.assertEquals("hello world", ((LiteralNode)pattern).getLiteral());
		Assert.assertEquals(input, pattern.toString());
	}

	@Test
	public void testDoubleQuoteLiteral() {
		final String input = "\"hello world\"";
		final BotPatternParser parser = new BotPatternParser();
		final BotPatternNode pattern = parser.parse(input).getPatternRootNode();
		Assert.assertEquals(LiteralNode.class, pattern.getClass());
		Assert.assertEquals("hello world", ((LiteralNode)pattern).getLiteral());
	}

	@Test
	public void testSingleQuoteLiteralWithEscaping() {
		final String input = "'hello\\' world'";
		final BotPatternParser parser = new BotPatternParser();
		final BotPatternNode pattern = parser.parse(input).getPatternRootNode();
		Assert.assertEquals(LiteralNode.class, pattern.getClass());
		Assert.assertEquals("hello' world", ((LiteralNode)pattern).getLiteral());
		Assert.assertEquals(input, pattern.toString());
	}

	@Test
	public void testDoubleQuoteLiteralWithEscaping() {
		final String input = "\"hello\\\" world\"";
		final BotPatternParser parser = new BotPatternParser();
		final BotPatternNode pattern = parser.parse(input).getPatternRootNode();
		Assert.assertEquals(LiteralNode.class, pattern.getClass());
		Assert.assertEquals("hello\" world", ((LiteralNode)pattern).getLiteral());
	}

	@Test
	public void testWord() {
		final String input = "hello";
		final BotPatternParser parser = new BotPatternParser();
		final BotPatternNode pattern = parser.parse(input).getPatternRootNode();
		Assert.assertEquals(WordNode.class, pattern.getClass());
		Assert.assertEquals("hello", ((WordNode)pattern).getWord());
		Assert.assertEquals(input, pattern.toString());
	}

	@Test
	public void testReference() {
		final String input = "~hello";
		final BotPatternParser parser = new BotPatternParser();
		final BotPatternNode pattern = parser.parse(input).getPatternRootNode();
		Assert.assertEquals(ReferenceNode.class, pattern.getClass());
		final ReferenceNode refNode = ((ReferenceNode)pattern);
		Assert.assertEquals("hello", refNode.getReferenceId());
		Assert.assertTrue(refNode.getParameterNodes().isEmpty());
		Assert.assertEquals(input, pattern.toString());
	}

	@Test
	public void testReferenceWithEmptyParameterList() {
		final String input = "~hello()";
		final BotPatternParser parser = new BotPatternParser();
		final BotPatternNode pattern = parser.parse(input).getPatternRootNode();
		Assert.assertEquals(ReferenceNode.class, pattern.getClass());
		final ReferenceNode refNode = ((ReferenceNode)pattern);
		Assert.assertEquals("hello", refNode.getReferenceId());
		Assert.assertTrue(refNode.getParameterNodes().isEmpty());
	}

	@Test
	public void testReferenceWithSingleParameter() {
		final String input = "~hello(world)";
		final BotPatternParser parser = new BotPatternParser();
		final BotPatternNode pattern = parser.parse(input).getPatternRootNode();
		Assert.assertEquals(ReferenceNode.class, pattern.getClass());
		final ReferenceNode refNode = ((ReferenceNode)pattern);
		Assert.assertEquals("hello", refNode.getReferenceId());
		final List<BotPatternNode> paramNodes = refNode.getParameterNodes();
		Assert.assertEquals(1, paramNodes.size());
		Assert.assertEquals(WordNode.class, paramNodes.get(0).getClass());
		Assert.assertEquals("world", ((WordNode) paramNodes.get(0)).getWord());
		Assert.assertEquals(input, pattern.toString());
	}

	@Test
	public void testReferenceWithTwoParameters() {
		final String input = "~hello(first, second)";
		final BotPatternParser parser = new BotPatternParser();
		final BotPatternNode pattern = parser.parse(input).getPatternRootNode();
		Assert.assertEquals(ReferenceNode.class, pattern.getClass());
		final ReferenceNode refNode = ((ReferenceNode)pattern);
		Assert.assertEquals("hello", refNode.getReferenceId());
		final List<BotPatternNode> paramNodes = refNode.getParameterNodes();
		Assert.assertEquals(2, paramNodes.size());
		Assert.assertEquals(WordNode.class, paramNodes.get(0).getClass());
		Assert.assertEquals("first", ((WordNode) paramNodes.get(0)).getWord());
		Assert.assertEquals(WordNode.class, paramNodes.get(1).getClass());
		Assert.assertEquals("second", ((WordNode) paramNodes.get(1)).getWord());
		Assert.assertEquals(input, pattern.toString());
	}

	@Test
	public void testRegex() {
		final String input = "/hello/";
		final BotPatternParser parser = new BotPatternParser();
		final BotPatternNode pattern = parser.parse(input).getPatternRootNode();
		Assert.assertEquals(RegexNode.class, pattern.getClass());
		Assert.assertEquals("hello", ((RegexNode)pattern).getPattern().pattern());
		Assert.assertEquals(input, pattern.toString());
	}

	@Test
	public void testRegexWithEscape() {
		final String input = "/hel\\/lo/";
		final BotPatternParser parser = new BotPatternParser();
		final BotPatternNode pattern = parser.parse(input).getPatternRootNode();
		Assert.assertEquals(RegexNode.class, pattern.getClass());
		Assert.assertEquals("hel/lo", ((RegexNode)pattern).getPattern().pattern());
		Assert.assertEquals(input, pattern.toString());
	}

	@Test
	public void testRegexWithNamedCapturingGroup() {
		final String input = "/(?<greeting>hello)/";
		final BotPatternParser parser = new BotPatternParser();
		final BotPatternNode pattern = parser.parse(input).getPatternRootNode();
		Assert.assertEquals(RegexNode.class, pattern.getClass());
		Assert.assertEquals(1, pattern.getNamedEntities().size());
		Assert.assertEquals("greeting", pattern.getNamedEntities().get(0).getName());
	}

	@Test
	public void testVariable() {
		final String input = "$1";
		final BotPatternParser parser = new BotPatternParser();
		final BotPatternNode pattern = parser.parse(input).getPatternRootNode();
		Assert.assertEquals(VariableNode.class, pattern.getClass());
		Assert.assertEquals(1, ((VariableNode) pattern).getIndex());
		Assert.assertEquals(input, pattern.toString());
	}

	@Test
	public void testWildcard() {
		final String input = "_";
		final BotPatternParser parser = new BotPatternParser();
		final BotPatternNode pattern = parser.parse(input).getPatternRootNode();
		Assert.assertEquals(WildcardNode.class, pattern.getClass());
		Assert.assertEquals(input, pattern.toString());
	}

	@Test
	public void testSequence() {
		final String input = "hello pretty world";
		final BotPatternParser parser = new BotPatternParser();
		final BotPatternNode pattern = parser.parse(input).getPatternRootNode();
		Assert.assertEquals(SequenceNode.class, pattern.getClass());
		final List<BotPatternNode> children = ((SequenceNode) pattern).getChildren();
		Assert.assertEquals(3, children.size());
		Assert.assertEquals(WordNode.class, children.get(0).getClass());
		Assert.assertEquals(WordNode.class, children.get(1).getClass());
		Assert.assertEquals(WordNode.class, children.get(2).getClass());
		Assert.assertEquals("hello", ((WordNode)children.get(0)).getWord());
		Assert.assertEquals("pretty", ((WordNode)children.get(1)).getWord());
		Assert.assertEquals("world", ((WordNode)children.get(2)).getWord());
		Assert.assertEquals(input, pattern.toString());
	}

	@Test
	public void testAlternative() {
		final String input = "hello | hey | hi";
		final BotPatternParser parser = new BotPatternParser();
		final BotPatternNode pattern = parser.parse(input).getPatternRootNode();
		Assert.assertEquals(AlternativesNode.class, pattern.getClass());
		final List<BotPatternNode> children = ((AlternativesNode) pattern).getChildren();
		Assert.assertEquals(3, children.size());
		Assert.assertEquals(WordNode.class, children.get(0).getClass());
		Assert.assertEquals(WordNode.class, children.get(1).getClass());
		Assert.assertEquals(WordNode.class, children.get(2).getClass());
		Assert.assertEquals("hello", ((WordNode)children.get(0)).getWord());
		Assert.assertEquals("hey", ((WordNode)children.get(1)).getWord());
		Assert.assertEquals("hi", ((WordNode)children.get(2)).getWord());
		Assert.assertEquals(input, pattern.toString());
	}

	@Test
	public void testGroup() {
		final String input = "(hello)";
		final BotPatternParser parser = new BotPatternParser();
		final BotPatternNode pattern = parser.parse(input).getPatternRootNode();
		Assert.assertEquals(GroupNode.class, pattern.getClass());
		final BotPatternNode child = ((GroupNode) pattern).getChild();
		Assert.assertEquals(WordNode.class, child.getClass());
		Assert.assertEquals("hello", ((WordNode)child).getWord());
		Assert.assertEquals(input, pattern.toString());
	}

	@Test
	public void testOptional() {
		final String input = "hello?";
		final BotPatternParser parser = new BotPatternParser();
		final BotPatternNode pattern = parser.parse(input).getPatternRootNode();
		Assert.assertEquals(OptionalNode.class, pattern.getClass());
		final BotPatternNode child = ((OptionalNode) pattern).getChild();
		Assert.assertEquals(WordNode.class, child.getClass());
		Assert.assertEquals("hello", ((WordNode)child).getWord());
		Assert.assertEquals(input, pattern.toString());
	}

	@Test
	public void testMany() {
		final String input = "hello+";
		final BotPatternParser parser = new BotPatternParser();
		final BotPatternNode pattern = parser.parse(input).getPatternRootNode();
		Assert.assertEquals(ManyNode.class, pattern.getClass());
		final BotPatternNode child = ((ManyNode) pattern).getChild();
		Assert.assertEquals(WordNode.class, child.getClass());
		Assert.assertEquals("hello", ((WordNode)child).getWord());
		Assert.assertEquals(input, pattern.toString());
	}

	@Test
	public void testAny() {
		final String input = "hello*";
		final BotPatternParser parser = new BotPatternParser();
		final BotPatternNode pattern = parser.parse(input).getPatternRootNode();
		Assert.assertEquals(AnyNode.class, pattern.getClass());
		final BotPatternNode child = ((AnyNode) pattern).getChild();
		Assert.assertEquals(WordNode.class, child.getClass());
		Assert.assertEquals("hello", ((WordNode)child).getWord());
		Assert.assertEquals(input, pattern.toString());
	}

	@Test
	public void testConjunctionList() {
		final String input = "item+(and)";
		final BotPatternParser parser = new BotPatternParser();
		final BotPatternNode pattern = parser.parse(input).getPatternRootNode();
		Assert.assertEquals(ConjunctionListNode.class, pattern.getClass());
		final BotPatternNode child = ((ConjunctionListNode) pattern).getListNode();
		Assert.assertEquals(WordNode.class, child.getClass());
		Assert.assertEquals("item", ((WordNode)child).getWord());
		final BotPatternNode conjunctionNode = ((ConjunctionListNode) pattern).getConjunctionNode();
		Assert.assertEquals(WordNode.class, conjunctionNode.getClass());
		Assert.assertEquals("and", ((WordNode)conjunctionNode).getWord());
		Assert.assertEquals(input, pattern.toString());
	}

	@Test
	public void testEntity() {
		final String input = "hello{greeting}";
		final BotPatternParser parser = new BotPatternParser();
		final BotPatternNode pattern = parser.parse(input).getPatternRootNode();
		Assert.assertEquals(WordNode.class, pattern.getClass());
		Assert.assertEquals("hello", ((WordNode)pattern).getWord());
		final List<BotPatternEntity> entities = pattern.getNamedEntities();
		Assert.assertEquals(1, entities.size());
		Assert.assertEquals("greeting", entities.get(0).getName());
		Assert.assertNull(entities.get(0).getFixedValue());
		Assert.assertEquals(input, pattern.toString());
	}

	@Test
	public void testEntityWithFixedValue() {
		final String input = "hello{greeting=HELLO}";
		final BotPatternParser parser = new BotPatternParser();
		final BotPatternNode pattern = parser.parse(input).getPatternRootNode();
		Assert.assertEquals(WordNode.class, pattern.getClass());
		Assert.assertEquals("hello", ((WordNode)pattern).getWord());
		final List<BotPatternEntity> entities = pattern.getNamedEntities();
		Assert.assertEquals(1, entities.size());
		Assert.assertEquals("greeting", entities.get(0).getName());
		Assert.assertEquals("HELLO", entities.get(0).getFixedValue());
		Assert.assertEquals(input, pattern.toString());
	}

	@Test
	public void testEntityWithFixedValueContainingUnderscore() {
		final String input = "hello{greeting=HELLO_WORLD}";
		final BotPatternParser parser = new BotPatternParser();
		final BotPatternNode pattern = parser.parse(input).getPatternRootNode();
		Assert.assertEquals(WordNode.class, pattern.getClass());
		Assert.assertEquals("hello", ((WordNode)pattern).getWord());
		final List<BotPatternEntity> entities = pattern.getNamedEntities();
		Assert.assertEquals(1, entities.size());
		Assert.assertEquals("greeting", entities.get(0).getName());
		Assert.assertEquals("HELLO_WORLD", entities.get(0).getFixedValue());
		Assert.assertEquals(input, pattern.toString());
	}

	@Test
	public void testEntitiesMixes() {
		final String input = "hello{greetingRaw}{greeting=HELLO}";
		final BotPatternParser parser = new BotPatternParser();
		final BotPatternNode pattern = parser.parse(input).getPatternRootNode();
		Assert.assertEquals(WordNode.class, pattern.getClass());
		Assert.assertEquals("hello", ((WordNode)pattern).getWord());
		final List<BotPatternEntity> entities = pattern.getNamedEntities();
		Assert.assertEquals(2, entities.size());
		Assert.assertEquals("greetingRaw", entities.get(0).getName());
		Assert.assertNull(entities.get(0).getFixedValue());
		Assert.assertEquals("greeting", entities.get(1).getName());
		Assert.assertEquals("HELLO", entities.get(1).getFixedValue());
		Assert.assertEquals(input, pattern.toString());
	}

	@Test
	public void testEntitiesMixesInSequence() {
		final String input = "something hello{greetingRaw}{greeting=HELLO}";
		final BotPatternParser parser = new BotPatternParser();
		final BotPatternNode pattern = parser.parse(input).getPatternRootNode();
		Assert.assertEquals(SequenceNode.class, pattern.getClass());
		final BotPatternNode subPattern = ((SequenceNode)pattern).getChildren().get(1);
		Assert.assertEquals("hello", ((WordNode)subPattern).getWord());
		final List<BotPatternEntity> entities = subPattern.getNamedEntities();
		Assert.assertEquals(2, entities.size());
		Assert.assertEquals("greetingRaw", entities.get(0).getName());
		Assert.assertNull(entities.get(0).getFixedValue());
		Assert.assertEquals("greeting", entities.get(1).getName());
		Assert.assertEquals("HELLO", entities.get(1).getFixedValue());
		Assert.assertEquals(input, pattern.toString());
	}

	@Test
	public void testEntitiesMixesOnGroup() {
		final String input = "(hello){greetingRaw}{greeting=HELLO}";
		final BotPatternParser parser = new BotPatternParser();
		final BotPatternNode pattern = parser.parse(input).getPatternRootNode();
		Assert.assertEquals(GroupNode.class, pattern.getClass());
		final List<BotPatternEntity> entities = pattern.getNamedEntities();
		Assert.assertEquals(2, entities.size());
		Assert.assertEquals("greetingRaw", entities.get(0).getName());
		Assert.assertNull(entities.get(0).getFixedValue());
		Assert.assertEquals("greeting", entities.get(1).getName());
		Assert.assertEquals("HELLO", entities.get(1).getFixedValue());
		Assert.assertEquals(input, pattern.toString());
	}

	@Test(expected = RecognitionException.class)
	public void testIncompleteGroup() {
		final BotPatternParser parser = new BotPatternParser();
		parser.parse("(hello");
	}

	@Test(expected = RecognitionException.class)
	public void testInvalidToken() {
		final BotPatternParser parser = new BotPatternParser();
		parser.parse(",");
	}

}
