package nl.nlnetlabs.bgpsym01.primitives.factories;

import java.util.NoSuchElementException;

import nl.nlnetlabs.bgpsym01.mock.AbstractTest;

public class ASFactoryTest extends AbstractTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testGetInstanceString() {
        assertEquals(getAS(23), ASFactory.getInstance("AS23"));

        // look for unknown value
        try {
            ASFactory.getInstance("AS124124214");
            fail("found...");
        } catch (NoSuchElementException e) {
            // OK
        }
    }

}
