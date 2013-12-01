package de.paluch.midi.relay.http;

import com.google.common.io.ByteStreams;
import de.paluch.midi.relay.job.PlayJob;
import de.paluch.midi.relay.midi.PlayerState;
import de.paluch.midi.relay.relay.RemoteRelayReceiver;
import de.paluch.midi.relay.midi.MidiInstance;
import de.paluch.midi.relay.midi.MidiPlayer;
import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.Scheduler;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.util.Date;

/**
 * @author <a href="mailto:mark.paluch@1und1.de">Mark Paluch</a>
 * @since 09.11.12 19:58
 */
@Path("player")
public class HttpControlInterface
{

    private MidiPlayer midiPlayer;
    private RemoteRelayReceiver remoteRelayReceiver;
    private Scheduler scheduler;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PlayerStateRepresentation isRunning() throws Exception
    {

        PlayerState state = midiPlayer.getState();
        if (state == null)
        {
            return new PlayerStateRepresentation();
        }

        PlayerStateRepresentation result = toPlayerStateRepresentation(state);

        return result;
    }

    private PlayerStateRepresentation toPlayerStateRepresentation(PlayerState state)
    {
        PlayerStateRepresentation result = new PlayerStateRepresentation();

        result.setRunning(state.isRunning());

        if (state.isRunning())
        {

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
    public String play() throws Exception
    {
        scheduler.triggerJob(new JobKey("playJob"));
        return "OK";
    }

    @PUT
    @Path("play")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes
    public String play(@HeaderParam("X-Request-Id") String id, @HeaderParam("X-Request-FileName") String fileName,
                       byte[] body) throws Exception
    {
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
    public String play(@PathParam("filename") String filename) throws Exception
    {
        midiPlayer.play(filename);
        return "OK";
    }

    @GET
    @Path("stop")
    @Produces(MediaType.TEXT_PLAIN)
    public String stop() throws Exception
    {
        midiPlayer.stop();
        return "OK";
    }

    @GET
    @Path("device")
    @Produces(MediaType.TEXT_PLAIN)
    public String setActive(@QueryParam("id") int id, @QueryParam("state") boolean state) throws Exception
    {
        MidiInstance.getInstance().getReceiver().setActive(id, state);
        return "OK";
    }

    @GET
    @Path("port/{port:[0-8]}/{state:(ON|OFF)}")
    @Produces(MediaType.TEXT_PLAIN)
    public String setActive(@PathParam("port") int port, @PathParam("state") String state) throws Exception
    {
        if (state.equals("ON"))
        {
            remoteRelayReceiver.on(port);
        }

        if (state.equals("OFF"))
        {
            remoteRelayReceiver.off(port);

        }
        return "OK";
    }

    @GET
    @Path("in-devices/")
    @Produces(MediaType.TEXT_PLAIN)
    public String setActive() throws Exception
    {
        StringBuffer sb = new StringBuffer();

        MidiDevice.Info infos[] = MidiSystem.getMidiDeviceInfo();
        for (MidiDevice.Info info : infos)
        {
            sb.append(info.getName() + ", " + info.getDescription() + " (" + info.getVendor() + ")");
            sb.append("\r\n");
        }
        return sb.toString();
    }

    @GET
    @Path("in-devices/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public String addInputDevice(@PathParam("id") int id) throws Exception
    {

        MidiDevice.Info infos[] = MidiSystem.getMidiDeviceInfo();
        MidiDevice device = MidiSystem.getMidiDevice(infos[id]);

        device.getTransmitter().setReceiver(MidiInstance.getInstance().getReceiver());
        if (!device.isOpen())
        {
            device.open();
        }
        return "OK";
    }

    public MidiPlayer getMidiPlayer()
    {
        return midiPlayer;
    }

    public void setMidiPlayer(MidiPlayer midiPlayer)
    {
        this.midiPlayer = midiPlayer;
    }

    public RemoteRelayReceiver getRemoteRelayReceiver()
    {
        return remoteRelayReceiver;
    }

    public void setRemoteRelayReceiver(RemoteRelayReceiver remoteRelayReceiver)
    {
        this.remoteRelayReceiver = remoteRelayReceiver;
    }

    public Scheduler getScheduler()
    {
        return scheduler;
    }
    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }
}
