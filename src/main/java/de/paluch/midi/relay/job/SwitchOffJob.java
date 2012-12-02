package de.paluch.midi.relay.job;

import de.paluch.midi.relay.relay.ETHRLY16;
import org.quartz.*;

/**
 * @author <a href="mailto:mark.paluch@1und1.de">Mark Paluch</a>
 * @since 09.11.12 19:44
 */
@DisallowConcurrentExecution
public class SwitchOffJob implements Job {


    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap data = context.getJobDetail().getJobDataMap();
        ETHRLY16 ethrly16 = (ETHRLY16) data.get("ethrly16");
        ethrly16.off(0);
    }
}
