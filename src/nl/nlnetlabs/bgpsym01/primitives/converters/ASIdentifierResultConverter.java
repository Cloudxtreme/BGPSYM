package nl.nlnetlabs.bgpsym01.primitives.converters;

import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ASIdentifierResultConverter implements Converter {

	@Override
	public boolean canConvert(Class arg0) {
		return arg0.equals(ASIdentifier.class);
	}

	@Override
	public void marshal(Object asObject, HierarchicalStreamWriter writer,
			MarshallingContext arg2) {
		ASIdentifier asId = (ASIdentifier) asObject;
        writer.setValue(serializeValue(asId));
	}
	
    String serializeValue(ASIdentifier asId) {
        return asId.getId();
    }

	@Override
	public Object unmarshal(HierarchicalStreamReader arg0,
			UnmarshallingContext arg1) {
		
		return null;
	}
	

}
