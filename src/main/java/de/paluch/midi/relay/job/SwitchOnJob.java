package de.paluch.midi.relay.job;

import de.paluch.midi.relay.relay.RemoteRelayReceiver;
import org.quartz.*;

/**
 * @author <a href="mailto:mark.paluch@1und1.de">Mark Paluch</a>
 * @since 09.11.12 19:44
 */
@DisallowConcurrentExecution
public class SwitchOnJob implements Job {


    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap data = context.getJobDetail().getJobDataMap();
        RemoteRelayReceiver remoteRelayReceiver = (RemoteRelayReceiver) data.get("remoteRelayReceiver");
        remoteRelayReceiver.on(0);
    }
}
