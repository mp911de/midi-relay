package de.paluch.midi.relay.job;

import de.paluch.midi.relay.midi.MidiPlayer;
import de.paluch.midi.relay.midi.PlayerState;
import org.quartz.*;

/**
 * @author <a href="mailto:mark.paluch@1und1.de">Mark Paluch</a>
 * @since 09.11.12 19:44
 */
@DisallowConcurrentExecution
public class PlayJob implements Job
{

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        JobDataMap data = context.getMergedJobDataMap();
        MidiPlayer midiPlayer = (MidiPlayer) data.get("midiPlayer");
        PlayerState request = (PlayerState) data.get("request");

        if (request == null)
        {

            midiPlayer.play((String) null);
        } else
        {
            midiPlayer.play(request);
        }
    }
}
