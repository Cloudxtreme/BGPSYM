package nl.nlnetlabs.bgpsym01.route;

import java.util.HashMap;

import nl.nlnetlabs.bgpsym01.command.Rewriter;
import nl.nlnetlabs.bgpsym01.coordinator.events.PrefixData;
import nl.nlnetlabs.bgpsym01.mock.AbstractTest;

public class NabsirUpdateTest extends AbstractTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        generateASes(14000);
    }

    private void check(NabsirUpdate n1, NabsirUpdate n2, boolean equal) {
        if (equal) {
            assertEquals(n1, n2);
            assertEquals(n2, n1);
        } else {
            assertFalse(n1.equals(n2));
            assertFalse(n2.equals(n1));
        }
    }

    public void testEquals() {
        NabsirUpdate n1 = new NabsirUpdate();
        NabsirUpdate n2 = new NabsirUpdate();
        check(n1, n2, true);
        n1.setPrefix(getPrefix(1));
        check(n1, n2, false);
        n2.setPrefix(getPrefix(1));
        check(n1, n2, true);

        n1.setRoute(createRoute(1, 2, 3, 4));
        check(n1, n2, false);
        n2.setRoute(createRoute(1, 3, 4));
        check(n1, n2, false);
        n2.setRoute(createRoute(1, 2, 3, 4));
        check(n1, n2, true);
    }

    public void testSerialize() {
        NabsirUpdate n = new NabsirUpdate();
        n.setPrefix(getPrefix(12));
        n.setRoute(createRoute(3, 4, 5, 6));
        n.setTo(getAS(12));
        n.setFrom(getAS(30));
        n.setTime(10324);
        n.setWithdrawal(true);
        assertTrue(n.equals(Rewriter.rewrite(n, NabsirUpdate.class)));
        n.setWithdrawal(false);
        assertTrue(n.equals(Rewriter.rewrite(n, NabsirUpdate.class)));
        n.setFrom(getAS(19));
        n.setTime(10321);
        assertTrue(n.equals(Rewriter.rewrite(n, NabsirUpdate.class)));
        n.setTo(getAS(93));
        assertTrue(n.equals(Rewriter.rewrite(n, NabsirUpdate.class)));

        n.setRoute(null);
        assertTrue(n.equals(Rewriter.rewrite(n, NabsirUpdate.class)));
        // just check for exceptions
        n.toString(new HashMap<String, PrefixData>());
    }

    public void testGetTxtDate() {
        /*
         * nabsir always returns day after 01/01/08
         */
        NabsirUpdate update = new NabsirUpdate();
        assertEquals(update.getTxtDate(41000, 0), "01/01/08 00:00:41");
        assertEquals(update.getTxtDate(41000, 3000), "01/01/08 00:00:44");
        assertEquals(update.getTxtDate(41000, -13000), "01/01/08 00:00:28");
    }

    public void testToString() {
        HashMap<String, PrefixData> map = new HashMap<String, PrefixData>();
        map.put("msg_101", new PrefixData(getPrefix(101), "84.205.71.0/24", 19000)); /* 19000 -> add 19 seconds */

        String expected = "347 ;  124;   84.205.71.0/24; False; False;   01/01/08 00:00:50;  [4347 12793 13237 12654]                           ;  None  ; Update";

        NabsirUpdate update = new NabsirUpdate();
        update.setFrom(getAS(347));
        update.setTo(getAS(124));
        update.setPrefix(getPrefix(101));
        update.setTime(31000); // this 19 seconds should be added
        update.setRoute(createRoute(4347, 12793, 13237, 12654));

        assertEquals(update.toString(map).replace(" ", ""), expected.replace(" ", ""));


        // check the same for withdrawal
        update.setWithdrawal(true);
        update.setRoute(null);
        String expected2 = "347 ;  124;   84.205.71.0/24; True; False;   01/01/08 00:00:50;  []                           ;  None  ; Update";
        assertEquals(update.toString(map).replace(" ", ""), expected2.replace(" ", ""));
    }

}
