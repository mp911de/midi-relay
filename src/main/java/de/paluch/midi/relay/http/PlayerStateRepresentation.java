package de.paluch.midi.relay.http;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 30.11.13 19:27
 */
@Data
@XmlRootElement
public class PlayerStateRepresentation {

    boolean running;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    Date startedTime;
    Date started;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    Date estimatedEndTime;
    Date estimatedEnd;

    int estimatedSecondsToPlay;

    PlayerStateTrackRepresentation track;

    public void setStarted(Date started) {
        this.startedTime = started;
        this.started = started;
    }

    public void setEstimatedEnd(Date estimatedEnd) {
        this.estimatedEndTime = estimatedEnd;
        this.estimatedEnd = estimatedEnd;
    }

}
