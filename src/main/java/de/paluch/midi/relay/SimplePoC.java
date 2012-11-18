package de.paluch.midi.relay;

import de.paluch.midi.relay.midi.MidiHelper;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import java.net.URL;

/**
 * @author <a href="mailto:mark.paluch@1und1.de">Mark Paluch</a>
 * @since 18.11.12 19:57
 */
public class SimplePoC {

    public static void main(String[] args) throws Exception {

        Receiver receiver = new Receiver() {
            @Override
            public void send(MidiMessage mm, long timeStamp) {
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

                    System.out.println("Note: " + t2);
                    System.out.println("Velocity: " + t3);
                }

            }

            @Override
            public void close() {
            }
        };


        Sequencer s = MidiSystem.getSequencer(false);
        s.getTransmitter().setReceiver(receiver);
        s.open();
        s.setSequence(MidiSystem.getSequence(new URL("http://www.bluegrassbanjo.org/buffgals.mid")));
        s.start();

    }

}
