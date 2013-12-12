package de.paluch.midi.relay.http;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 12.12.13 19:54
 */
@XmlAccessorType(XmlAccessType.NONE)
public class MidiDeviceInfoRepresentation
{
    @XmlAttribute(name = "id")
    private int id;

    @XmlElement(name = "name")
    private String name;

    @XmlElementWrapper(name = "types")
    @XmlElement(name = "type")
    private List<Type> types = new ArrayList<Type>();

    public int getId()
    {
        return id;
    }
    public void setId(int id)
    {
        this.id = id;
    }
    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }

    public List<Type> getTypes()
    {
        return types;
    }
    public void setTypes(List<Type> types)
    {
        this.types = types;
    }
    public enum Type
    {
        IN, OUT, RECEIVER, SYNTHESIZER, TRANSMITTER, SEQUENCER;
    }
}
