package de.paluch.midi.relay.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author <a href="mailto:mark.paluch@1und1.de">Mark Paluch</a>
 * @since 09.11.12 19:22
 */
@XmlRootElement(name = "midiRelayConfiguration")
@XmlAccessorType(XmlAccessType.FIELD)
public class MidiRelayConfiguration {
    private String timerCronExpression;
    private String midiDirectory;
    private String ethrlyHostname;
    private int ethrlyPort;

    private List<MidiChannelMap> channel;

    public String getTimerCronExpression() {
        return timerCronExpression;
    }

    public void setTimerCronExpression(String timerCronExpression) {
        this.timerCronExpression = timerCronExpression;
    }

    public String getMidiDirectory() {
        return midiDirectory;
    }

    public void setMidiDirectory(String midiDirectory) {
        this.midiDirectory = midiDirectory;
    }

    public String getEthrlyHostname() {
        return ethrlyHostname;
    }

    public void setEthrlyHostname(String ethrlyHostname) {
        this.ethrlyHostname = ethrlyHostname;
    }

    public int getEthrlyPort() {
        return ethrlyPort;
    }

    public void setEthrlyPort(int ethrlyPort) {
        this.ethrlyPort = ethrlyPort;
    }

    public List<MidiChannelMap> getChannel() {
        return channel;
    }

    public void setChannel(List<MidiChannelMap> channel) {
        this.channel = channel;
    }
}
