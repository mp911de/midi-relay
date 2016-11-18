package de.paluch.midi.relay.http;

import lombok.Data;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 01.12.13 10:27
 */
@Data
public class PlayerStateTrackRepresentation {

    String id;
    String sequenceName;
    String fileName;
    int duration;
    boolean errorState;
    String exceptionMessage;
}
