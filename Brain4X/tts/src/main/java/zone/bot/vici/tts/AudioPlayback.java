package zone.bot.vici.tts;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * Observe and control the playback process of a single audio track.
 *
 * @author Hendrik Motza
 * @since 12.19
 */
public interface AudioPlayback {

    /**
     * Returns true if playback completed.
     *
     * @return true if playback completed
     */
    boolean isFinished();

    /**
     * Calling this method will block until audio playback completed.
     *
     * @throws InterruptedException Thrown if interrupted
     */
    void join() throws InterruptedException;

    /**
     * Stop playback immediately.
     */
    void stop();

    /**
     * Set volume for audio speech synthesis.
     * Please be aware that the final volume may also depend on volume settings of operating system and the physical speaker device.
     *
     * @param volume as value between 0 (off) and 1 (100%) or above (might not always be supported)
     */
    void setVolume(final float volume);

    /**
     * Set a listener that will be called when playback completed.
     * If the playback already completed before this listener got set, the listener will get called (immediately) nevertheless.
     *
     * @param listener to be called when playback completes
     */
    void onFinished(@Nonnull final Consumer<AudioPlayback> listener);

}
