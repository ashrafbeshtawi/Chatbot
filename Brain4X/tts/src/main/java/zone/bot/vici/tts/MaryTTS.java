package zone.bot.vici.tts;

import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.datatypes.MaryDataType;
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;
import marytts.util.dom.DomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import zone.bot.vici.Language;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sound.sampled.*;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;
import java.util.function.BiFunction;

/**
 * TTS engine using MaryTTS.
 *
 * @author Hendrik Motza
 * @since 12.19
 */
public class MaryTTS implements TextToSpeech{

    @Nonnull
    private static final Logger LOG = LoggerFactory.getLogger(MaryTTS.class);

    private static final MaryDataType[] supportedInputTypes = new MaryDataType[] { MaryDataType.TEXT, MaryDataType.RAWMARYXML };

    @Nonnull
    private final MaryInterface mary;

    @Nonnull
    private final AudioPlayer audioPlayer;

    @Nonnull
    private final Map<String, String> langCode3VoiceMap = new HashMap<>();

    @Nonnull
    private final Map<String, String> langCode3LocaleMap = new HashMap<>();

    @Nonnull
    private BiFunction<String, Language, String>[] preProcessors = new BiFunction[0];

    @Nullable
    private String ttsStateFile = null;

    @Nonnull
    private static final AudioFormat defaultAudioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 16000f, 16, 1, 1*16/8, 16000f, false);


    public MaryTTS() throws MaryConfigurationException {
        this(getDefaultMixer());
    }

    public MaryTTS(@Nonnull final Mixer mixer) throws MaryConfigurationException {
        Objects.requireNonNull(mixer, "Parameter 'mixer' must not be null");
        this.audioPlayer = new AudioPlayer(mixer);
        this.mary = new LocalMaryInterface();
        final Set<Locale> availableLocales = this.mary.getAvailableLocales();
        if(availableLocales.isEmpty()) {
            LOG.warn("No available languages found for MaryTTS. Please ensure that the required languages and voices are installed for your application.");
        }
        for(final Locale locale : availableLocales) {
            final String langCode3 = locale.getISO3Language();
            this.langCode3LocaleMap.put(langCode3, locale.toString());
            final Set<String> availableVoices = this.mary.getAvailableVoices(locale);
            final Iterator<String> iterator = availableVoices.iterator();
            if(!iterator.hasNext()) {
                LOG.warn("No voice found for language '{}' [{}]", langCode3, locale);
            } else {
                final String voiceName = iterator.next();
                this.langCode3VoiceMap.put(langCode3, voiceName);
                if(iterator.hasNext()) {
                    LOG.info("{} voices found for language '{}' [{}], voice '{}' will be used as default.", availableVoices.size(), langCode3, locale, voiceName);
                } else {
                    LOG.info("Found voice for language '{}' [{}]: {}", langCode3, locale, voiceName);
                }
            }

        }
    }

    @Nullable
    String getLocaleIdentifierByLangCode3(@Nonnull final String langCode3) {
        return this.langCode3LocaleMap.get(langCode3);
    }

    public void setInputType(@Nonnull final MaryDataType inputType) {
        for(final MaryDataType t : supportedInputTypes) {
            if(t.equals(inputType)) {
                this.mary.setInputType(inputType.name());
                return;
            }
        }
        throw new IllegalArgumentException("Input type '"+inputType.name()+"' is not allowed.");
    }

    private static Mixer getDefaultMixer() throws MaryConfigurationException {
        final Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        final DataLine.Info info = new DataLine.Info(SourceDataLine.class, defaultAudioFormat);
        for(final Mixer.Info mixerInfo : mixerInfos) {
            final Mixer mixer = AudioSystem.getMixer(mixerInfo);
            if(mixer.isLineSupported(info)) {
                return mixer;
            }
        }
        throw new MaryConfigurationException("Could not find any audio output device");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPreProcessors(@Nonnull final BiFunction<String, Language, String>... processors) {
        this.preProcessors = Objects.requireNonNull(processors, "Parameter 'processors' must not be null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getVolume() {
        return this.audioPlayer.getVolume();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVolume(final float volume) {
        this.audioPlayer.setVolume(volume);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void speak(@Nonnull final Language language, @Nonnull final String text) throws SpeechSynthesisException, IOException {
        speak(language, text, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void speak(@Nonnull final Language language, @Nonnull final String text, final boolean interrupt) throws SpeechSynthesisException, IOException {
        final String voiceName = this.langCode3VoiceMap.get(language.getLangCode3());
        if(voiceName == null) {
            throw new SpeechSynthesisException("No voice found for language '"+language.getLangCode3()+"'");
        }
        String inputString = text;
        for (final BiFunction<String, Language, String> preProcessor : this.preProcessors) {
            inputString = preProcessor.apply(inputString, language);
        }
        final AudioInputStream audioInputStream;
        synchronized (this.mary) {
            try {
                this.mary.setVoice(voiceName);
                if(MaryDataType.RAWMARYXML.name().equals(this.mary.getInputType())) {
                    audioInputStream = this.mary.generateAudio(convertStringToXMLDocument(inputString));
                } else {
                    audioInputStream = this.mary.generateAudio(inputString);
                }
            } catch (final SynthesisException e) {
                throw new SpeechSynthesisException(e.getMessage(), e);
            }
        }
        if(interrupt) {
            this.audioPlayer.stop();
        }
        final AudioPlayback audioPlayback = this.audioPlayer.playAudio(audioInputStream);
        if(!text.equals("Tamam, lütfen söylediklerimi tekrar et ve sesini kaydedelim.")) {
            writeTTSState(audioPlayback);
        }
    }

    private static Document convertStringToXMLDocument(final String xmlString)
    {
        try {
            return DomUtils.parseDocument(xmlString, false);
        } catch (final ParserConfigurationException | SAXException | IOException e) {
            throw new IllegalArgumentException("Provided input is not a valid xmlString");
        }
    }

    //

    /**
     * Writes a file with static content to notify ASR component about end of playback.
     *
     * @deprecated Dirty hack for compatibility with IntelliOven application, should be replaced by a REST or WebSocket api call
     *
     * @param ttsStateFile path to target file
     */
    @Deprecated
    public void setTtsStateFile(@Nullable final String ttsStateFile) {
        this.ttsStateFile = ttsStateFile;
    }

    private void writeTTSState(@Nonnull final AudioPlayback audioPlayback) {
        if(this.ttsStateFile == null) {
            return;
        }
        new Thread(() -> {
            try {
                audioPlayback.join();
            } catch (final InterruptedException e) {
                LOG.error(e.getMessage(), e);
                return;
            }
            try {
                final PrintWriter writer = new PrintWriter(this.ttsStateFile);
                writer.println("spoken=true");
                writer.close();
            } catch (final FileNotFoundException e) {
                LOG.error(e.getMessage(), e);
            }
        }).start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void playSound(@Nonnull final AudioInputStream ais) throws IOException {
        playSound(ais, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void playSound(@Nonnull final AudioInputStream ais, final boolean interrupt) throws IOException {
        if(interrupt) {
            this.audioPlayer.stop();
        }
        this.audioPlayer.playAudio(ais);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        this.audioPlayer.stop();
    }

}
