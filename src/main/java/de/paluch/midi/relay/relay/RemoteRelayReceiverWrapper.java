package de.paluch.midi.relay.relay;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 30.11.13 19:25
 */
public class RemoteRelayReceiverWrapper implements RemoteRelayReceiver
{
    private RemoteRelayReceiver delegate;

    public RemoteRelayReceiver getDelegate()
    {
        return delegate;
    }
    public void setDelegate(RemoteRelayReceiver delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public void close()
    {
        delegate.close();
    }
    @Override
    public void keepaliveOrClose()
    {
        delegate.keepaliveOrClose();
    }
    @Override
    public void on(int port)
    {
        delegate.on(port);
    }
    @Override
    public void off(int port)
    {
        delegate.off(port);
    }
    @Override
    public long getBytesSent()
    {
        return delegate.getBytesSent();
    }
}
