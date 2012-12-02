package de.paluch.midi.relay.http;

import de.paluch.midi.relay.relay.ETHRLY16;
import de.paluch.midi.relay.midi.MidiInstance;
import de.paluch.midi.relay.midi.MidiPlayer;
import de.paluch.midi.relay.job.PlayJob;
import org.quartz.*;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author <a href="mailto:mark.paluch@1und1.de">Mark Paluch</a>
 * @since 09.11.12 19:58
 */
@Path("player")
public class HttpControlInterface {

    private MidiPlayer midiPlayer;
    private ETHRLY16 midiReceiver;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String isRunning() throws Exception {
        if (midiPlayer.isRunning()) {
            return "RUNNING";
        }
        return "STOP";
    }

    @GET
    @Path("play")
    @Produces(MediaType.TEXT_PLAIN)
    public String play() throws Exception {
        midiPlayer.play(null);
        return "OK";
    }

    @GET
    @Path("play/{filename}")
    @Produces(MediaType.TEXT_PLAIN)
    public String play(@PathParam("filename") String filename) throws Exception {
        midiPlayer.play(filename);
        return "OK";
    }

    @GET
    @Path("stop")
    @Produces(MediaType.TEXT_PLAIN)
    public String stop() throws Exception {
        midiPlayer.stop();
        return "OK";
    }

    @GET
    @Path("device")
    @Produces(MediaType.TEXT_PLAIN)
    public String setActive(@QueryParam("id") int id, @QueryParam("state") boolean state) throws Exception {
        MidiInstance.getInstance().getReceiver().setActive(id, state);
        return "OK";
    }

    @GET
    @Path("port/{port:[0-8]}/{state:(ON|OFF)}")
    @Produces(MediaType.TEXT_PLAIN)
    public String setActive(@PathParam("port") int port, @PathParam("state") String state) throws Exception {
        if (state.equals("ON")) {
            midiReceiver.on(port);
        }

        if (state.equals("OFF")) {
            midiReceiver.off(port);

        }
        return "OK";
    }

    @GET
    @Path("in-devices/")
    @Produces(MediaType.TEXT_PLAIN)
    public String setActive() throws Exception {
        StringBuffer sb = new StringBuffer();


        MidiDevice.Info infos[] = MidiSystem.getMidiDeviceInfo();
        for (MidiDevice.Info info : infos) {
            sb.append(info.getName() + ", " + info.getDescription() + " (" + info.getVendor() + ")");
            sb.append("\r\n");
        }
        return sb.toString();
    }

    @GET
    @Path("in-devices/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public String addInputDevice(@PathParam("id") int id) throws Exception {

        MidiDevice.Info infos[] = MidiSystem.getMidiDeviceInfo();
        MidiDevice device = MidiSystem.getMidiDevice(infos[id]);


        device.getTransmitter().setReceiver(MidiInstance.getInstance().getReceiver());
        if (!device.isOpen()) {
            device.open();
        }
        return "OK";
    }


    public MidiPlayer getMidiPlayer() {
        return midiPlayer;
    }

    public void setMidiPlayer(MidiPlayer midiPlayer) {
        this.midiPlayer = midiPlayer;
    }

    public ETHRLY16 getMidiReceiver() {
        return midiReceiver;
    }

    public void setMidiReceiver(ETHRLY16 midiReceiver) {
        this.midiReceiver = midiReceiver;
    }

}
