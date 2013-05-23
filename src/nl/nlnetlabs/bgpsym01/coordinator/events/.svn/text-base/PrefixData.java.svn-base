package nl.nlnetlabs.bgpsym01.coordinator.events;

import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.converters.PrefixDataConverter;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

@XStreamAlias("pdata")
@XStreamConverter(PrefixDataConverter.class)
public class PrefixData {

    public PrefixData() {

    }

    public PrefixData(Prefix prefix, String name, long additional) {
        super();
        this.prefix = prefix;
        this.name = name;
        this.additional = additional;
    }
    public Prefix prefix;
    public String name;
    public long additional;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PrefixData) {
            PrefixData data = (PrefixData) obj;
            return prefix.equals(data.prefix) && name.equals(data.name) && additional == data.additional;
        }
        return false;
    }

    @Override
    public String toString() {
        return prefix + ";" + name + ";" + additional;
    }

}
