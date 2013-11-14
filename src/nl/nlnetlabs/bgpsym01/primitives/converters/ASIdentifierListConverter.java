package nl.nlnetlabs.bgpsym01.primitives.converters;

import java.util.ArrayList;
import java.util.List;

import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.factories.ASFactory;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ASIdentifierListConverter implements Converter {

	@Override
	public boolean canConvert(Class clazz) {
		return List.class.isAssignableFrom(clazz);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void marshal(Object list, HierarchicalStreamWriter writer, MarshallingContext arg2) {
		List<ASIdentifier> asIdentifiers = (List<ASIdentifier>) list;
        for (ASIdentifier asId : asIdentifiers) {
        	writer.startNode("as");
            writer.setValue(asId.getInternalId() + "");
            writer.endNode();
        }
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext arg1) {
		List<ASIdentifier> asIdentifiers = new ArrayList<ASIdentifier>();
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            String value = reader.getValue();
            ASIdentifier instance = parseValue(value);
            asIdentifiers.add(instance);
            reader.moveUp();
        }
        return asIdentifiers;
	}
	
	ASIdentifier parseValue(String value) {
        if (value.charAt(0) == ASIdentifierConverter.LETTER_INDICATOR) {
            return ASFactory.getInstance(value);
        } else {
            return ASFactory.getInstance(Integer.parseInt(value));
        }
    }

}
