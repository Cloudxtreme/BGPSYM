package nl.nlnetlabs.bgpsym01.command;

import java.io.IOException;
import java.util.List;

import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.types.InvalidateUpdate;

public class InvalidateCommandTest extends AbstractTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testSerialize() throws IOException {
        check(getAS(0), getAS(11), getPrefixList(1, 2, 3), true);
        check(getAS(3), getAS(2), getPrefixList(1, 2, 3), true);
        check(getAS(9), getAS(3), getPrefixList(2, 3, 11), false);
        check(getAS(2), getAS(11), getPrefixList(), false);
        check(getAS(2), null, getPrefixList(), false);

    }

    public void testUpdateCreation() {
        checkUpdate(getAS(10), getPrefixList(1, 2, 3), true);
        checkUpdate(getAS(3), getPrefixList(3), true);
        checkUpdate(getAS(1), getPrefixList(3, 19), false);

    }

    private void checkUpdate(ASIdentifier as, List<Prefix> prefixList, boolean validate) {
        InvalidateCommand command = getInitializedCommand(null, as, prefixList, validate);
        InvalidateUpdate update = command.getUpdate();
        assertSame(update.getNeighborId(), as);
        assertSame(update.getPrefixes(), prefixList);
        assertEquals(update.isValidate(), validate);
    }

    private void check(ASIdentifier asId, ASIdentifier neighborASId, List<Prefix> list, boolean validate) throws IOException {
        InvalidateCommand command = getInitializedCommand(asId, neighborASId, list, validate);
        assertEquals(command, MasterCommand.readCommand(Rewriter.getStream(command)));
    }

    private InvalidateCommand getInitializedCommand(ASIdentifier asId, ASIdentifier neighborASId, List<Prefix> list, boolean validate) {
        InvalidateCommand command = new InvalidateCommand();
        command.setAsIdentifier(asId);
        command.setNeighborsIdentifier(neighborASId);
        command.setPrefixes(list);
        command.setValidate(validate);
        return command;
    }


}
