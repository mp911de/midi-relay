package de.paluch.midi.relay.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import lombok.Data;

/**
 * @author <a href="mailto:mark.paluch@1und1.de">Mark Paluch</a>
 * @since 09.11.12 19:42
 */
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class MidiChannelMap {

    @XmlAttribute(name = "id")
    int channel;

    @XmlAttribute(name = "device")
    String device;

    List<String> note = new ArrayList<>();
}
