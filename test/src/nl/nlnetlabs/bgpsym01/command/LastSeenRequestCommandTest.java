package nl.nlnetlabs.bgpsym01.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;

public class LastSeenRequestCommandTest extends AbstractTest {

    @Override
    protected void setUp() throws Exception {
        Prefix.init(1000);
        generateASes(1000);
    }

    /**
     * Tests whether
     * {@link LastSeenRequestCommand#getPrefixDataList(java.util.Collection)}
     * behaves correctly
     */
    public void testGetPrefixDataList() {
        // which prefixes do we like ?
        List<Prefix> prefixes = getPrefixList(1, 10, 11, 12);
        LastSeenRequestCommand lsrc = new LastSeenRequestCommand();
        lsrc.setPrefixes(prefixes);

        Collection<RouteViewDataResponse> responses = new ArrayList<RouteViewDataResponse>();
        ArrayList<RouteViewDataResponse> goodResponses = new ArrayList<RouteViewDataResponse>();

        addGoodOne(responses, goodResponses, getAS(1), getPrefix(1), 1, 2);

        // those are not interesting
        responses.add(new RouteViewDataResponse(getAS(2), getPrefix(3), 1, 7));
        responses.add(new RouteViewDataResponse(getAS(7), getPrefix(2), 1, 7));

        // those are cool
        addGoodOne(responses, goodResponses, getAS(10), getPrefix(10), 1, -1);
        addGoodOne(responses, goodResponses, getAS(10), getPrefix(11), 1, -1);

        // this one isn't
        responses.add(new RouteViewDataResponse(getAS(17), getPrefix(27), 10, 21));

        // just check that at least one is cool and that at least one isn't
        assertFalse(responses.size() == goodResponses.size());
        assertTrue(goodResponses.size() > 0);

        List<RouteViewDataResponse> list = lsrc.getPrefixDataList(responses);
        compareCollectionWithIterator(list, goodResponses.iterator());

    }

    private void addGoodOne(Collection<RouteViewDataResponse> responses, ArrayList<RouteViewDataResponse> goodResponses, ASIdentifier asX, Prefix prefixX, long x1,
            long x2) {
        RouteViewDataResponse resp2 = new RouteViewDataResponse(asX, prefixX, x1, x2);
        responses.add(resp2);
        goodResponses.add(resp2);
    }

    public void testSerialize() throws IOException {
        LastSeenRequestCommand lsrc = new LastSeenRequestCommand();

        List<Prefix> list = new ArrayList<Prefix>();

        for (int i = 0; i < 30; i++) {
            list.add(Prefix.getInstance(i));
        }

        lsrc.setPrefixes(list);

        assertEquals(lsrc, CoordinationCommand.readCommand(Rewriter.getStream(lsrc)));
    }

}
