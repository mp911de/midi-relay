package de.paluch.midi.relay;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import org.apache.log4j.Logger;

/**
 * Static Details about the runtime container: Hostname (simple/fqdn), Address and timestamp of the first access (time
 * when the application was loaded).
 *
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class RuntimeContainer
{

    /**
     * Current Hostname.
     */
    public static final String HOSTNAME;

    /**
     * Current FQDN Hostname.
     */
    public static final String FQDN_HOSTNAME;

    /**
     * Load-Time of this class.
     */
    public static final long FIRST_ACCESS;

    private static final Logger LOGGER = Logger.getLogger(RuntimeContainer.class);

    /**
     * Utility Constructor.
     */
    private RuntimeContainer()
    {

    }

    static
    {

        FIRST_ACCESS = System.currentTimeMillis();

        String myHostName = "";
        String myFQDNHostName = "";
        try
        {

            myHostName = getHostname(false);
            myFQDNHostName = getHostname(true);
        } catch (IOException e)
        {
            LOGGER.info(e.getMessage(), e);
        }

        FQDN_HOSTNAME = myFQDNHostName;
        HOSTNAME = myHostName;
    }

    private static String getHostname(boolean fqdn) throws UnknownHostException
    {

        String hostname = InetAddress.getLocalHost().getHostName();
        if (hostname.indexOf('.') != -1 && !fqdn)
        {
            hostname = hostname.substring(0, hostname.indexOf('.'));
        }

        return hostname;
    }
}
