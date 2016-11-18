package de.paluch.midi.relay.relay;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author <a href="mailto:mark.paluch@1und1.de">Mark Paluch</a>
 * @since 08.11.12 19:59
 */
@Slf4j
@NoArgsConstructor
public class ETHRLY16 implements RemoteRelayReceiver {

    String hostname;
    int port;
    long lastSendTimestamp = -1;
    long connectionKeepAlive = 30000;
    long bytesSent = 0;
    SocketChannel socketChannel;

    public final static int ALL_ON = 100;
    public final static int ALL_OFF = 110;

    private void checkOrInit() {
        try {
            if (socketChannel == null) {
                socketChannel = SocketChannel.open();
                socketChannel.setOption(StandardSocketOptions.TCP_NODELAY, true);

            }
            if (!socketChannel.isConnected()) {
                boolean connected = socketChannel.connect(new InetSocketAddress(hostname, port));
                if (!connected) {
                    long startTime = System.currentTimeMillis();
                    while (!socketChannel.finishConnect()) {
                        if (System.currentTimeMillis() - startTime < 1000) {
                            // keep trying
                            Thread.sleep(100);
                        } else {
                            throw new SocketException("Connection timeout");
                        }
                    }
                }
            }

        } catch (IOException e) {
            log.warn(e.getMessage(), e);
            try {
                if (socketChannel != null) {
                    socketChannel.close();
                }
            } catch (IOException e1) {
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {

        if (socketChannel != null) {
            if (socketChannel.isConnected()) {
                try {
                    socketChannel.close();
                } catch (IOException e) {
                    log.warn(e.getMessage(), e);
                }
            }
            socketChannel = null;
        }
    }

    @Override
    public void keepaliveOrClose() {
        if (socketChannel != null) {
            long unused = System.currentTimeMillis() - lastSendTimestamp;
            if (unused > connectionKeepAlive) {
                log.info("Closing socket because of inactivity since " + unused + "ms");
                close();
            }
        }
    }

    @Override
    public void on(int port) {

        write(ALL_ON + port);

    }

    private void write(int command) {
        checkOrInit();
        try {
            if (socketChannel != null && socketChannel.isConnected()) {
                bytesSent++;

                ByteBuffer buf = ByteBuffer.allocate(1);
                buf.clear();
                buf.put((byte) command);

                buf.flip();

                while (buf.hasRemaining()) {
                    socketChannel.write(buf);
                }

                lastSendTimestamp = System.currentTimeMillis();
            }
        } catch (Exception e) {
            log.warn(e.toString());
        }
    }

    @Override
    public void off(int port) {
        write(ALL_OFF + port);
    }

    @Override
    public long getBytesSent() {
        return bytesSent;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
