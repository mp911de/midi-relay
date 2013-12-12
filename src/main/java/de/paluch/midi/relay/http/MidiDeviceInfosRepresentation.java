package de.paluch.midi.relay.http;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 12.12.13 19:53
 */
@XmlRootElement(name = "midiDeviceInfos")
@XmlAccessorType(XmlAccessType.NONE)
public class MidiDeviceInfosRepresentation
{
    @XmlElement(name = "device")
    private List<MidiDeviceInfoRepresentation> devices = new ArrayList();

    public List<MidiDeviceInfoRepresentation> getDevices()
    {
        return devices;
    }
    public void setDevices(List<MidiDeviceInfoRepresentation> devices)
    {
        this.devices = devices;
    }
}
