package zone.bot.vici.nlg.output;

import org.junit.Assert;
import org.junit.Test;
import zone.bot.vici.nlg.output.processors.StripMarkerProcessor;

public class MarkerParserTest {

    @Test
    public void testMarkerStripping() {
        final MarkerParser parser = new MarkerParser(new BBCodeSyntaxConfiguration(), new StripMarkerProcessor());
        final String result = parser.process("abc[bla]def[blub]gh[/blub]ij[bli]k[/bli]lmn[/bla]op");
        Assert.assertEquals("abcdefghijklmnop", result);
    }

}
