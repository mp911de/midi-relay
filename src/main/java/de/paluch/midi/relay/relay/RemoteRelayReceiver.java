package de.paluch.midi.relay.relay;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 30.11.13 19:17
 */
public interface RemoteRelayReceiver
{

    void close();

    void keepaliveOrClose();

    void on(int port);

    void off(int port);

    long getBytesSent();
}
