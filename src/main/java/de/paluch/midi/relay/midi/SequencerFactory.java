package de.paluch.midi.relay.midi;

import java.util.List;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Transmitter;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.util.StringUtils;

import de.paluch.midi.relay.config.MidiChannelMap;
import de.paluch.midi.relay.relay.RemoteRelayReceiver;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author <a href="mailto:mark.paluch@1und1.de">Mark Paluch</a>
 * @since 02.12.12 13:44
 */
@Setter
@Slf4j
public class SequencerFactory extends AbstractFactoryBean<Sequencer> implements DisposableBean {

    private RemoteRelayReceiver remoteRelayReceiver;
    private String deviceFilter = null;
    private String systemSynth = null;
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
        MultiTargetReceiver withSound = MidiInstance.getInstance().getWithSound();
        MultiTargetReceiver withRelay = MidiInstance.getInstance().getWithRelay();

        MultiTargetReceiver sequencerPlayerGroup = new MultiTargetReceiver();
        withRelay.addReceiver(midiRelayReceiver);

        sequencerPlayerGroup.addReceiver(withRelay);
        sequencerPlayerGroup.addReceiver(withSound);
        transmitter.setReceiver(sequencerPlayerGroup);

        MidiDevice.Info[] midiDeviceInfo = MidiSystem.getMidiDeviceInfo();
        if (StringUtils.hasText(systemSynth)) {
            log.info("Trying to resolve " + systemSynth + " to a system synthesizer");

            for (MidiDevice.Info info : midiDeviceInfo) {
                if (systemSynth.contains(info.getName())) {
                    addDevice(withSound, withSound, info);
                }
            }
        } else {
            withSound.addReceiver(MidiSystem.getReceiver());
        }

        if (StringUtils.hasText(deviceFilter)) {

            log.info("Trying to resolve " + deviceFilter + " to a device output");

            for (MidiDevice.Info info : midiDeviceInfo) {
                if (deviceFilter.contains(info.getName())) {
                    addDevice(withSound, withRelay, info);
                }
            }
        }

        seq.open();
        MidiInstance.getInstance().setSequencer(seq);

        return seq;
    }

    private void addDevice(MultiTargetReceiver withSound, MultiTargetReceiver withRelay, MidiDevice.Info info)
            throws MidiUnavailableException {
        MidiDevice midiDevice = MidiSystem.getMidiDevice(info);

        if (midiDevice.getMaxReceivers() != 0) {
            log.info("Adding receiver " + info.getName());

            if (!midiDevice.isOpen()) {
                midiDevice.open();
            }

            withSound.addReceiver(midiDevice.getReceiver());
        }

        if (midiDevice.getMaxTransmitters() != 0) {
            log.info("Adding transmitter " + info.getName());

            if (!midiDevice.isOpen()) {
                midiDevice.open();
            }

            midiDevice.getTransmitter().setReceiver(withRelay);
        }
    }

    @Override
    protected void destroyInstance(Sequencer instance) throws Exception {

        if (instance.isOpen()) {
            instance.close();
        }
    }
}
