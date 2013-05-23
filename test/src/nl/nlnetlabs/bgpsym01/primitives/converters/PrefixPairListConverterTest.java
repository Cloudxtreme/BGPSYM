package nl.nlnetlabs.bgpsym01.primitives.converters;

import java.util.ArrayList;
import java.util.List;

import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.factories.XStreamFactory;
import nl.nlnetlabs.bgpsym01.primitives.types.Pair;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.Annotations;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

public class PrefixPairListConverterTest extends AbstractTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @XStreamAlias("tmp1")
    private static class Tmp1 {

        @XStreamConverter(PrefixPairListConverter.class)
        public List<Pair<Prefix, String>> prefixes;

        @Override
        public boolean equals(Object obj) {
            return ((Tmp1) obj).prefixes.equals(prefixes);
        }

    }

    public void test1() {

        XStream stream = XStreamFactory.getXStream();
        Annotations.configureAliases(stream, Tmp1.class);

        List<Pair<Prefix, String>> prefixes = new ArrayList<Pair<Prefix, String>>();
        for (int i = 0; i < 3; i++) {
            prefixes.add(new Pair<Prefix, String>(Prefix.getInstance(i + 7), "prefix_" + (i + 3) + "__"));
        }

        Tmp1 obj1 = new Tmp1();
        obj1.prefixes = prefixes;
        Object obj2 = stream.fromXML(stream.toXML(obj1));
        assertEquals(obj1, obj2);

    }



}
