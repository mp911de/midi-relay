package de.paluch.midi.relay.job;

import org.quartz.*;

import de.paluch.midi.relay.relay.RemoteRelayReceiver;

/**
 * @author <a href="mailto:mark.paluch@1und1.de">Mark Paluch</a>
 * @since 09.11.12 19:44
 */
@DisallowConcurrentExecution
public class SwitchOffJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap data = context.getJobDetail().getJobDataMap();
        RemoteRelayReceiver remoteRelayReceiver = (RemoteRelayReceiver) data.get("remoteRelayReceiver");
        remoteRelayReceiver.off(0);
    }
}
