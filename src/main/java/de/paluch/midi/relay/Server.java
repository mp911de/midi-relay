package de.paluch.midi.relay;

import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.jersey.api.core.ApplicationAdapter;
import com.sun.jersey.core.impl.provider.entity.StringProvider;
import com.sun.net.httpserver.HttpServer;
import de.paluch.midi.relay.config.MidiRelayConfiguration;
import de.paluch.midi.relay.http.HttpControlInterface;
import de.paluch.midi.relay.http.RsApplication;
import de.paluch.midi.relay.job.ConnectionWatchdogJob;
import de.paluch.midi.relay.job.PlayJob;
import de.paluch.midi.relay.midi.MidiInstance;
import de.paluch.midi.relay.midi.MidiPlayer;
import de.paluch.midi.relay.midi.MidiRelayReceiver;
import de.paluch.midi.relay.midi.MultiTargetReceiver;
import de.paluch.midi.relay.relay.ETHRLY16;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import javax.sound.midi.*;
import javax.xml.bind.JAXB;
import java.io.File;
import java.util.Collections;
import java.util.Set;

/**
 * Server Initiator.
 */
public class Server {


    private static Server instance;

    private ETHRLY16 ethrly16;
    private RsApplication rsApplication;

    private boolean active = true;
    private boolean shutdown = false;


    public static void main(String args[]) throws Exception {

        if (args.length == 0) {
            System.out.println("Usage: Server CONFIG FILE NAME");
            return;
        }
        System.out.println("using config " + args[0]);

        instance = new Server(args[0]);
        instance.installShutdownHook();
        instance.run();
        instance.close();
    }

    private void installShutdownHook() {

        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                instance.active = false;

                System.out.println("Stopping server");
                try {
                    while (!instance.shutdown) {
                        Thread.sleep(100);
                    }
                } catch (InterruptedException e) {

                }
            }
        });
    }

    private void run() throws Exception {


        ApplicationAdapter adapter = new ApplicationAdapter(rsApplication);
        adapter.getProviderClasses().add(StringProvider.class);


        HttpServer server = HttpServerFactory.create("http://localhost:9595/", adapter);
        server.start();

        System.out.println("Server running");
        System.out.println("Visit:");
        System.out.println("http://localhost:9595/player/play to play");
        System.out.println("http://localhost:9595/player/stop to stop");
        System.out.println("http://localhost:9595/player/ to get the current state (running/stopped)");
        System.out.println("http://localhost:9595/player/device?id&state set MIDI receiver state");
        System.out.println("http://localhost:9595/player/schedule?cronExpression to add a schedule");

        while (active) {
            Thread.sleep(500);
        }


        server.stop(0);
        shutdown = true;
        System.out.println("Server stopped");
    }

    private void close() {

        MidiInstance.getInstance().getReceiver().close();
        MidiInstance.getInstance().getSequencer().close();
    }

    private Server(String configFile) throws Exception {

        MidiRelayConfiguration config = JAXB.unmarshal(new File(configFile), MidiRelayConfiguration.class);
        ethrly16 = new ETHRLY16(config.getEthrlyHostname(), config.getEthrlyPort());

        Sequencer seq = setupSequencer(config);
        MidiInstance.getInstance().setSequencer(seq);


        MidiPlayer midiPlayer = new MidiPlayer(config.getMidiDirectory(), seq, ethrly16);


        Scheduler scheduler = new StdSchedulerFactory().getScheduler();
        addMidiPlayerJob(midiPlayer, scheduler);
        addConnectionWatchdogJob(ethrly16, scheduler);

        HttpControlInterface hci = setupControlInterface(midiPlayer, scheduler);

        rsApplication = new RsApplication();
        rsApplication.setObjects((Set) Collections.singleton(hci));
        hci.addSchedule(config.getTimerCronExpression());

        ethrly16.on(0);

        scheduler.start();

    }

    private HttpControlInterface setupControlInterface(MidiPlayer midiPlayer, Scheduler scheduler) {
        HttpControlInterface hci = new HttpControlInterface();
        hci.setScheduler(scheduler);
        hci.setMidiPlayer(midiPlayer);
        hci.setMidiReceiver(ethrly16);
        return hci;
    }

    private Sequencer setupSequencer(MidiRelayConfiguration config) throws MidiUnavailableException {
        MidiRelayReceiver midiRelayReceiver = new MidiRelayReceiver(ethrly16);
        midiRelayReceiver.setChannelMap(config.getChannel());


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

    private void addMidiPlayerJob(MidiPlayer midiPlayer, Scheduler scheduler) throws SchedulerException {
        JobDetail detail = JobBuilder.newJob(PlayJob.class).withIdentity(PlayJob.class.getName()).build();
        detail.getJobDataMap().put("midiPlayer", midiPlayer);
        scheduler.addJob(detail, true);
    }

    private void addConnectionWatchdogJob(ETHRLY16 ethrly16, Scheduler scheduler) throws SchedulerException {
        JobDetail detail = JobBuilder.newJob(ConnectionWatchdogJob.class).withIdentity(ConnectionWatchdogJob.class.getName())
                                     .build();
        detail.getJobDataMap().put("ethrly16", ethrly16);
        scheduler.addJob(detail, true);
    }

    public static Server getInstance() {
        return instance;
    }

}
