package de.paluch.midi.relay.relay;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.paluch.midi.relay.config.MidiChannelMap;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class RoutingRelayReceiverImpl implements RoutingRelayReceiver, RemoteRelayReceiver, ApplicationContextAware,
        InitializingBean {

    private Logger log = Logger.getLogger(getClass());
    private List<MidiChannelMap> channelMap = Lists.newArrayList();
    private Map<String, RemoteRelayReceiver> devices = Maps.newHashMap();
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (devices.isEmpty()) {

            String[] names = applicationContext.getBeanNamesForType(RemoteRelayReceiver.class);
            for (String name : names) {
                RemoteRelayReceiver relayReceiver = (RemoteRelayReceiver) applicationContext.getBean(name);
                if (relayReceiver instanceof RoutingRelayReceiver) {
                    continue;
                }

                devices.put(name, relayReceiver);
            }
        }

    }

    @Override
    public void close() {
        for (RemoteRelayReceiver routingRelayReceiver : devices.values()) {
            routingRelayReceiver.close();
        }

    }

    @Override
    public void keepaliveOrClose() {
        for (RemoteRelayReceiver routingRelayReceiver : devices.values()) {
            routingRelayReceiver.keepaliveOrClose();
        }
    }

    @Override
    public void on(int port) {
        for (RemoteRelayReceiver routingRelayReceiver : devices.values()) {
            routingRelayReceiver.on(port);
        }
    }

    @Override
    public void off(int port) {
        for (RemoteRelayReceiver routingRelayReceiver : devices.values()) {
            routingRelayReceiver.off(port);
        }
    }

    @Override
    public long getBytesSent() {
        long sum = 0;

        for (RemoteRelayReceiver routingRelayReceiver : devices.values()) {
            sum += routingRelayReceiver.getBytesSent();
        }
        return sum;
    }

    public void on(String note) {

        for (MidiChannelMap midiChannelMap : channelMap) {
            if (midiChannelMap.getNote().contains(note)) {
                RemoteRelayReceiver routingRelayReceiver = devices.get(midiChannelMap.getDevice());
                if (routingRelayReceiver != null)
                {
                    routingRelayReceiver.on(midiChannelMap.getChannel());
                } else {
                    log.warn("Cannot resolve device " + midiChannelMap.getDevice());
                }
            }
        }

    }

    public void off(String note) {
        for (MidiChannelMap midiChannelMap : channelMap) {
            if (midiChannelMap.getNote().contains(note)) {
                RemoteRelayReceiver routingRelayReceiver = devices.get(midiChannelMap.getDevice());
                if (routingRelayReceiver != null) {
                    routingRelayReceiver.off(midiChannelMap.getChannel());
                } else {
                    log.warn("Cannot resolve device " + midiChannelMap.getDevice());
                }
            }
        }
    }

    public List<MidiChannelMap> getChannelMap() {
        return channelMap;
    }

    public void setChannelMap(List<MidiChannelMap> channelMap) {
        this.channelMap = channelMap;
    }

    public Map<String, RemoteRelayReceiver> getDevices() {
        return devices;
    }

    public void setDevices(Map<String, RemoteRelayReceiver> devices) {
        this.devices = devices;
    }
}
