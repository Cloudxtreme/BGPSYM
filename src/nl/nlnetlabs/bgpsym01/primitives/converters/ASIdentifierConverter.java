package nl.nlnetlabs.bgpsym01.primitives.converters;

import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.factories.ASFactory;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ASIdentifierConverter implements Converter {

    /* if the ASIdentifier starts with this letter than it is name (e.g. AS213), otherwise
     * we treat it as a number (and look for an AS with this internal ID).
     */
    public static final char LETTER_INDICATOR = 'A';

    public void marshal(Object asObject, HierarchicalStreamWriter writer, MarshallingContext arg2) {
        ASIdentifier asId = (ASIdentifier) asObject;
        writer.setValue(serializeValue(asId));
    }

    String serializeValue(ASIdentifier asId) {
        return asId.getId();
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext arg1) {
        String value = reader.getValue();
        ASIdentifier instance = parseValue(value);
        return instance;
    }

    ASIdentifier parseValue(String value) {
        if (value.charAt(0) == LETTER_INDICATOR) {
            return ASFactory.getInstance(value);
        } else {
            return ASFactory.getInstance(Integer.parseInt(value));
        }
    }

    @SuppressWarnings("unchecked")
    public boolean canConvert(Class arg0) {
        return arg0.equals(ASIdentifier.class);
    }

}
