package zone.bot.vici.nlg.output;

import org.junit.Test;
import org.junit.Assert;
import zone.bot.vici.nlg.output.MarkerFinder.MarkerNode;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.regex.PatternSyntaxException;

public class MarkerFinderTest {

    @Nonnull
    private final MarkerFinder finder = new MarkerFinder(new BBCodeSyntaxConfiguration());

    @Test
    public void testEscapedTags() {
        final String sample = "\\[tag]\\[/tag]";
        final List<MarkerNode> markerNodes = this.finder.findMarker(sample);
        Assert.assertTrue("Should not find any marker when the only marker is escaped", markerNodes.isEmpty());
    }

    @Test(expected = PatternSyntaxException.class)
    public void testUnclosedTag() {
        final String sample = "[tag]...";
        final List<MarkerNode> markerNodes = this.finder.findMarker(sample);
        System.out.println(markerNodes.size());
    }

    @Test(expected = PatternSyntaxException.class)
    public void testIncompleteStartTag() {
        final String sample = "[tag";
		this.finder.findMarker(sample);
    }

    @Test
    public void testElementarTag() {
        final String sample = "[tag][/tag]";
        final List<MarkerNode> markerNodes = this.finder.findMarker(sample);
        Assert.assertEquals(1, markerNodes.size());
        final MarkerNode marker = markerNodes.get(0);
        final MarkerData markerData = marker.getMarkerData();
        Assert.assertEquals("tag", markerData.getTagName());
        Assert.assertFalse(markerData.getOptUnnamedParameter().isPresent());
    }

    @Test
    public void testTagWithContent() {
        final String sample = "  [tag]content[/tag]  ";
        final List<MarkerNode> markerNodes = this.finder.findMarker(sample);
        Assert.assertEquals(1, markerNodes.size());
        final MarkerNode marker = markerNodes.get(0);
        final MarkerData markerData = marker.getMarkerData();
        Assert.assertEquals("tag", markerData.getTagName());
        Assert.assertEquals("content", sample.substring(marker.getContentBegin(), marker.getContentEnd()));
        Assert.assertEquals("[tag]content[/tag]", sample.substring(marker.getBegin(), marker.getEnd()));
    }

    @Test
    public void testTagWithUnnamedParam() {
        final String sample = "[tag=value][/tag]";
        final List<MarkerNode> markerNodes = this.finder.findMarker(sample);
        Assert.assertEquals(1, markerNodes.size());
        final MarkerNode marker = markerNodes.get(0);
        final MarkerData markerData = marker.getMarkerData();
        Assert.assertEquals("tag", markerData.getTagName());
        Assert.assertTrue(markerData.getOptUnnamedParameter().isPresent());
        Assert.assertEquals("value", markerData.getOptUnnamedParameter().get());
    }

    @Test
    public void testTagWithNamedParams() {
        final String sample = "[tag key1=value1 key2=\"value2\"][/tag]";
        final List<MarkerNode> markerNodes = this.finder.findMarker(sample);
        Assert.assertEquals(1, markerNodes.size());
        final MarkerNode marker = markerNodes.get(0);
        final MarkerData markerData = marker.getMarkerData();
        Assert.assertEquals("tag", markerData.getTagName());
        Assert.assertFalse(markerData.getOptUnnamedParameter().isPresent());
        Assert.assertTrue(markerData.getNamedParameters().containsKey("key1"));
        Assert.assertEquals("value1", markerData.getNamedParameters().get("key1"));
        Assert.assertTrue(markerData.getNamedParameters().containsKey("key2"));
        Assert.assertEquals("value2", markerData.getNamedParameters().get("key2"));
    }

    @Test
    public void testNestedTags() {
        final String sample = "[tag1]childrens here[tag2]...[/tag2] [tag3]_[/tag3]childrens end[/tag1]";
        final List<MarkerNode> markerNodes = this.finder.findMarker(sample);
        Assert.assertEquals(1, markerNodes.size());
        final MarkerNode marker = markerNodes.get(0);
        final MarkerData markerData = marker.getMarkerData();
        Assert.assertEquals("tag1", markerData.getTagName());

        Assert.assertEquals(2, marker.getChildMarker().size());
        final MarkerNode marker2 = marker.getChildMarker().get(0);
        final MarkerNode marker3 = marker.getChildMarker().get(1);
        Assert.assertEquals("tag2", marker2.getMarkerData().getTagName());
        Assert.assertEquals("tag3", marker3.getMarkerData().getTagName());
    }


    @Test
    public void testHTMLConfig() {
        final MarkerFinder htmlFinder = new MarkerFinder(new HTMLSyntaxConfiguration());
        final String sample = "<tag key=\"value\"></tag>";
        final List<MarkerNode> markerNodes = htmlFinder.findMarker(sample);
        Assert.assertEquals(1, markerNodes.size());
        final MarkerNode marker = markerNodes.get(0);
        final MarkerData markerData = marker.getMarkerData();
        Assert.assertEquals("tag", markerData.getTagName());
        Assert.assertTrue(markerData.getNamedParameters().containsKey("key"));
        Assert.assertEquals("value", markerData.getNamedParameters().get("key"));
    }

}