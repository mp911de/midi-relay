package de.paluch.midi.relay.midi;

/**
 * @author <a href="mailto:mark.paluch@1und1.de">Mark Paluch</a>
 * @since 08.11.12 19:57
 */
public class MidiHelper {

    public static int NOTE_ON = 9;
       public static int NOTE_OFF = 8;
       public static int PGM_CHANGE = 192;
       public static char OFFSET_CHAR = 'N';

       private static String NOTENAMES[];


       static {
           NOTENAMES = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
       }

    /**
     * Numerischen Notenwert ermitteln.
     *
     * @param note
     * @return
     */

    public static int getNoteValue(int note) {

        int oct = getOctave(note) * 12;
        note = note - oct;
        return note;

    }

    /**
     * Noten-Name ermitteln.
     *
     * @param note
     * @return
     */
    public static String getNote(int note) {

        return NOTENAMES[getNoteValue(note)];
    }

    /**
     * Oktave ermitteln.
     *
     * @param note
     * @return
     */
    public static int getOctave(int note) {

        int oct = (int) (note / 12d);
        return oct;
    }
}
