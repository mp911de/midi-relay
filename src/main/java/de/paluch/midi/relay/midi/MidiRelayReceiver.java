package de.paluch.midi.relay.midi;

import de.paluch.midi.relay.relay.ETHRLY16;
import de.paluch.midi.relay.config.MidiChannelMap;
import org.apache.log4j.Logger;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:mark.paluch@1und1.de">Mark Paluch</a>
 * @since 08.11.12 19:59
 */
public class MidiRelayReceiver implements Receiver {


    private Logger log = Logger.getLogger(getClass());
    private Map<String, Integer> channelMap = new HashMap<String, Integer>();
    private ETHRLY16 ethrly16;


    public final static int ALL_ON = 100;
    public final static int ALL_OFF = 110;

    public MidiRelayReceiver(ETHRLY16 ethrly16) {
        this.ethrly16 = ethrly16;
    }

    @Override
    public void send(MidiMessage message, long timeStamp) {


        if (!(message instanceof MetaMessage)) {
            processMessage(message);
        }


    }

    private void processMessage(MidiMessage mm) {

        byte[] bytes = mm.getMessage();
        byte message[] = null;
        int t1 = 0;
        int t2 = 0;
        int t3 = 0;
        byte hi;
        byte lo;

        if (bytes.length > 1) {
            t1 = bytes[0];
            t2 = bytes[1];

            if (bytes.length > 2) {
                t3 = bytes[2];

                message = new byte[bytes.length - 3];
                if (bytes.length >= 3) {
                    System.arraycopy(bytes, 3, message, 0, message.length);
                }
            }
        }


        hi = (byte) ((t1 & 0xF0) >> 4);
        lo = (byte) (t1 & 0x0F);


        if (lo == 9) {
            // drum channel, we skip that guy.
            return;
        }
        if (hi == MidiHelper.NOTE_ON || hi == MidiHelper.NOTE_OFF) {


            int noteValue = MidiHelper.getNoteValue(t2);
            char relNote = (char) (MidiHelper.OFFSET_CHAR + (noteValue));
            String theNote = MidiHelper.getNote(t2).toUpperCase();

            Integer outputChannel = channelMap.get(theNote);
            if (outputChannel != null) {
                if (hi == MidiHelper.NOTE_ON && t3 > 10) {
                    ethrly16.on(outputChannel);
                } else if (hi == MidiHelper.NOTE_OFF || (hi == MidiHelper.NOTE_ON && t3 <= 10)) {
                    ethrly16.off(outputChannel);
                }
            }
        }


    }


    @Override
    public void close() {
        ethrly16.close();
    }

    public void setChannelMap(List<MidiChannelMap> channels) {
        for (MidiChannelMap midiChannelMap : channels) {
            for (String note : midiChannelMap.getNote()) {
                channelMap.put(note, midiChannelMap.getChannel());
            }
        }
    }
}
