package de.paluch.midi.relay.relay;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 30.11.13 19:17
 */
public interface RoutingRelayReceiver
{

    void on(String note);

    void off(String note);
}
