package de.paluch.midi.relay.config;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

/**
 * @author <a href="mailto:mark.paluch@1und1.de">Mark Paluch</a>
 * @since 09.11.12 19:22
 */
@XmlRootElement(name = "midiRelayConfiguration")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class MidiRelayConfiguration {
    List<MidiChannelMap> channel;
}
