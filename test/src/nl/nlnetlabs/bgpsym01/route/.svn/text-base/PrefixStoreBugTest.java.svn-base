package nl.nlnetlabs.bgpsym01.route;

import java.util.ArrayList;

import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;
import nl.nlnetlabs.bgpsym01.primitives.timers.TimeControllerFactory;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

public class PrefixStoreBugTest extends AbstractTest {

    private PrefixStoreMapImpl store;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Prefix.init(1000);
        TimeControllerFactory.getTimeController();

        XProperties properties = XProperties.getInstance();
        properties.setPrefixArraySize(10000);
        XProperties.setInstance(properties);

        generateASes(10000);
        this.store = MockedPrefixStoreFactory.getStore();

    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        TimeControllerFactory.reload();
    }

    public void test1() {
        Prefix prefix = getPrefix(1);
        // get the prefix from 30 peers...
        for (int i = 0; i < 30; i++) {
            Route r = gr(i);
            store.prefixReceived(getAS(i), prefix, r);
        }

    }

    private Route gr(int i) {
        ArrayList<ASIdentifier> ids = new ArrayList<ASIdentifier>();
        for (int j = 0; j < i / 3 + 1; j++) {
            ids.add(getAS(101 + j));
        }
        Route r = new Route();
        r.setHops(ids.toArray(new ASIdentifier[0]));
        return r;
    }

}
