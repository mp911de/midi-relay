package de.paluch.midi.relay.midi;

import de.paluch.midi.relay.config.MidiChannelMap;
import de.paluch.midi.relay.relay.RemoteRelayReceiver;
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
public class MidiRelayReceiver implements Receiver
{

    private Logger log = Logger.getLogger(getClass());
    private Map<String, Integer> channelMap = new HashMap<String, Integer>();
    private RemoteRelayReceiver remoteRelayReceiver;

    public final static int ALL_ON = 100;
    public final static int ALL_OFF = 110;

    public MidiRelayReceiver(RemoteRelayReceiver remoteRelayReceiver)
    {
        this.remoteRelayReceiver = remoteRelayReceiver;
    }

    @Override
    public void send(MidiMessage message, long timeStamp)
    {

        if (!(message instanceof MetaMessage))
        {
            processMessage(message);
        }

    }

    private void processMessage(MidiMessage mm)
    {

        MidiMessageDetail detail = new MidiMessageDetail(mm);

        int t1 = detail.getT1();
        int t2 = detail.getT2();
        int t3 = detail.getT3();
        byte hi = detail.getHi();
        byte lo = detail.getLo();

        if (lo == 9)
        {
            // drum channel, we skip that guy.
            return;
        }
        if (hi == MidiHelper.NOTE_ON || hi == MidiHelper.NOTE_OFF)
        {

            int noteValue = MidiHelper.getNoteValue(t2);
            char relNote = (char) (MidiHelper.OFFSET_CHAR + (noteValue));
            String theNote = MidiHelper.getNote(t2).toUpperCase();

            Integer outputChannel = channelMap.get(theNote);
            if (outputChannel != null)
            {
                if (hi == MidiHelper.NOTE_ON && t3 > 2)
                {
                    remoteRelayReceiver.on(outputChannel);
                } else if (hi == MidiHelper.NOTE_OFF || (hi == MidiHelper.NOTE_ON && t3 <= 2))
                {
                    remoteRelayReceiver.off(outputChannel);
                }
            }
        }

    }

    @Override
    public void close()
    {
        remoteRelayReceiver.close();
    }

    public void setChannelMap(List<MidiChannelMap> channels)
    {
        for (MidiChannelMap midiChannelMap : channels)
        {
            for (String note : midiChannelMap.getNote())
            {
                channelMap.put(note, midiChannelMap.getChannel());
            }
        }
    }
}
