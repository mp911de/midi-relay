package de.paluch.midi.relay.midi;

import de.paluch.midi.relay.config.MidiChannelMap;
import de.paluch.midi.relay.relay.ETHRLY16;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.AbstractFactoryBean;

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

    private ETHRLY16 ethrly16;

    private List<MidiChannelMap> channelMap;


    @Override
    public Class<?> getObjectType() {
        return Sequencer.class;
    }

    @Override
    protected Sequencer createInstance() throws Exception {


        MidiRelayReceiver midiRelayReceiver = new MidiRelayReceiver(ethrly16);
        midiRelayReceiver.setChannelMap(channelMap);

        Sequencer seq = MidiSystem.getSequencer(false);
        Transmitter transmitter = seq.getTransmitter();
        MultiTargetReceiver multiTargetReceiver = MidiInstance.getInstance().getReceiver();
        Receiver systemReceiver = MidiSystem.getReceiver();

        transmitter.setReceiver(multiTargetReceiver);

        multiTargetReceiver.addReceiver(systemReceiver);
        multiTargetReceiver.addReceiver(midiRelayReceiver);

        seq.open();

        return seq;
    }

    @Override
    protected void destroyInstance(Sequencer instance) throws Exception {

        if (instance.isOpen()) {
            instance.close();
        }
    }


    public ETHRLY16 getEthrly16() {
        return ethrly16;
    }

    public void setEthrly16(ETHRLY16 ethrly16) {
        this.ethrly16 = ethrly16;
    }

    public List<MidiChannelMap> getChannelMap() {
        return channelMap;
    }

    public void setChannelMap(List<MidiChannelMap> channelMap) {
        this.channelMap = channelMap;
    }
}
