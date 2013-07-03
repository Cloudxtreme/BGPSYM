package nl.nlnetlabs.bgpsym01.primitives.converters;

import java.util.List;

import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class RouteConverter implements Converter {

	@Override
	public boolean canConvert(Class clazz) {
		return List.class.isAssignableFrom(clazz);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void marshal(Object route, HierarchicalStreamWriter writer, MarshallingContext arg2) {
		Route r = (Route) route;
		writer.startNode("rt");
        for (ASIdentifier asId : r.getHops()) {
        	writer.startNode("as");
            writer.setValue(asId.getId());
            writer.endNode();
        }
        writer.endNode();		
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader arg0,
			UnmarshallingContext arg1) {
		// TODO Auto-generated method stub
		return null;
	}

}
