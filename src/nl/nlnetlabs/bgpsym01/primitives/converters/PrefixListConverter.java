package nl.nlnetlabs.bgpsym01.primitives.converters;

import java.util.ArrayList;
import java.util.List;

import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class PrefixListConverter implements Converter {

    @SuppressWarnings("unchecked")
    public void marshal(Object list, HierarchicalStreamWriter writer, MarshallingContext arg2) {
        List<Prefix> prefixList = (List<Prefix>) list;
        for (Prefix prefix : prefixList) {
            writer.startNode("prefix");
            writer.setValue(prefix.getNum() + "");
            writer.endNode();
        }
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext arg1) {
        List<Prefix> prefixList = new ArrayList<Prefix>();
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            prefixList.add(Prefix.getInstance(Integer.parseInt(reader.getValue())));
            reader.moveUp();
        }
        return prefixList;

    }

    public boolean canConvert(Class clazz) {
        return List.class.isAssignableFrom(clazz);
    }

}
