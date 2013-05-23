package nl.nlnetlabs.bgpsym01.primitives.converters;

import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;

public class ASIdentifierConverterTest extends AbstractTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testSerializeValue() {
        ASIdentifierConverter converter = getConverter();
        assertEquals("AS23", converter.serializeValue(getAS(23)));
        assertEquals("AS231", converter.serializeValue(getAS(231)));
    }

    private ASIdentifierConverter getConverter() {
        return new ASIdentifierConverter();
    }

    public void testParseValue() {
        ASIdentifierConverter converter = getConverter();
        assertEquals(getAS(23), converter.parseValue("AS23"));
        assertEquals(getAS(191), converter.parseValue("AS191"));
        assertEquals(getAS(12), converter.parseValue("AS12"));

        // oh yes - we still like the old style (if its number than its number)
        ASIdentifier as415 = getAS(415);
        int num = as415.getInternalId();
        assertEquals(as415, converter.parseValue("" + num));

    }

}
