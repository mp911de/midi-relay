package de.paluch.midi.relay.midi;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Track;

import org.apache.commons.io.FileUtils;

import de.paluch.midi.relay.relay.RemoteRelayReceiver;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author <a href="mailto:mark.paluch@1und1.de">Mark Paluch</a>
 * @since 09.11.12 19:50
 */
@RequiredArgsConstructor
@Slf4j
public class MidiPlayer {

    @NonNull
    String midiDirectory;
    @NonNull
    Sequencer sequencer;
    @NonNull
    RemoteRelayReceiver receiver;

    boolean run = false;
    PlayerState state = null;

    public void play(PlayerState request) {

        if (run) {
            log.info("Already running");
            return;
        }

        run = true;
        receiver.off(0);

        if (run) {
            try {
                this.state = request;
                state.setErrorState(false);
                state.setExceptionMessage(null);
                prepare(request);
                playFile(request);
                receiver.off(0);
            } catch (Exception e) {
                state.setErrorState(true);
                state.setExceptionMessage(e.toString());
                log.warn(request.getFileName() + ": " + e.getMessage(), e);
            } finally {
                if (state != null) {
                    state.setRunning(false);
                }
            }
        }

        receiver.on(0);

        run = false;
    }

    public void play(String fileName) {

        if (run) {
            log.info("Already running");
            return;
        }

        run = true;
        File[] files = getFiles(fileName);

        receiver.off(0);
        if (files != null) {
            List<File> theFiles = Arrays.asList(files);
            Collections.sort(theFiles, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    return o1.getName().compareToIgnoreCase(o2.getName());
                }
            });

            for (File theFile : theFiles) {
                if (run) {
                    try {
                        this.state = new PlayerState();
                        state.setErrorState(false);
                        state.setExceptionMessage(null);

                        byte[] bytes = FileUtils.readFileToByteArray(theFile);
                        state.setMidiContents(bytes);
                        state.setFileName(theFile.getName());

                        prepare(state);
                        playFile(state);
                        receiver.off(0);
                    } catch (Exception e) {
                        state.setErrorState(true);
                        state.setExceptionMessage(e.toString());
                        log.warn(theFile.getName() + ": " + e.getMessage(), e);
                    } finally {
                        if (state != null) {
                            state.setRunning(false);
                        }
                    }
                }
            }
        }

        receiver.on(0);

        run = false;
    }

    private void prepare(PlayerState state) throws InvalidMidiDataException, IOException, MidiUnavailableException {
        Sequence sequence = MidiSystem.getSequence(new ByteArrayInputStream(state.getMidiContents()));
        int durationInSecs = getDurationSecs(sequence);
        state.setDuration(durationInSecs);
        state.setSequenceName(getSequenceName(sequence));
    }

    private int getDurationSecs(Sequence sequence) throws MidiUnavailableException, InvalidMidiDataException {
        sequencer.setSequence(sequence);
        int durationInSecs = (int) (sequencer.getMicrosecondLength() / 1000000.0);
        return durationInSecs;
    }

    protected String getSequenceName(Sequence sequence) {
        for (Track track : sequence.getTracks()) {
            for (int i = 0; i < track.size(); i++) {
                MidiEvent midiEvent = track.get(i);
                if (midiEvent.getMessage() instanceof MetaMessage) {
                    MidiMessageDetail detail = new MidiMessageDetail(midiEvent.getMessage());

                    if (detail.getT2() == 3 || detail.getT2() == 6) {
                        try {
                            if (detail.getBytes()[0] == -1) {
                                return new String(detail.getBytes(), 3, detail.getBytes().length - 3, "ASCII");
                            }

                            return new String(detail.getBytes(), "ASCII");
                        } catch (UnsupportedEncodingException e) {
                        }
                    }

                }
            }
        }

        return null;
    }

    public boolean isRunning(long start, int duration) {
        if (run) {
            long now = System.currentTimeMillis();
            long played = now - start;
            int playedSeconds = (int) played / 1000;
            if (sequencer.isRunning() || playedSeconds < duration) {
                return true;
            }
        }

        return false;
    }

    private void playFile(PlayerState state) throws InvalidMidiDataException, IOException, InterruptedException {

        this.state = state;
        int originalDuration = state.getDuration();
        state.setDuration(originalDuration + 3);
        state.setStarted(System.currentTimeMillis());
        state.setRunning(true);

        log.info("Starting  " + state.getSequenceName() + "/" + state.getFileName());
        receiver.off(0);

        Thread.sleep(1000);
        sequencer.setSequence(MidiSystem.getSequence(new ByteArrayInputStream(state.getMidiContents())));
        long sequenceStarted = System.currentTimeMillis();
        sequencer.start();

        while (isRunning(sequenceStarted, originalDuration)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
        }

        Thread.sleep(2000);
        receiver.off(0);

        DecimalFormat df = getFormatter();
        log.info("Finished file " + state.getSequenceName() + "/" + state.getFileName());
        log.info("Bytes sent: " + df.format(receiver.getBytesSent()));
    }

    private DecimalFormat getFormatter() {
        DecimalFormat df = new DecimalFormat();
        df.setMinimumFractionDigits(0);
        df.setMaximumFractionDigits(0);
        df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.GERMAN));
        df.setGroupingSize(3);
        df.setGroupingUsed(true);
        return df;
    }

    private File[] getFiles(String fileName) {

        if (fileName == null) {
            File file = new File(midiDirectory);

            return file.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".mid");
                }
            });
        }

        File file = new File(midiDirectory, fileName);
        if (file.exists()) {
            return new File[] { file };
        }

        return new File[0];

    }

    public void stop() {
        sequencer.stop();
        run = false;
    }

    public PlayerState getState() {
        return state;
    }
}
