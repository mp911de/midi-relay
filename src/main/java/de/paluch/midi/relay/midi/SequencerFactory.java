package de.paluch.midi.relay.midi;

import de.paluch.midi.relay.config.MidiChannelMap;
import de.paluch.midi.relay.relay.RemoteRelayReceiver;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.util.StringUtils;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Transmitter;
import java.util.List;

/**
 * @author <a href="mailto:mark.paluch@1und1.de">Mark Paluch</a>
 * @since 02.12.12 13:44
 */
public class SequencerFactory extends AbstractFactoryBean<Sequencer> implements DisposableBean {
    private Logger log = Logger.getLogger(getClass());
    private RemoteRelayReceiver remoteRelayReceiver;
    private String deviceFilter = null;
    private WorkQueueExecutor workQueueExecutor;

    private List<MidiChannelMap> channelMap;

    @Override
    public Class<?> getObjectType() {
        return Sequencer.class;
    }

    @Override
    protected Sequencer createInstance() throws Exception {

        MidiRelayReceiver midiRelayReceiver = new MidiRelayReceiver(remoteRelayReceiver);
        midiRelayReceiver.setChannelMap(channelMap);
        midiRelayReceiver.setWorkQueueExecutor(workQueueExecutor);

        Sequencer seq = MidiSystem.getSequencer(false);
        Transmitter transmitter = seq.getTransmitter();
        MultiTargetReceiver multiTargetReceiver = MidiInstance.getInstance().getReceiver();

        transmitter.setReceiver(multiTargetReceiver);

        multiTargetReceiver.addReceiver(midiRelayReceiver);

        if (!StringUtils.hasText(deviceFilter)) {
            Receiver systemReceiver = MidiSystem.getReceiver();
            multiTargetReceiver.addReceiver(systemReceiver);

        } else {
            MidiDevice.Info[] midiDeviceInfo = MidiSystem.getMidiDeviceInfo();
            for (MidiDevice.Info info : midiDeviceInfo) {
                if (deviceFilter.contains(info.getName())) {
                    MidiDevice midiDevice = MidiSystem.getMidiDevice(info);
                    if (midiDevice instanceof Receiver) {
                        log.info("Adding receiver" + info.getName());

                        multiTargetReceiver.addReceiver((Receiver) midiDevice);

                    } else {
                        log.warn("Device " + info.getName() + " is not a receiver.");
                    }
                }
            }
        }

        seq.open();
        MidiInstance.getInstance().setSequencer(seq);

        return seq;
    }

    @Override
    protected void destroyInstance(Sequencer instance) throws Exception {

        if (instance.isOpen()) {
            instance.close();
        }
    }

    public RemoteRelayReceiver getRemoteRelayReceiver() {
        return remoteRelayReceiver;
    }

    public void setRemoteRelayReceiver(RemoteRelayReceiver remoteRelayReceiver) {
        this.remoteRelayReceiver = remoteRelayReceiver;
    }

    public List<MidiChannelMap> getChannelMap() {
        return channelMap;
    }

    public void setChannelMap(List<MidiChannelMap> channelMap) {
        this.channelMap = channelMap;
    }

    public String getDeviceFilter() {
        return deviceFilter;
    }

    public void setDeviceFilter(String deviceFilter) {
        this.deviceFilter = deviceFilter;
    }

    public WorkQueueExecutor getWorkQueueExecutor() {
        return workQueueExecutor;
    }

    public void setWorkQueueExecutor(WorkQueueExecutor workQueueExecutor) {
        this.workQueueExecutor = workQueueExecutor;
    }
}
