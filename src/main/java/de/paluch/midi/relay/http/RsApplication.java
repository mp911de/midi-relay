package de.paluch.midi.relay.http;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:mark.paluch@1und1.de">Mark Paluch</a>
 * @since 09.11.12 20:44
 */
public class RsApplication extends Application {
    private java.util.Set<java.lang.Object> objects = new HashSet<Object>();


    public void setObjects(Set<Object> objects) {
        this.objects = objects;
    }

    @Override
    public Set<Object> getSingletons() {
        return objects;
    }
}
