package nl.nlnetlabs.bgpsym01.primitives.converters;

import nl.nlnetlabs.bgpsym01.coordinator.events.PrefixData;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class PrefixDataConverter implements Converter {

    @Override
    public void marshal(Object x, HierarchicalStreamWriter writer, MarshallingContext arg2) {
        PrefixData data = (PrefixData) x;
        writer.startNode("prefix");
        writer.setValue(data.prefix.getNum() + "");
        writer.endNode();
        writer.startNode("name");
        writer.setValue(data.name);
        writer.endNode();
        writer.startNode("additional");
        writer.setValue(data.additional + "");
        writer.endNode();
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext arg1) {
        PrefixData data = new PrefixData();
        reader.moveDown();
        data.prefix = Prefix.getInstance(Integer.parseInt(reader.getValue()));
        reader.moveUp();
        reader.moveDown();
        data.name = reader.getValue();
        reader.moveUp();
        reader.moveDown();
        data.additional = Long.parseLong(reader.getValue());
        reader.moveUp();
        return data;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean canConvert(Class clazz) {
        return PrefixData.class.isAssignableFrom(clazz);
    }

}
