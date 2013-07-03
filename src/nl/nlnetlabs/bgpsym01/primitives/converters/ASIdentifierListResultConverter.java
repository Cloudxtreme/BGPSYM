package nl.nlnetlabs.bgpsym01.primitives.converters;

import java.util.List;

import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ASIdentifierListResultConverter implements Converter {

	@Override
	public boolean canConvert(Class clazz) {
		return List.class.isAssignableFrom(clazz);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void marshal(Object list, HierarchicalStreamWriter writer,
			MarshallingContext arg2) {
		List<ASIdentifier> asIdentifiers = (List<ASIdentifier>) list;
        for (ASIdentifier asId : asIdentifiers) {
        	writer.startNode("as");
            writer.setValue(asId.getId() + "");
            writer.endNode();
        }
		
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader arg0,
			UnmarshallingContext arg1) {
		// TODO Auto-generated method stub
		return null;
	}

}
