package nl.nlnetlabs.bgpsym01.coordinator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import nl.nlnetlabs.bgpsym01.command.AnnounceCommand;
import nl.nlnetlabs.bgpsym01.command.CoordinationCommand;
import nl.nlnetlabs.bgpsym01.command.MasterCommand;
import nl.nlnetlabs.bgpsym01.primitives.BGPSymException;
import nl.nlnetlabs.bgpsym01.primitives.bgp.BGPUpdate;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;

import org.apache.log4j.Logger;

public class CommunicatorMock implements Communicator {

    private static Logger log = Logger.getLogger(CommunicatorMock.class);

    private List<MasterCommand> commands = new LinkedList<MasterCommand>();

    public void sendCommand(MasterCommand command) {
        // make a copy
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            EDataOutputStream eos = new EDataOutputStream(baos);
            command.writeExternal(eos);
            eos.close();

            EDataInputStream eis = new EDataInputStream(new ByteArrayInputStream(baos.toByteArray()));
            command = (MasterCommand) CoordinationCommand.readCommand(eis);
        } catch (IOException e) {
            log.error(e);
            throw new BGPSymException(e);
        }

        if (command instanceof AnnounceCommand) {

            // we have to make a copy of that queue in order not to keep main
            // service
            // objects...
            AnnounceCommand cmd = (AnnounceCommand) command;
            BGPUpdate up = (BGPUpdate) cmd.getUpdate();
            if (up.getPrefixes() != null) {
                up.setPrefixes(new ArrayList<Prefix>(up.getPrefixes()));
            }
            if (up.getWithdrawals() != null) {
                up.setWithdrawals(new ArrayList<Prefix>(up.getWithdrawals()));
            }
        }
        commands.add(command);
    }

    public void setShowCancelMessage(boolean show) {
    }

    public void shutdown() {
    }

    public List<MasterCommand> getCommands() {
        return commands;
    }

    public void clear() {
        commands.clear();
    }

}
