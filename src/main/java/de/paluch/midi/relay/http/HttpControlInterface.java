package de.paluch.midi.relay.http;

import de.paluch.midi.relay.midi.MidiInstance;
import de.paluch.midi.relay.midi.MidiPlayer;
import de.paluch.midi.relay.midi.PlayerState;
import de.paluch.midi.relay.relay.RemoteRelayReceiver;
import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.impl.matchers.GroupMatcher;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Transmitter;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:mark.paluch@1und1.de">Mark Paluch</a>
 * @since 09.11.12 19:58
 */
@Path("player")
public class HttpControlInterface {

    private MidiPlayer midiPlayer;
    private RemoteRelayReceiver remoteRelayReceiver;
    private Scheduler scheduler;
    private Map<Integer, MidiDevice> deviceMap = new ConcurrentHashMap<Integer, MidiDevice>();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PlayerStateRepresentation isRunning() throws Exception {

        PlayerState state = midiPlayer.getState();
        if (state == null) {
            return new PlayerStateRepresentation();
        }

        PlayerStateRepresentation result = toPlayerStateRepresentation(state);

        return result;
    }

    private PlayerStateRepresentation toPlayerStateRepresentation(PlayerState state) {
        PlayerStateRepresentation result = new PlayerStateRepresentation();

        result.setRunning(state.isRunning());

        if (state.isRunning()) {

            result.setStarted(new Date(state.getStarted()));
            result.setEstimatedEnd(new Date(state.getStarted() + (state.getDuration() * 1000)));
            long now = System.currentTimeMillis();
            long played = now - state.getStarted();
            int secondsToPlay = Math.max(0, state.getDuration() - ((int) played / 1000));

            result.setEstimatedSecondsToPlay(secondsToPlay);

            PlayerStateTrackRepresentation track = new PlayerStateTrackRepresentation();
            track.setDuration(state.getDuration());
            track.setFileName(state.getFileName());
            track.setSequenceName(state.getSequenceName());
            track.setId(state.getId());
            result.setTrack(track);

        }

        return result;
    }

    @GET
    @Path("play")
    @Produces(MediaType.TEXT_PLAIN)
    public String play() throws Exception {
        scheduler.triggerJob(new JobKey("playJob"));
        return "OK";
    }

    @PUT
    @Path("play")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes
    public String play(@HeaderParam("X-Request-Id") String id, @HeaderParam("X-Request-FileName") String fileName, byte[] body)
            throws Exception {
        JobDataMap jobData = new JobDataMap();
        PlayerState request = new PlayerState();
        request.setId(id);
        request.setFileName(fileName);
        request.setMidiContents(body);
        jobData.put("request", request);
        scheduler.triggerJob(new JobKey("playJob"), jobData);
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
            remoteRelayReceiver.on(port);
        }

        if (state.equals("OFF")) {
            remoteRelayReceiver.off(port);

        }
        return "OK";
    }

    @GET
    @Path("port/{state:(ON|OFF)}")
    @Produces(MediaType.TEXT_PLAIN)
    public String setActive(@PathParam("state") String state) throws Exception {

        Set<JobKey> def = scheduler.getJobKeys(GroupMatcher.jobGroupContains("DEF"));
        System.out.println(def);

        if (state.equals("ON")) {
            scheduler.triggerJob(new JobKey("switchOnJob"), new JobDataMap());
        }

        if (state.equals("OFF")) {
            scheduler.triggerJob(new JobKey("switchOffJob"), new JobDataMap());
        }

        return "OK";
    }

    @GET
    @Path("devices")
    @Produces({ MediaType.TEXT_XML })
    public MidiDeviceInfosRepresentation getInputDevices() throws Exception {
        MidiDeviceInfosRepresentation result = new MidiDeviceInfosRepresentation();
        MidiDevice.Info infos[] = MidiSystem.getMidiDeviceInfo();
        int id = 0;
        for (MidiDevice.Info info : infos) {

            MidiDeviceInfoRepresentation device = getMidiDeviceInfoRepresentation(id, info);
            result.getDevices().add(device);
            id++;
        }

        return result;
    }

    private MidiDeviceInfoRepresentation getMidiDeviceInfoRepresentation(int id, MidiDevice.Info info)
            throws MidiUnavailableException {
        MidiDeviceInfoRepresentation device = new MidiDeviceInfoRepresentation();
        device.setId(id);
        device.setName(info.getName() + ", " + info.getDescription() + " (" + info.getVendor() + ")");

        MidiDevice midiDevice = MidiSystem.getMidiDevice(info);
        if (midiDevice instanceof Receiver) {
            device.getTypes().add(MidiDeviceInfoRepresentation.Type.RECEIVER);
        }
        if (midiDevice instanceof Sequencer) {
            device.getTypes().add(MidiDeviceInfoRepresentation.Type.SEQUENCER);
        }
        if (midiDevice instanceof Transmitter) {
            device.getTypes().add(MidiDeviceInfoRepresentation.Type.TRANSMITTER);
        }
        if (midiDevice instanceof Synthesizer) {
            device.getTypes().add(MidiDeviceInfoRepresentation.Type.SYNTHESIZER);
        }
        if (midiDevice.getClass().getName().contains("MidiIn")) {
            device.getTypes().add(MidiDeviceInfoRepresentation.Type.IN);
        }
        if (midiDevice.getClass().getName().contains("MidiOut")) {
            device.getTypes().add(MidiDeviceInfoRepresentation.Type.OUT);
        }

        deviceMap.put(id, midiDevice);

        return device;
    }

    @GET
    @Path("devices/{id}/to/relay")
    @Produces(MediaType.TEXT_PLAIN)
    public String addInputDevice(@PathParam("id") int id) throws Exception {

        MidiDevice device = deviceMap.get(id);
        if (device == null) {
            MidiDevice.Info infos[] = MidiSystem.getMidiDeviceInfo();
            device = MidiSystem.getMidiDevice(infos[id]);
            deviceMap.put(id, device);
        }

        device.getTransmitter().setReceiver(MidiInstance.getInstance().getReceiver());

        if (!device.isOpen()) {
            device.open();
        }
        return "ADDED";

    }

    public MidiPlayer getMidiPlayer() {
        return midiPlayer;
    }

    public void setMidiPlayer(MidiPlayer midiPlayer) {
        this.midiPlayer = midiPlayer;
    }

    public RemoteRelayReceiver getRemoteRelayReceiver() {
        return remoteRelayReceiver;
    }

    public void setRemoteRelayReceiver(RemoteRelayReceiver remoteRelayReceiver) {
        this.remoteRelayReceiver = remoteRelayReceiver;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
}
