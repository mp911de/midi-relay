package de.paluch.midi.relay.midi;

import javax.sound.midi.MidiMessage;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 30.11.13 17:50
 */
public class MidiMessageDetail {
    byte[] bytes;
    byte message[] = null;
    int t1 = 0;
    int t2 = 0;
    int t3 = 0;
    byte hi;
    byte lo;

    private MidiMessage midiMessage;

    public MidiMessageDetail(MidiMessage midiMessage) {

        bytes = midiMessage.getMessage();
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
    }

    public byte[] getBytes() {
        return bytes;
    }

    public byte[] getMessage() {
        return message;
    }

    public int getT1() {
        return t1;
    }

    public int getT2() {
        return t2;
    }

    public int getT3() {
        return t3;
    }

    public byte getHi() {
        return hi;
    }

    public byte getLo() {
        return lo;
    }

    public MidiMessage getMidiMessage() {
        return midiMessage;
    }
}
