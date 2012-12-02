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
import de.paluch.midi.relay.job.SwitchOffJob;
import de.paluch.midi.relay.job.SwitchOnJob;
import de.paluch.midi.relay.midi.MidiInstance;
import de.paluch.midi.relay.midi.MidiPlayer;
import de.paluch.midi.relay.midi.MidiRelayReceiver;
import de.paluch.midi.relay.midi.MultiTargetReceiver;
import de.paluch.midi.relay.relay.ETHRLY16;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

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

    private RsApplication rsApplication;
    private AbstractXmlApplicationContext context;

    private boolean active = true;
    private boolean shutdown = false;

    private Server(String configFile) throws Exception {


        context = new ClassPathXmlApplicationContext("spring-ctx.xml");
        HttpControlInterface http = (HttpControlInterface) context.getBean("http");

        rsApplication = new RsApplication();
        rsApplication.setObjects((Set) Collections.singleton(http));

    }


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
        System.out.println("http://localhost:9595/player/port/{port:0-8}/{state:ON|OFF} to control port state");

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

        context.close();
    }


    public static Server getInstance() {
        return instance;
    }

}
