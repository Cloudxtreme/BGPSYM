package nl.nlnetlabs.bgpsym01.main;

import nl.nlnetlabs.bgpsym01.mock.AbstractTest;

import org.apache.log4j.Logger;

public class ToolsTest extends AbstractTest {

    @SuppressWarnings("unused")
    private static Logger log = Logger.getLogger(ToolsTest.class);

    public void testTools() {
        Tools tools = Tools.getInstance();

        // those values are hardcoded for now!!!
        assertEquals("too big block for a small neighbors count", 1024, tools.getPrefixBlockSize(2));
        assertEquals("too big block for a small neighbors count", 1024, tools.getPrefixBlockSize(4));
        assertTrue("too small block for a big neighbors count", tools.getPrefixBlockSize(512) > 33000);

    }

}
