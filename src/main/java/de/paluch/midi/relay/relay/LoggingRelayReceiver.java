package de.paluch.midi.relay.relay;

import org.apache.log4j.Logger;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 30.11.13 19:24
 */
public class LoggingRelayReceiver implements RemoteRelayReceiver
{
    private Logger log = Logger.getLogger(getClass());

    @Override
    public void close()
    {

    }

    @Override
    public void keepaliveOrClose()
    {

    }

    @Override
    public void on(int port)
    {
        log.info("ON " + port);
    }

    @Override
    public void off(int port)
    {
        log.info("OFF " + port);
    }

    @Override
    public long getBytesSent()
    {
        return 0;
    }
}
