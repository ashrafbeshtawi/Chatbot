package zone.bot.vici.tts;

import marytts.exceptions.MaryConfigurationException;
import org.junit.Test;
import zone.bot.vici.Language;

import java.io.IOException;

public class MaryTTSTest {

    //  got line unavailable exception when running on gitlab runner
    //@Test
    public void testForSuccess() throws MaryConfigurationException, IOException, SpeechSynthesisException {
        // test that no exceptions appear
        final MaryTTS maryTTS = new MaryTTS();
        maryTTS.setVolume(0);
        maryTTS.speak(Language.ENGLISH, "test");
        maryTTS.speak(Language.GERMAN, "test");
    }

    @Test(expected = SpeechSynthesisException.class)
    public void testForUninstalledLanguage() throws MaryConfigurationException, IOException, SpeechSynthesisException {
        // test that no exceptions appear
        final MaryTTS maryTTS = new MaryTTS();
        maryTTS.setVolume(0);
        maryTTS.speak(Language.TURKISH, "test");
    }

}
