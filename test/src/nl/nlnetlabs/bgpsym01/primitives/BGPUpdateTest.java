package nl.nlnetlabs.bgpsym01.primitives;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.primitives.bgp.BGPUpdate;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.factories.ASFactory;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;

public class BGPUpdateTest extends AbstractTest {

    @Override
    protected void setUp() throws Exception {
        generateASes(1000);
    }

    public void testSerialize() throws IOException {
        BGPUpdate update = new BGPUpdate();
        update.setPrefixes(getPrefixList(4, 5, 67, 7));
        update.setRoute(createRoute(1, 2, 3, 4));
        update.setSender(ASFactory.getInstance(0));

        Collection<Prefix> withdrawals = new ArrayList<Prefix>();
        update.setWithdrawals(withdrawals);
        checkUpdate(update);

        withdrawals.add(getPrefix(17));
        checkUpdate(update);

        withdrawals.addAll(getPrefixList(1, 2, 3));
        checkUpdate(update);

        update.setPrefixes(null);
        checkUpdate(update);
    }

    private void checkUpdate(BGPUpdate update) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        EDataOutputStream eos = new EDataOutputStream(baos);
        update.writeExternal(eos);
        eos.close();

        EDataInputStream eis = new EDataInputStream(new ByteArrayInputStream(baos.toByteArray()));
        BGPUpdate newUpdate = new BGPUpdate();
        newUpdate.readExternal(eis);
        eis.close();

        assertEquals(update, newUpdate);

    }

}
