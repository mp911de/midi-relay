package de.paluch.midi.relay.midi;

import de.paluch.midi.relay.relay.ETHRLY16;
import org.apache.log4j.Logger;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequencer;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

/**
 * @author <a href="mailto:mark.paluch@1und1.de">Mark Paluch</a>
 * @since 09.11.12 19:50
 */
public class MidiPlayer {

    private Logger log = Logger.getLogger(getClass());
    private String midiDirectory;
    private Sequencer sequencer;
    private ETHRLY16 receiver;
    private boolean run = false;

    public MidiPlayer(String midiDirectory, Sequencer sequencer, ETHRLY16 receiver) {
        this.midiDirectory = midiDirectory;
        this.sequencer = sequencer;
        this.receiver = receiver;
    }

    public void play() {


        if (run) {
            log.info("Already running");
            return;
        }

        run = true;
        File[] files = getFiles();

        if (files != null) {
            List<File> theFiles = Arrays.asList(files);
            Collections.shuffle(theFiles);

            for (File theFile : theFiles) {
                if (run) {
                    try {
                        receiver.off(0);
                        Thread.sleep(1000);
                        playFile(theFile);
                        receiver.off(0);
                    } catch (Exception e) {
                        log.warn(theFile.getName() + ": " + e.getMessage(), e);
                    }
                }
            }
        }

        receiver.on(0);

    }

    public boolean isRunning() {
        if (run && sequencer.isRunning()) {
            return true;
        }

        return false;
    }

    private void playFile(File theFile) throws InvalidMidiDataException, IOException {

        sequencer.setSequence(MidiSystem.getSequence(theFile));
        sequencer.start();
    }

    private File[] getFiles() {
        File file = new File(midiDirectory);
        return file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".mid");
            }
        });
    }


    public void stop() {
        sequencer.stop();
        run = false;
    }


}
