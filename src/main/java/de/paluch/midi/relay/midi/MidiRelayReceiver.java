package de.paluch.midi.relay.midi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;

import de.paluch.midi.relay.config.MidiChannelMap;
import de.paluch.midi.relay.relay.RemoteRelayReceiver;
import de.paluch.midi.relay.relay.RoutingRelayReceiver;
import lombok.extern.slf4j.Slf4j;

/**
 * @author <a href="mailto:mark.paluch@1und1.de">Mark Paluch</a>
 * @since 08.11.12 19:59
 */
@Slf4j
public class MidiRelayReceiver implements Receiver {

    final Map<String, Integer> channelMap = new HashMap<String, Integer>();
    final RemoteRelayReceiver remoteRelayReceiver;
    WorkQueueExecutor workQueueExecutor;

    public MidiRelayReceiver(RemoteRelayReceiver remoteRelayReceiver) {
        this.remoteRelayReceiver = remoteRelayReceiver;
    }

    public void setWorkQueueExecutor(WorkQueueExecutor workQueueExecutor) {
        this.workQueueExecutor = workQueueExecutor;
        workQueueExecutor.setCallback(this::processMessage);
    }

    @Override
    public void send(MidiMessage message, long timeStamp) {

        try {
            if (message instanceof MetaMessage) {
                return;
            }

            if (workQueueExecutor == null) {
                processMessage(message);
            } else {
                workQueueExecutor.submit(message);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    private void processMessage(MidiMessage mm) {

        MidiMessageDetail detail = new MidiMessageDetail(mm);

        int t1 = detail.getT1();
        int t2 = detail.getT2();
        int t3 = detail.getT3();
        byte hi = detail.getHi();
        byte lo = detail.getLo();

        if (lo == 9) {
            // drum channel, we skip that guy.
            return;
        }

        if (hi == MidiHelper.NOTE_ON || hi == MidiHelper.NOTE_OFF) {

            int noteValue = MidiHelper.getNoteValue(t2);
            char relNote = (char) (MidiHelper.OFFSET_CHAR + (noteValue));
            String theNote = MidiHelper.getNote(t2).toUpperCase();

            Integer outputChannel = channelMap.get(theNote);
            if (outputChannel == null) {
                return;
            }

            if (hi == MidiHelper.NOTE_ON && t3 > 2) {
                if (remoteRelayReceiver instanceof RoutingRelayReceiver) {
                    RoutingRelayReceiver routingRelayReceiverImpl = (RoutingRelayReceiver) remoteRelayReceiver;
                    routingRelayReceiverImpl.on(theNote);
                } else {
                    remoteRelayReceiver.on(outputChannel);
                }
            } else if (hi == MidiHelper.NOTE_OFF || (hi == MidiHelper.NOTE_ON && t3 <= 2)) {
                if (remoteRelayReceiver instanceof RoutingRelayReceiver) {
                    RoutingRelayReceiver routingRelayReceiverImpl = (RoutingRelayReceiver) remoteRelayReceiver;
                    routingRelayReceiverImpl.off(theNote);
                } else {
                    remoteRelayReceiver.off(outputChannel);
                }
            }
        }

    }

    @Override
    public void close() {
        remoteRelayReceiver.close();
    }

    public void setChannelMap(List<MidiChannelMap> channels) {

        for (MidiChannelMap midiChannelMap : channels) {
            if (midiChannelMap.getNote() == null) {
                continue;
            }

            for (String note : midiChannelMap.getNote()) {
                channelMap.put(note, midiChannelMap.getChannel());
            }
        }
    }
}
