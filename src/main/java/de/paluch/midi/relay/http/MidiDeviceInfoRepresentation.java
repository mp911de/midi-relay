package de.paluch.midi.relay.http;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import lombok.Data;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 12.12.13 19:54
 */
@XmlAccessorType(XmlAccessType.NONE)
@Data
public class MidiDeviceInfoRepresentation {

    @XmlAttribute(name = "id")
    int id;

    @XmlElement(name = "name")
    String name;

    @XmlElementWrapper(name = "types")
    @XmlElement(name = "type")
    List<Type> types = new ArrayList<Type>();

    public enum Type {
        IN, OUT, RECEIVER, SYNTHESIZER, TRANSMITTER, SEQUENCER;
    }
}
