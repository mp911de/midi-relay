package de.paluch.midi.relay.config;

import java.net.URL;
import java.util.List;

import javax.xml.bind.JAXB;

import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * @author <a href="mailto:mark.paluch@1und1.de">Mark Paluch</a>
 * @since 02.12.12 13:49
 */
public class ChannelMapFactory extends AbstractFactoryBean<List<MidiChannelMap>> {
    private URL configUrl;

    @Override
    public Class<?> getObjectType() {
        return List.class;
    }

    @Override
    protected List<MidiChannelMap> createInstance() throws Exception {

        MidiRelayConfiguration config = JAXB.unmarshal(configUrl, MidiRelayConfiguration.class);

        return config.getChannel();
    }

    public URL getConfigUrl() {
        return configUrl;
    }

    public void setConfigUrl(URL configUrl) {
        this.configUrl = configUrl;
    }
}
