package de.paluch.midi.relay.midi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;

/**
 * @author <a href="mailto:mark.paluch@1und1.de">Mark Paluch</a>
 * @since 09.11.12 19:45
 */
public class MultiTargetReceiver implements Receiver {

    private List<Receiver> receivers = new ArrayList<Receiver>();
    private Map<Receiver, Boolean> activeTarget = new HashMap<Receiver, Boolean>();

    @Override
    public void send(MidiMessage message, long timeStamp) {
        for (Receiver receiver : receivers) {
            receiver.send(message, timeStamp);
        }
    }

    public void addReceiver(Receiver receiver) {
        receivers.add(receiver);
        activeTarget.put(receiver, true);
    }

    public void setActive(int id, boolean state) {
        activeTarget.put(receivers.get(id), state);
    }

    @Override
    public void close() {
        for (Receiver receiver : receivers) {
            receiver.close();
        }
    }
}
