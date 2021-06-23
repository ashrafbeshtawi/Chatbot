package zone.bot.vici.nlg.output;

import org.junit.Assert;
import org.junit.Test;
import zone.bot.vici.Language;

public class ResponseBuilderTest {

    private final ResponseBuilder rb = new ResponseBuilder();

    public ResponseBuilderTest() {
        this.rb.registerTemplate(Language.GERMAN, "randTest", "[@rand]Hallo|Hi|Hey||||||||[/@rand]");
        this.rb.registerTemplate(Language.GERMAN, "ttsReplaceTest", "[@tts replace=\"by the way\"]btw[/@tts]");
        this.rb.registerTemplate(Language.GERMAN, "ttsLangTest", "[@tts lang=\"eng\"]hello[/@tts]");
        this.rb.registerTemplate(Language.GERMAN, "ttsPhoneTest", "[@tts phone=\"H A I\"]hi[/@tts]");
    }

    @Test
    public void testRandomizer() throws ResponseTemplateNotFoundException, ResponseProcessingException {
        final String response = this.rb.buildResponse(Language.GERMAN, "randTest", null);
        Assert.assertTrue("Response must not be null", response != null);
        Assert.assertTrue("Rand directive must choose from a non-empty option", !response.isEmpty());
        final boolean foundMatch = "Hallo".equals(response) || "Hi".equals(response) || "Hey".equals(response);
        Assert.assertTrue("Generated response '"+response+"' does not match to any of the random options", foundMatch);
    }

    @Test
    public void testTTSReplace() throws ResponseTemplateNotFoundException, ResponseProcessingException {
        final String response = this.rb.buildResponse(Language.GERMAN, "ttsReplaceTest", null);
        Assert.assertEquals("[tts replace=\"by the way\"]btw[/tts]", response);
    }

    @Test
    public void testTTSLang() throws ResponseTemplateNotFoundException, ResponseProcessingException {
        final String response = this.rb.buildResponse(Language.GERMAN, "ttsLangTest", null);
        Assert.assertEquals("[tts lang=\"en\"]hello[/tts]", response);
    }

    @Test
    public void testTTSPhone() throws ResponseTemplateNotFoundException, ResponseProcessingException {
        final String response = this.rb.buildResponse(Language.GERMAN, "ttsPhoneTest", null);
        Assert.assertEquals("[tts phone=\"H A I\"]hi[/tts]", response);
    }

}
