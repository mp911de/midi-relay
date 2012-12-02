package de.paluch.midi.relay.job;

import de.paluch.midi.relay.midi.MidiPlayer;
import org.quartz.*;

/**
 * @author <a href="mailto:mark.paluch@1und1.de">Mark Paluch</a>
 * @since 09.11.12 19:44
 */
@DisallowConcurrentExecution
public class PlayJob implements Job {


    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap data = context.getJobDetail().getJobDataMap();
        MidiPlayer midiPlayer = (MidiPlayer) data.get("midiPlayer");
        midiPlayer.play(null);
    }
}
