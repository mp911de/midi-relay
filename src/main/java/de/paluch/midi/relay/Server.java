package de.paluch.midi.relay;

import de.paluch.midi.relay.http.HttpControlInterface;
import de.paluch.midi.relay.http.NotFoundExceptionMapper;
import de.paluch.midi.relay.http.RsApplication;
import de.paluch.midi.relay.midi.MidiInstance;
import de.paluch.midi.relay.midi.WorkQueueExecutor;
import org.jboss.resteasy.plugins.server.netty.NettyJaxrsServer;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import java.util.Arrays;
import java.util.HashSet;
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
    private WorkQueueExecutor executor;

    private Server(String configFile) throws Exception {

        context = new ClassPathXmlApplicationContext("spring-ctx.xml");
        HttpControlInterface http = (HttpControlInterface) context.getBean("http");

        rsApplication = new RsApplication();
        rsApplication.setObjects((Set) new HashSet(Arrays.asList(http, new NotFoundExceptionMapper())));
        executor = context.getBean(WorkQueueExecutor.class);
        executor.start();

    }

    public static void main(String args[]) throws Exception {

        if (args.length == 0) {
            System.out.println("Usage: Server CONFIG FILE NAME");
            return;
        }
        System.out.println("using config " + args[0]);

        MidiDevice.Info[] devices = MidiSystem.getMidiDeviceInfo();
        System.out.println("Available Devices");
        for (MidiDevice.Info device : devices) {
            System.out.println("   " + device.getName() + "/" + device.getDescription() + "/" + device.getVendor());
        }

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
                executor.shutdown();

                System.out.println("Stopping server");
                try {
                    executor.join();
                    while (!instance.shutdown) {
                        Thread.sleep(100);
                    }
                } catch (InterruptedException e) {

                }
            }
        });
    }

    private void run() throws Exception {
        ResteasyDeployment deployment = new ResteasyDeployment();
        deployment.setApplication(rsApplication);
        NettyJaxrsServer netty = new NettyJaxrsServer();
        netty.setDeployment(deployment);

        int port = (Integer) context.getBean("serverPort");

        netty.setPort(port);
        netty.setRootResourcePath("");
        netty.setSecurityDomain(null);
        netty.start();

        System.out.println("Server running");
        System.out.println("Visit:");
        String hostPart = RuntimeContainer.FQDN_HOSTNAME + ":" + port;

        System.out.println("GET http://" + hostPart + "/player/ to retrieve the current status");
        System.out.println("GET http://" + hostPart + "/player/play to play");
        System.out.println("PUT http://" + hostPart + "/player/play to play uploaded midi data");
        System.out.println("GET http://" + hostPart + "/player/stop to stop");
        System.out.println("GET http://" + hostPart + "/player/ to get the current state (running/stopped)");
        System.out.println("GET http://" + hostPart + "/player/port/{port:0-8}/{state:ON|OFF} to control port state");
        System.out.println("GET http://" + hostPart + "/player/devices get a list of all devices (xml format)");
        System.out.println("GET http://" + hostPart
                + "/player/devices/{id}/to/relay to connect an in-device to the out device (relay)");

        while (active) {
            Thread.sleep(500);
        }

        netty.stop();
        shutdown = true;
        System.out.println("Server stopped");
    }

    private void close() {
        if (MidiInstance.getInstance().getWithRelay() != null) {
            MidiInstance.getInstance().getWithRelay().close();
        }

        if (MidiInstance.getInstance().getWithSound() != null) {
            MidiInstance.getInstance().getWithSound().close();
        }

        if (MidiInstance.getInstance().getSequencer() != null) {
            MidiInstance.getInstance().getSequencer().close();
        }

        context.close();
    }

    public static Server getInstance() {
        return instance;
    }

}
