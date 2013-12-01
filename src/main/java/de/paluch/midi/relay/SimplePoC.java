package de.paluch.midi.relay;

import com.google.common.io.Files;
import de.paluch.midi.relay.midi.MidiHelper;
import de.paluch.midi.relay.midi.MidiMessageDetail;
import de.paluch.midi.relay.midi.PlayerState;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Track;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;

/**
 * @author <a href="mailto:mark.paluch@1und1.de">Mark Paluch</a>
 * @since 18.11.12 19:57
 */
public class SimplePoC
{

    public static void main(String[] args) throws Exception
    {

        Sequencer sequencer = MidiSystem.getSequencer();
        Sequence sequence = MidiSystem.getSequence(new File("/Users/mark/midi/P-ding_dong_merrily_on_high.mid"));
        int durationInSecs = (int) (sequencer.getMicrosecondLength() / 1000000.0);
        sequencer.setSequence(sequence);

        for (Track track : sequence.getTracks())
        {
            for (int i = 0; i < track.size(); i++)
            {
                MidiEvent midiEvent = track.get(i);

                if (midiEvent.getMessage() instanceof MetaMessage)
                {
                    MetaMessage mm = (MetaMessage) midiEvent.getMessage();

                    MidiMessageDetail detail = new MidiMessageDetail(midiEvent.getMessage());

                    String s = new String(mm.getData());
                                            System.out.println(s);
                    if (detail.getT2() == 3)
                    {
                         s = new String(mm.getData());
                        //System.out.println(s);
                    }
                }

            }
        }

    }

}
