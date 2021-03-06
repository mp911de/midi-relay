package de.paluch.midi.relay.http;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 12.12.13 19:53
 */
@XmlRootElement(name = "midiDeviceInfos")
@XmlAccessorType(XmlAccessType.NONE)
@Data
public class MidiDeviceInfosRepresentation {

    @XmlElement(name = "device")
    List<MidiDeviceInfoRepresentation> devices = new ArrayList<>();
}
