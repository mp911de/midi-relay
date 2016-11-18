package de.paluch.midi.relay.midi;

import lombok.Data;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 30.11.13 17:42
 */
@Data
public class PlayerState {

    String id;
    String sequenceName;
    String fileName;
    int duration;
    long started;
    byte[] midiContents;
    boolean running;
    boolean errorState;
    String exceptionMessage;
}
