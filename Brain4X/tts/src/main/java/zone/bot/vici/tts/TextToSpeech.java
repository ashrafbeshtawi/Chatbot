package zone.bot.vici.tts;

import zone.bot.vici.Language;

import javax.annotation.Nonnull;
import javax.sound.sampled.AudioInputStream;
import java.io.IOException;
import java.util.function.BiFunction;

/**
 * TTS Engine that can generate speech from text and play the sound to the user.
 *
 * @author Hendrik Motza
 * @since 12.19
 */
public interface TextToSpeech {

    /**
     * Sets a list of functions to be used for pre-processing the speech input text.
     * The functions take the input text and its language as argument and return the new (modified) input text that shall be used for speech synthesis.
     * These processors can for example be used to convert the input into a format that improve the generated speech (pronounciation fixes, converting numeric numbers into word representation...) or they can extract meta-data that might be included in the input text string (for example bb-codes) and process them accordingly.
     *
     * @param processors that should be used to pre-process the input text, {@code null} disallowed
     */
    void setPreProcessors(@Nonnull final BiFunction<String, Language, String>... processors);

    /**
     * Get current volume setting.
     * Please be aware that the final volume may also depend on volume settings of operating system and the physical speaker device.
     *
     * @return volume as value between 0 (off) and 1 (100%) or above (might not always be supported)
     */
    float getVolume();

    /**
     * Set volume for audio speech synthesis.
     * Please be aware that the final volume may also depend on volume settings of operating system and the physical speaker device.
     *
     * @param volume as value between 0 (off) and 1 (100%) or above (might not always be supported)
     */
    void setVolume(final float volume);

    /**
     * Produce and play speech.
     *
     * @param language in which the {@code text} is written, {@code null} disallowed
     * @param text to be spoken, {@code null} disallowed
     * @throws SpeechSynthesisException Thrown if speech synthesis failed, for example if the language is not supported
     * @throws IOException Thrown if generated audio data could not be played, for example if audio output device is not available
     */
    void speak(@Nonnull final Language language, @Nonnull final String text) throws SpeechSynthesisException, IOException;

    /**
     * Produce and play speech.
     *
     * @param language in which the {@code text} is written, {@code null} disallowed
     * @param text to be spoken, {@code null} disallowed
     * @param interrupt currently playing speech/sound if set to true
     * @throws SpeechSynthesisException Thrown if speech synthesis failed, for example if the language is not supported
     * @throws IOException Thrown if generated audio data could not be played, for example if audio output device is not available
     */
    void speak(@Nonnull final Language language, @Nonnull final String text, final boolean interrupt) throws SpeechSynthesisException, IOException;

    /**
     * Play some custom sound to output audio device.
     *
     * @param ais audio input stream providing the sound to be played, {@code null} disallowed
     * @throws IOException Thrown if provided audio data could not be played, for example if audio output device is not available
     */
    void playSound(@Nonnull final AudioInputStream ais) throws IOException;

    /**
     * Play some custom sound to output audio device.
     *
     * @param ais audio input stream providing the sound to be played, {@code null} disallowed
     * @param interrupt currently playing speech/sound if set to true
     * @throws IOException Thrown if provided audio data could not be played, for example if audio output device is not available
     */
    void playSound(@Nonnull final AudioInputStream ais, final boolean interrupt) throws IOException;

    /**
     * If any audio (speech/sound) is still played, this will try to interrupt the playback.
     * There is no feedback whether there was a playback to be stopped or if it got stopped successfully.
     */
    void stop();

}