package nl.nlnetlabs.bgpsym01.coordinator;

import nl.nlnetlabs.bgpsym01.command.MasterCommand;

public interface Communicator {

    public void sendCommand(MasterCommand command);

    public void shutdown();

}