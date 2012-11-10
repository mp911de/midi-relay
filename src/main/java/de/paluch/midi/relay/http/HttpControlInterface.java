package de.paluch.midi.relay.http;

import de.paluch.midi.relay.relay.ETHRLY16;
import de.paluch.midi.relay.midi.MidiInstance;
import de.paluch.midi.relay.midi.MidiPlayer;
import de.paluch.midi.relay.job.PlayJob;
import org.quartz.*;

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
    private Scheduler scheduler;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String isRunning() throws Exception {
        if (midiPlayer.isRunning()) {
            return "RUNNING";
        }
        return "STOP";
    }

    @GET
    @Path("schedule")
    @Produces(MediaType.TEXT_PLAIN)
    public String addSchedule(@QueryParam("cronExpression") String cronExpression) throws Exception {
        if (cronExpression == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Empty " +
                                                                                                          "cronExpression").build());
        }

        Trigger trigger = TriggerBuilder.newTrigger().forJob(PlayJob.class.getName()).withSchedule(CronScheduleBuilder
                                                                                                           .cronSchedule(cronExpression)).build();
        scheduler.scheduleJob(trigger);
        return "OK";
    }

    @GET
    @Path("play")
    @Produces(MediaType.TEXT_PLAIN)
    public String play() throws Exception {
        midiPlayer.play();
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

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
}
