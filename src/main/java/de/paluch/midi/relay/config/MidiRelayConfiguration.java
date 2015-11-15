package de.paluch.midi.relay.config;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author <a href="mailto:mark.paluch@1und1.de">Mark Paluch</a>
 * @since 09.11.12 19:22
 */
@XmlRootElement(name = "midiRelayConfiguration")
@XmlAccessorType(XmlAccessType.FIELD)
public class MidiRelayConfiguration {

    private List<MidiChannelMap> channel;

    public List<MidiChannelMap> getChannel() {
        return channel;
    }

    public void setChannel(List<MidiChannelMap> channel) {
        this.channel = channel;
    }
}
