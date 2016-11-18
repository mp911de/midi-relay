package de.paluch.midi.relay.job;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import de.paluch.midi.relay.relay.RemoteRelayReceiver;

/**
 * @author <a href="mailto:mark.paluch@1und1.de">Mark Paluch</a>
 * @since 09.11.12 19:20
 */
public class ConnectionWatchdogJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        JobDataMap data = context.getJobDetail().getJobDataMap();
        RemoteRelayReceiver remoteRelayReceiver = (RemoteRelayReceiver) data.get("remoteRelayReceiver");
        remoteRelayReceiver.keepaliveOrClose();
    }
}
