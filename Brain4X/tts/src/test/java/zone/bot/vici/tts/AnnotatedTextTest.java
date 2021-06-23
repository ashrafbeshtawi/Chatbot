package zone.bot.vici.tts;

import marytts.exceptions.MaryConfigurationException;
import org.junit.Assert;
import org.junit.Test;
import zone.bot.vici.Language;

import javax.annotation.Nonnull;

public class AnnotatedTextTest {

    @Nonnull
    private final String rawMaryXMLPrefix_German = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<maryxml version=\"0.4\"\n" +
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "xmlns=\"http://mary.dfki.de/2002/MaryXML\"\n" +
            "xml:lang=\"de\">";
    @Nonnull
    private final String rawMaryXMLSuffix = "</maryxml>";

    @Nonnull
	private static final String[] TEST_MESSAGES = new String[] {
        "[tts=b]a[/tts]",
        "[tts replace=b]a[/tts]",
        "[tts replace=\"b\"]a[/tts]",
        "[tts lang=\"deu\"]a[/tts]",
        "[tts phone=\"b\"]a[/tts]",
        "[tts lang=\"bla\"]a[/tts]"
    };

    @Test
    public void testRawMaryXMLConversion() throws MaryConfigurationException {
        final String[] expected = new String[] {
				this.rawMaryXMLPrefix_German +"b"+ this.rawMaryXMLSuffix,
				this.rawMaryXMLPrefix_German +"b"+ this.rawMaryXMLSuffix,
				this.rawMaryXMLPrefix_German +"b"+ this.rawMaryXMLSuffix,
				this.rawMaryXMLPrefix_German +"<t xml:lang=\"de\">a</t>"+ this.rawMaryXMLSuffix,
				this.rawMaryXMLPrefix_German +"<t ph=\"b\">a</t>"+ this.rawMaryXMLSuffix,
				this.rawMaryXMLPrefix_German +"a"+ this.rawMaryXMLSuffix
        };

        final MaryTTS maryTTS = new MaryTTS();
        final AnnotatedTextToRawMaryXmlConverter converter = new AnnotatedTextToRawMaryXmlConverter(maryTTS);

        for(int i=0; i<TEST_MESSAGES.length; i++) {
            final String result = converter.apply(TEST_MESSAGES[i], Language.GERMAN);
            Assert.assertEquals(expected[i], result);
        }
    }


}
