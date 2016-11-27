package de.paluch.midi.relay.http;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Transmitter;

import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.paluch.midi.relay.midi.MidiInstance;
import de.paluch.midi.relay.midi.MidiPlayer;
import de.paluch.midi.relay.midi.PlayerState;
import de.paluch.midi.relay.midi.WorkQueueExecutor;
import de.paluch.midi.relay.relay.RemoteRelayReceiver;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author <a href="mailto:mark.paluch@1und1.de">Mark Paluch</a>
 * @since 09.11.12 19:58
 */
@RequestMapping(value = "player")
@Controller
@ResponseBody
@RequiredArgsConstructor
@Slf4j
public class HttpControlInterface {

    private final Map<Integer, MidiDevice> deviceMap = new ConcurrentHashMap<Integer, MidiDevice>();

    @NonNull
    MidiPlayer midiPlayer;
    @NonNull
    RemoteRelayReceiver remoteRelayReceiver;
    @NonNull
    Scheduler scheduler;
    @NonNull
    WorkQueueExecutor workQueueExecutor;

    @GetMapping
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
        }

        if (state.isRunning() || state.isErrorState()) {

            PlayerStateTrackRepresentation track = new PlayerStateTrackRepresentation();
            track.setDuration(state.getDuration());
            track.setFileName(state.getFileName());
            track.setSequenceName(state.getSequenceName());
            track.setId(state.getId());
            track.setErrorState(state.isErrorState());
            track.setExceptionMessage(state.getExceptionMessage());
            result.setTrack(track);
        }

        return result;
    }

    @GetMapping(value = "play", produces = MediaType.TEXT_PLAIN_VALUE)
    public String play() throws Exception {
        scheduler.triggerJob(new JobKey("playJob"));
        return "OK";
    }

    @PutMapping(value = "play", produces = MediaType.TEXT_PLAIN_VALUE, consumes = MediaType.ALL_VALUE)
    public String play(@RequestHeader(value = "X-Request-Id", defaultValue = "") String id,
            @RequestHeader(value = "X-Request-FileName", defaultValue = "") String fileName, @RequestBody byte[] body)
            throws Exception {

        log.info("Play with upload file {}, upload size {}", fileName, body != null ? body.length : -1);

        JobDataMap jobData = new JobDataMap();
        PlayerState request = new PlayerState();
        request.setId(id);
        request.setFileName(fileName);
        request.setMidiContents(body);
        jobData.put("request", request);
        scheduler.triggerJob(new JobKey("playJob"), jobData);
        return "OK";
    }

    @GetMapping(value = "play/{filename}", produces = MediaType.TEXT_PLAIN_VALUE)
    public String play(@PathVariable("filename") String filename) throws Exception {

        log.info("Play {}", filename);
        midiPlayer.play(filename);
        return "OK";
    }

    @GetMapping(value = "stop", produces = MediaType.TEXT_PLAIN_VALUE)
    public String stop() throws Exception {
        midiPlayer.stop();
        return "OK";
    }

    @GetMapping(value = "device", produces = MediaType.TEXT_PLAIN_VALUE)
    public String setActive(@RequestParam("id") int id, @RequestParam("state") boolean state) throws Exception {

        MidiInstance.getInstance().getWithRelay().setActive(id, state);
        MidiInstance.getInstance().getWithSound().setActive(id, state);
        return "OK";
    }

    @GetMapping(value = "port/{port:[0-8]}/{state}", produces = MediaType.TEXT_PLAIN_VALUE)
    public String setActive(@PathVariable("port") int port, @PathVariable("state") String state) throws Exception {

        if (state.equals("ON")) {
            remoteRelayReceiver.on(port);
        }

        if (state.equals("OFF")) {
            remoteRelayReceiver.off(port);

        }
        return "OK";
    }

    @GetMapping(value = "port/{state}", produces = MediaType.TEXT_PLAIN_VALUE)
    public String setActive(@PathVariable("state") String state) throws Exception {

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

    @GetMapping(value = "devices", produces = MediaType.TEXT_XML_VALUE)
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

    @GetMapping(value = "devices/{id}/to/relay", produces = MediaType.TEXT_PLAIN_VALUE)
    public String addInputDevice(@PathVariable("id") int id) throws Exception {

        MidiDevice device = deviceMap.get(id);
        if (device == null) {
            MidiDevice.Info infos[] = MidiSystem.getMidiDeviceInfo();
            device = MidiSystem.getMidiDevice(infos[id]);
            deviceMap.put(id, device);
        }

        device.getTransmitter().setReceiver(MidiInstance.getInstance().getWithRelay());

        if (!device.isOpen()) {
            device.open();
        }
        return "ADDED " + device.getDeviceInfo().getName() + "/" + device.getDeviceInfo().getDescription();

    }

    @GetMapping(value = "delay", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getDelay() {
        return "" + workQueueExecutor.getDelay();
    }

    @PutMapping(value = "delay", produces = MediaType.TEXT_PLAIN_VALUE)
    public String setDelay(String delay) {
        workQueueExecutor.setDelay(Long.parseLong(delay));
        return "" + workQueueExecutor.getDelay();

    }
}
