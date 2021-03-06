package de.paluch.midi.relay.relay;

import lombok.extern.slf4j.Slf4j;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 30.11.13 19:24
 */
@Slf4j
public class LoggingRelayReceiver implements RemoteRelayReceiver, RoutingRelayReceiver {

    @Override
    public void close() {

    }

    @Override
    public void keepaliveOrClose() {

    }

    @Override
    public void on(int port) {
        log.info("ON " + port);
    }

    @Override
    public void off(int port) {
        log.info("OFF " + port);
    }

    @Override
    public long getBytesSent() {
        return 0;
    }

    @Override
    public void on(String note) {
        log.info("ON " + note);
    }

    @Override
    public void off(String note) {
        log.info("OFF " + note);
    }
}
