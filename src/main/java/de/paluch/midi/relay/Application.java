/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.paluch.midi.relay;

import java.net.URL;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequencer;
import javax.xml.bind.JAXB;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.event.EventListener;

import de.paluch.midi.relay.config.MidiRelayConfiguration;
import de.paluch.midi.relay.midi.MidiInstance;
import de.paluch.midi.relay.midi.MidiPlayer;
import de.paluch.midi.relay.midi.SequencerFactory;
import de.paluch.midi.relay.midi.WorkQueueExecutor;
import de.paluch.midi.relay.relay.LoggingRelayReceiver;
import de.paluch.midi.relay.relay.RemoteRelayReceiver;
import de.paluch.midi.relay.relay.RoutingRelayReceiverImpl;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@ImportResource("classpath:META-INF/applicationContext.xml")
@Slf4j
public class Application {

    @Value("${server.port}")
    int port;

    public static void main(String[] args) {

        MidiDevice.Info[] devices = MidiSystem.getMidiDeviceInfo();
        log.info("Available Devices");
        for (MidiDevice.Info device : devices) {
            log.info("   " + device.getName() + "/" + device.getDescription() + "/" + device.getVendor());
        }

        SpringApplication.run(Application.class, args);
    }

    @EventListener
    public void onStartup(EmbeddedServletContainerInitializedEvent event) {

        log.info("Server running");
        log.info("Visit:");

        String hostPart = RuntimeContainer.FQDN_HOSTNAME + ":" + port;

        log.info("* GET http://" + hostPart + "/player/ to retrieve the current status");
        log.info("* GET http://" + hostPart + "/player/play to play");
        log.info("* PUT http://" + hostPart + "/player/play to play uploaded midi data");
        log.info("* GET http://" + hostPart + "/player/stop to stop");
        log.info("* GET http://" + hostPart + "/player/ to get the current state (running/stopped)");
        log.info("* GET http://" + hostPart + "/player/port/{port:0-8}/{state:ON|OFF} to control port state");
        log.info("* GET http://" + hostPart + "/player/devices get a list of all devices (xml format)");
        log.info("* GET http://" + hostPart + "/player/devices/{id}/to/relay to connect an in-device to the out device (relay)");
    }

    @Bean("log")
    LoggingRelayReceiver loggingRelayReceiver() {
        return new LoggingRelayReceiver();
    }

    @Bean
    MidiRelayConfiguration channelMapFactory(@Value("${midiMapping}") URL resource) throws Exception {
        return JAXB.unmarshal(resource, MidiRelayConfiguration.class);
    }

    @Bean
    WorkQueueExecutor workQueueExecutor() {

        WorkQueueExecutor executor = new WorkQueueExecutor();

        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {

                if (MidiInstance.getInstance().getWithRelay() != null) {
                    MidiInstance.getInstance().getWithRelay().close();
                }

                if (MidiInstance.getInstance().getWithSound() != null) {
                    MidiInstance.getInstance().getWithSound().close();
                }

                if (MidiInstance.getInstance().getSequencer() != null) {
                    MidiInstance.getInstance().getSequencer().close();
                }

                executor.shutdown();

                log.info("Stopping server");
                try {
                    executor.join();
                } catch (InterruptedException e) {

                }
            }
        });

        executor.start();
        return executor;
    }

    @Bean
    SequencerFactory sequencerFactory(@Value("${device.filter:}") String deviceFilter,
            @Value("${device.system-synth:}") String systemSynth, RemoteRelayReceiver remoteRelayReceiver,
            MidiRelayConfiguration midiRelayConfiguration, WorkQueueExecutor workQueueExecutor) {

        SequencerFactory sequencerFactory = new SequencerFactory();
        sequencerFactory.setDeviceFilter(deviceFilter);
        sequencerFactory.setSystemSynth(systemSynth);
        sequencerFactory.setRemoteRelayReceiver(remoteRelayReceiver);
        sequencerFactory.setWorkQueueExecutor(workQueueExecutor);
        sequencerFactory.setChannelMap(midiRelayConfiguration.getChannel());

        return sequencerFactory;
    }

    @Bean
    RoutingRelayReceiverImpl routingRelayReceiver(MidiRelayConfiguration midiRelayConfiguration) {
        return new RoutingRelayReceiverImpl(midiRelayConfiguration.getChannel());
    }

    @Bean
    MidiPlayer midiPlayer(@Value("${midiDirectory}") String midiDirectory, Sequencer sequencer,
            RemoteRelayReceiver remoteRelayReceiver) {
        return new MidiPlayer(midiDirectory, sequencer, remoteRelayReceiver);
    }
}
