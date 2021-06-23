package zone.bot.vici.tts;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Player used to play audio on a specific device and control volume, wait for playback to be finished, play multiple sounds at once or interrupt playback before end of audio track.
 *
 * @author Hendrik Motza
 * @since 12.19
 */
public class AudioPlayer {

    @Nonnull
    private static final Logger LOG = LoggerFactory.getLogger(AudioPlayer.class);

    private class AudioPlaybackImpl implements AudioPlayback {

        @Nonnull
        private final AudioInputStream ais;
        @Nonnull
        private final SourceDataLine line;
        @Nonnull
        private final Thread thread;
        @Nullable
        private Consumer<AudioPlayback> listener;

        private volatile boolean interrupted = false;
        private boolean finished = false;

        public AudioPlaybackImpl(@Nonnull final AudioInputStream audioSource) throws IOException {
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioSource.getFormat());
            if(AudioPlayer.this.mixer.isLineSupported(info)) {
                this.ais = audioSource;
            } else {
                LOG.info("Line not supported, changing format...");
                final AudioFormat sourceFormat = audioSource.getFormat();
                final AudioFormat targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sourceFormat.getSampleRate(), sourceFormat.getSampleSizeInBits(),
                        sourceFormat.getChannels(), sourceFormat.getChannels() * ( sourceFormat.getSampleSizeInBits() / 8 ), sourceFormat.getSampleRate(),
                        sourceFormat.isBigEndian());
                this.ais = AudioSystem.getAudioInputStream(targetFormat, audioSource);
                info = new DataLine.Info(SourceDataLine.class, targetFormat);
                if(AudioPlayer.this.mixer.isLineSupported(info)) {
                    final Set<String> props = targetFormat.properties().entrySet().stream().map((e) -> e.getKey() + "=" + e.getValue()).collect(
                            Collectors.toSet());
                    throw new IOException("Format not supported: " + String.join(", ", props));
                }
            }
            this.line = openLine(AudioPlayer.this.mixer, info);
            setVolume(AudioPlayer.this.volume);
            this.thread = new Thread(() -> {
                this.line.start();
                try {
                    int nRead = 0;
                    final byte[] abData = new byte[65532];
                    while ( ( nRead != -1 ) && ( !this.interrupted)) {
                        nRead = this.ais.read(abData, 0, abData.length);
                        if (nRead >= 0) {
                            this.line.write(abData, 0, nRead);
                        }
                    }
                    if (!this.interrupted) {
                        this.line.drain();
                    }
                } catch (final IOException e) {
                    LOG.error(e.getMessage(), e);
                } finally {
                    this.line.close();
                }
            });
        }

        private SourceDataLine openLine(@Nonnull final Mixer mixer, @Nonnull final DataLine.Info info) throws IOException {
            final SourceDataLine ln;
            try {
                ln = (SourceDataLine) mixer.getLine(info);
                ln.open(this.ais.getFormat());
            } catch (final LineUnavailableException e) {
                throw new IOException("Can't get line from mixer", e);
            }
            ln.addLineListener(new LineListener() {
                @Override
                public synchronized void update(final LineEvent event) {
                    if(AudioPlaybackImpl.this.finished) return;
                    if(LineEvent.Type.CLOSE.equals(event.getType())) {
                        AudioPlaybackImpl.this.finished = true;
                        if(AudioPlaybackImpl.this.listener != null) {
                            AudioPlaybackImpl.this.listener.accept(AudioPlaybackImpl.this);
                        }
                    }
                }
            });
            return ln;
        }

        private void start() {
            this.thread.start();
        }

        @Override
        public boolean isFinished() {
            return this.finished;
        }

        @Override
        public synchronized void join() throws InterruptedException {
            this.thread.join();
        }

        @Override
        public synchronized void stop() {
            this.interrupted = true;
            this.thread.interrupt();
        }

        @Override
        public void setVolume(final float volume) {
            if (this.line.isControlSupported(FloatControl.Type.MASTER_GAIN))
                ( (FloatControl) this.line.getControl(FloatControl.Type.MASTER_GAIN) ).setValue((float) ( 20 * Math.log10(volume <= 0.0 ? 0.0000 : volume) ));
        }

        @Override
        public void onFinished(@Nonnull final Consumer<AudioPlayback> listener) {
            this.listener = listener;
            if(this.finished) {
                listener.accept(this);
            }
        }
    }

    @Nonnull
    private final Mixer mixer;

    private float volume = 1f;

    @Nonnull
    private final List<AudioPlayback> runningTracks = new LinkedList<>();

    public AudioPlayer(@Nonnull final Mixer mixer) {
        this.mixer = Objects.requireNonNull(mixer, "Parameter 'mixer' must not be null");
    }

    /**
     * Get current volume setting.
     * Please be aware that the final volume may also depend on volume settings of operating system and the physical speaker device.
     *
     * @return volume as value between 0 (off) and 1 (100%) or above (might not always be supported)
     */
    public float getVolume() {
        return this.volume;
    }

    /**
     * Set volume for audio speech synthesis.
     * Please be aware that the final volume may also depend on volume settings of operating system and the physical speaker device.
     *
     * @param volume as value between 0 (off) and 1 (100%) or above (might not always be supported)
     */
    public void setVolume(final float volume) {
        if(volume<0) {
            throw new IllegalArgumentException("Volume must not be a negative value");
        }
        this.volume = volume;
        synchronized (this.runningTracks) {
            for(final AudioPlayback track : this.runningTracks) {
                track.setVolume(volume);
            }
        }
     }

    @Nonnull
    public AudioPlayback playAudio(@Nonnull final AudioInputStream audioInputStream) throws IOException {
        Objects.requireNonNull(audioInputStream, "Parameter 'audioInputStream' must not be null");
        final AudioPlaybackImpl track = new AudioPlaybackImpl(audioInputStream);
        synchronized (this.runningTracks) {
            this.runningTracks.add(track);
        }
        track.onFinished((t) -> {
            synchronized (this.runningTracks) {
                this.runningTracks.remove(t);
            }
        });
        track.start();
        return track;
    }

    public synchronized void stop() {
        synchronized (this.runningTracks) {
            for(final AudioPlayback track : this.runningTracks) {
                track.stop();
            }
        }
    }

}
