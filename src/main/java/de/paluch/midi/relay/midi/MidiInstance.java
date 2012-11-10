package de.paluch.midi.relay.midi;

import javax.sound.midi.Sequencer;

/**
 * @author <a href="mailto:mark.paluch@1und1.de">Mark Paluch</a>
 * @since 09.11.12 19:44
 */
public class MidiInstance {

    private Sequencer sequencer;
    private MultiTargetReceiver receiver = new MultiTargetReceiver();

    private static MidiInstance instance = new MidiInstance();

    public static MidiInstance getInstance() {
        return instance;
    }

    public Sequencer getSequencer() {
        return sequencer;
    }

    public void setSequencer(Sequencer sequencer) {
        this.sequencer = sequencer;
    }

    public MultiTargetReceiver getReceiver() {
        return receiver;
    }
}
